<#
.SYNOPSIS
  One-shot setup for Archchess: plugins library + Stockfish plugin.

.DESCRIPTION
  1. Clones archchess-plugins-library next to this project and installs it in the local Maven repository.
  2. Gets stockfish.jar (latest GitHub release if there is one, otherwise clones archchess-stockfish-plugin
     next to this project and builds it) and copies it to the plugins folder.

.PARAMETER PluginsDir
  Folder the backend scans for plugin jars (default: temp/plugins, same as plugins.path).

.PARAMETER ForceBuild
  Skip the release download and always build Stockfish from source.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File scripts\setup.ps1
  powershell -ExecutionPolicy Bypass -File scripts\setup.ps1 -PluginsDir C:\archchess\plugins
#>
[CmdletBinding()]
param(
    [string]$PluginsDir,
    [switch]$ForceBuild
)

$ErrorActionPreference = 'Stop'

$ProjectDir = Split-Path -Parent $PSScriptRoot
$ParentDir  = Split-Path -Parent $ProjectDir
if (-not $PluginsDir) { $PluginsDir = Join-Path $ProjectDir 'temp\plugins' }

$LibraryRepo   = 'https://github.com/Reivajh06/archchess-plugins-library.git'
$StockfishRepo = 'https://github.com/Reivajh06/archchess-stockfish-plugin.git'
$StockfishApi  = 'https://api.github.com/repos/Reivajh06/archchess-stockfish-plugin/releases/latest'

function Step($text) { Write-Host "`n==> $text" -ForegroundColor Cyan }

function Require($command, $hint) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "'$command' not found. $hint"
    }
}

function Run($file, [string[]]$arguments) {
    $ErrorActionPreference = 'Continue'   # native tools write progress to stderr; check the exit code instead
    & $file @arguments
    if ($LASTEXITCODE -ne 0) { throw "'$file $($arguments -join ' ')' failed (exit $LASTEXITCODE)" }
}

# Clone if missing, otherwise fast-forward to the latest commit.
function Sync-Repo($url, $dir) {
    if (Test-Path (Join-Path $dir '.git')) {
        Write-Host "Updating $dir"
        Run git @('-C', $dir, 'pull', '--ff-only')
    } else {
        Write-Host "Cloning $url -> $dir"
        Run git @('clone', '--depth', '1', $url, $dir)
    }
}

# Same download as the plugin repo's scripts/fetch-stockfish.sh, without needing bash (CRLF checkouts break it).
function Get-StockfishEngine($repoDir) {
    $version = 'sf_19'
    $asset   = 'stockfish-windows-x86-64-universal'
    $dest    = Join-Path $repoDir 'src\main\resources\engines'
    $exe     = Join-Path $dest 'windows\stockfish.exe'
    if (Test-Path $exe) { Write-Host 'Stockfish engine already downloaded.'; return }

    $tmp = Join-Path ([IO.Path]::GetTempPath()) ("archchess-sf-" + [guid]::NewGuid())
    New-Item -ItemType Directory -Force -Path $tmp, (Join-Path $dest 'windows') | Out-Null
    try {
        Write-Host "Downloading Stockfish $version (~100 MB)"
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri "https://github.com/official-stockfish/Stockfish/releases/download/$version/$asset.zip" -OutFile "$tmp\sf.zip"
        Expand-Archive "$tmp\sf.zip" -DestinationPath $tmp -Force
        Copy-Item (Get-ChildItem $tmp -Recurse -Filter "$asset.exe" | Select-Object -First 1).FullName $exe
        Copy-Item (Get-ChildItem $tmp -Recurse -Filter Copying.txt | Select-Object -First 1).FullName (Join-Path $dest 'STOCKFISH-COPYING.txt')
    } finally { Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue }
}

