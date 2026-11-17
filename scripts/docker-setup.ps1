<#
.SYNOPSIS
  One-shot Docker setup for Archchess, equivalent to scripts/setup.ps1 plus the README's Docker section.

.DESCRIPTION
  1. Runs setup.ps1 (plugins library + stockfish.jar into the plugins folder).
  2. Compiles the backend and copies its runtime dependencies (the Dockerfile copies target/).
  3. Builds the image with a Linux Stockfish installed (INSTALL_STOCKFISH=true), because stockfish.jar
     only bundles a Windows engine.
  4. Starts the container with the data volume and the plugins folder bind-mounted read-only.

.PARAMETER PluginsDir
  Host folder with the plugin jars; mounted at /data/plugins (default: temp/plugins).

.PARAMETER Port
  Host port for the web app (default 5000).

.PARAMETER OllamaHost / OllamaModel
  Optional: passed to the container as OLLAMA_HOST / OLLAMA_MODEL (e.g. http://host.docker.internal:11434).

.PARAMETER NoRun
  Build the image but do not start the container.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File scripts\docker-setup.ps1
  powershell -ExecutionPolicy Bypass -File scripts\docker-setup.ps1 -OllamaHost http://host.docker.internal:11434 -OllamaModel llama3
#>
[CmdletBinding()]
param(
    [string]$PluginsDir,
    [int]$Port = 5000,
    [string]$OllamaHost,
    [string]$OllamaModel,
    [string]$ImageName = 'archchess',
    [string]$ContainerName = 'archchess',
    [switch]$NoRun
)

$ErrorActionPreference = 'Stop'

$ProjectDir = Split-Path -Parent $PSScriptRoot
if (-not $PluginsDir) { $PluginsDir = Join-Path $ProjectDir 'temp\plugins' }

function Step($text) { Write-Host "`n==> $text" -ForegroundColor Cyan }

function Run($file, [string[]]$arguments) {
    $ErrorActionPreference = 'Continue'
    & $file @arguments
    if ($LASTEXITCODE -ne 0) { throw "'$file $($arguments -join ' ')' failed (exit $LASTEXITCODE)" }
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw "'docker' not found. Install Docker Desktop." }
cmd /c "docker info >nul 2>&1"
if ($LASTEXITCODE -ne 0) { throw 'The Docker daemon is not running. Start Docker Desktop and retry.' }

# --- 1. Plugins library + Stockfish jar ---------------------------------------------------------------------------
Step 'Running setup.ps1 (library + plugins)'
& (Join-Path $PSScriptRoot 'setup.ps1') -PluginsDir $PluginsDir
if (-not $?) { throw 'setup.ps1 failed.' }
$PluginsDir = (Resolve-Path $PluginsDir).Path

# --- 2. Compile the backend ---------------------------------------------------------------------------------------
Step 'Compiling the backend'
Push-Location $ProjectDir
try {
    Run mvn @('-B', '-q', '-DskipTests', 'package', 'dependency:copy-dependencies', '-DincludeScope=runtime')

    # --- 3. Image -------------------------------------------------------------------------------------------------
    Step "Building image '$ImageName' (with Linux Stockfish)"
    Run docker @('build', '--build-arg', 'INSTALL_STOCKFISH=true', '-t', $ImageName, '.')
} finally { Pop-Location }

if ($NoRun) {
    Write-Host "`nImage '$ImageName' built. Not starting it (-NoRun)."
    return
}

# --- 4. Container -------------------------------------------------------------------------------------------------
Step "Starting container '$ContainerName'"
$existing = & docker ps -a --filter "name=^/$ContainerName$" --format '{{.ID}}'
if ($existing) {
    Write-Host "Replacing the existing container (the 'archchess-data' volume is kept)."
    Run docker @('rm', '-f', $ContainerName)
}

$runArgs = @(
    'run', '-d', '--name', $ContainerName,
    '-p', "${Port}:5000",
    '-v', 'archchess-data:/data',
    '-v', "${PluginsDir}:/data/plugins:ro",
    '--add-host=host.docker.internal:host-gateway'
)
if ($OllamaHost)  { $runArgs += @('-e', "OLLAMA_HOST=$OllamaHost") }
if ($OllamaModel) { $runArgs += @('-e', "OLLAMA_MODEL=$OllamaModel") }
$runArgs += $ImageName
Run docker $runArgs

Step 'Done'
Write-Host "Open http://localhost:$Port"
Write-Host "Logs  : docker logs -f $ContainerName   (look for: Plugins loaded: [...])"
Write-Host "Stop  : docker rm -f $ContainerName      (data stays in the 'archchess-data' volume)"