# --- Prerequisites ------------------------------------------------------------------------------------------------
# Finds a JDK 25 (JAVA_HOME first, then the usual install folders) and puts it first in PATH for this process only,
# so an older default JDK does not matter and nothing global is changed.
function Use-Jdk25 {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
    $roots = @(
        (Join-Path $env:USERPROFILE '.jdks'),
        (Join-Path $env:USERPROFILE '.gradle\jdks'),
        "$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Microsoft",
        "$env:ProgramFiles\Amazon Corretto", "$env:ProgramFiles\Zulu", "$env:ProgramFiles\BellSoft", "$env:ProgramFiles\Semeru"
    )
    foreach ($root in $roots) {
        if (Test-Path $root) {
            $candidates += Get-ChildItem $root -Directory -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -match '(^|[^0-9])25([^0-9]|$)' } | ForEach-Object { $_.FullName }
        }
    }
    foreach ($dir in $candidates) {
        $java = Join-Path $dir 'bin\java.exe'
        if (-not (Test-Path $java)) { continue }
        $out = cmd /c "`"$java`" -version 2>&1" | Select-Object -First 1
        if ($out -match '"25') {
            $env:JAVA_HOME = $dir
            $env:Path = "$dir\bin;$env:Path"
            Write-Host "Using JDK 25 at $dir"
            return
        }
    }
    throw "No JDK 25 found. Install one (or set JAVA_HOME to it) and retry."
}

Step 'Checking prerequisites'
Require git  'Install Git.'
Require mvn  'Install Maven 3.9+.'
Use-Jdk25

$mvnJava = (cmd /c "mvn -v 2>&1" | Select-String "Java version" | Select-Object -First 1).ToString()
if ($mvnJava -notmatch 'Java version: 25') {
    throw "Maven is not running on JDK 25 ($mvnJava). Check that JAVA_HOME points to a JDK 25."
}
Write-Host "Java and Maven are on JDK 25."

# --- 1. Plugins library -------------------------------------------------------------------------------------------
Step 'Installing archchess-plugins library'
$libraryDir = Join-Path $ParentDir 'archchess-plugins-library'
Sync-Repo $LibraryRepo $libraryDir
Push-Location $libraryDir
try { Run mvn @('-B', '-q', '-DskipTests', 'install') } finally { Pop-Location }
Write-Host 'archchess-plugins:1.0 installed in the local Maven repository.'

# --- 2. Stockfish plugin ------------------------------------------------------------------------------------------
New-Item -ItemType Directory -Force -Path $PluginsDir | Out-Null
$target = Join-Path $PluginsDir 'stockfish.jar'
$done = $false

if (-not $ForceBuild) {
    Step 'Looking for a Stockfish plugin release'
    try {
        $release = Invoke-RestMethod -Uri $StockfishApi -Headers @{ 'User-Agent' = 'archchess-setup' }
        $asset = $release.assets | Where-Object { $_.name -eq 'stockfish.jar' } | Select-Object -First 1
        if ($asset) {
            Write-Host "Downloading $($release.tag_name) ($([math]::Round($asset.size / 1MB)) MB)"
            Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $target
            $done = $true
        }
    } catch {
        Write-Host "No usable release ($($_.Exception.Message)); building from source."
    }
}

if (-not $done) {
    Step 'Building the Stockfish plugin from source'
    $stockfishDir = Join-Path $ParentDir 'archchess-stockfish-plugin'
    Sync-Repo $StockfishRepo $stockfishDir
    Push-Location $stockfishDir
    try {
        Get-StockfishEngine $stockfishDir
        Run mvn @('-B', '-q', '-DskipTests', 'package')
    } finally { Pop-Location }
    Copy-Item (Join-Path $stockfishDir 'target\stockfish.jar') $target -Force
}

# --- Done ---------------------------------------------------------------------------------------------------------
Step 'Done'
Write-Host "Library : archchess-plugins:1.0 (local Maven repository)"
Write-Host "Plugin  : $target"
Write-Host "`nNext: build and run the backend (README, section 2). On first run Stockfish unpacks itself to <tmp>\archchess-stockfish."
