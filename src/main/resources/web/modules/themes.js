export const themes = {
  classic: {
    name: "Clásico",
    group: "Base",
    pieces: "classic_pieces.png",
    tiles: "classic_tiles.png",
    colors: {
      background: "#0c1424",
      panel: "#141f35",
      surface: "#203253",
      hover: "#304973",
      text: "#edf4ff",
      muted: "#9db3d8",
      accent: "#44bcff",
      accentHover: "#8bdcff",
      border: "#486697",
      light: "#ded0b8",
      dark: "#333d54",
    },
  },
  warm: {
    name: "Cálido",
    group: "Base",
    pieces: "warm_pieces.png",
    tiles: "warm_tiles.png",
    colors: {
      background: "#1d1515",
      panel: "#2a1d20",
      surface: "#382529",
      hover: "#54343a",
      text: "#f1e5d4",
      muted: "#c3aca4",
      accent: "#e7c58f",
      accentHover: "#f7dcae",
      border: "#59333b",
      light: "#ead9b8",
      dark: "#713b3e",
    },
  },
  cold: {
    name: "Frío",
    group: "Base",
    pieces: "cold_pieces.png",
    tiles: "cold_tiles.png",
    colors: {
      background: "#101820",
      panel: "#192630",
      surface: "#233340",
      hover: "#344c5f",
      text: "#e0edf2",
      muted: "#9eafc0",
      accent: "#b8d7e6",
      accentHover: "#d6ecf6",
      border: "#314758",
      light: "#c4dae9",
      dark: "#2c466b",
    },
  },
  gothic: {
    name: "Gótico",
    group: "Fantasía",
    pieces: "gothic_pieces.png",
    tiles: "gothic_tiles.png",
    spriteLayout: "redesigned",
    colors: {
      background: "#19131e",
      panel: "#251c2d",
      surface: "#34263e",
      hover: "#4d385b",
      text: "#ece3f2",
      muted: "#b9a5c6",
      accent: "#c7afd9",
      accentHover: "#e2ccec",
      border: "#45314e",
      light: "#b9aec9",
      dark: "#45344f",
    },
  },
  medieval: {
    name: "Medieval",
    group: "Fantasía",
    pieces: "medieval_pieces.png",
    tiles: "medieval_tiles.png",
    spriteLayout: "redesigned",
    colors: {
      background: "#191b15",
      panel: "#25281f",
      surface: "#35392a",
      hover: "#4c513b",
      text: "#eee8d8",
      muted: "#b8b79b",
      accent: "#d5c28e",
      accentHover: "#ede0b5",
      border: "#414733",
      light: "#d9c391",
      dark: "#515847",
    },
  },
  halloween: {
    name: "Halloween",
    group: "Temporada",
    pieces: "halloween_pieces.png",
    tiles: "halloween_tiles.png",
    spriteLayout: "redesigned",
    colors: {
      background: "#1c1520",
      panel: "#2b1f2d",
      surface: "#3b293b",
      hover: "#594052",
      text: "#f4e2d1",
      muted: "#c0a6ba",
      accent: "#e8b178",
      accentHover: "#f5ca92",
      border: "#513547",
      light: "#d6b087",
      dark: "#50375e",
    },
  },
  christmas: {
    name: "Navidad",
    group: "Temporada",
    pieces: "christmas_pieces.png",
    tiles: "christmas_tiles.png",
    spriteLayout: "redesigned",
    colors: {
      background: "#121d1a",
      panel: "#1b2b24",
      surface: "#283c31",
      hover: "#3a5143",
      text: "#eeeadd",
      muted: "#adbbae",
      accent: "#dfd4a7",
      accentHover: "#f0e6c4",
      border: "#3b5141",
      light: "#e0dac6",
      dark: "#354f42",
    },
  },
  python: {
    name: "Python",
    group: "Lenguajes",
    pieces: "python_pieces.png",
    tiles: "python_tiles.png",
    spriteLayout: "redesigned",
    colors: {
      background: "#131c25",
      panel: "#1e2b36",
      surface: "#293b4b",
      hover: "#3d5264",
      text: "#eee8d6",
      muted: "#a6b8c8",
      accent: "#ead08b",
      accentHover: "#f5e2ac",
      border: "#364c61",
      light: "#e4d198",
      dark: "#344f68",
    },
  },
  java: {
    name: "Java",
    group: "Lenguajes",
    pieces: "java_pieces.png",
    tiles: "java_tiles.png",
    spriteLayout: "redesigned",
    colors: {
      background: "#201917",
      panel: "#2b2421",
      surface: "#3b302a",
      hover: "#574237",
      text: "#eee4dc",
      muted: "#c1aca0",
      accent: "#e0af8c",
      accentHover: "#f2cfad",
      border: "#5a3d30",
      light: "#dedbd5",
      dark: "#57413b",
    },
  },
};

const tokens = {
  background: "--bg",
  panel: "--panel",
  surface: "--surface",
  hover: "--surface-hover",
  text: "--text",
  muted: "--muted",
  accent: "--accent",
  accentHover: "--accent-hover",
  border: "--line",
  light: "--light",
  dark: "--dark",
};

export function applyTheme(id) {
  const theme = themes[id] ?? themes.classic;
  document.documentElement.dataset.theme = id;
  for (const [name, color] of Object.entries(theme.colors)) {
    document.documentElement.style.setProperty(tokens[name], color);
  }
  document.querySelector('meta[name="theme-color"]').content = theme.colors.background;
}

export function populateThemes(select) {
  let group;
  for (const [id, theme] of Object.entries(themes)) {
    if (group?.label !== theme.group) {
      group = document.createElement("optgroup");
      group.label = theme.group;
      select.append(group);
    }
    const option = document.createElement("option");
    option.value = id;
    option.textContent = theme.name;
    group.append(option);
  }
}
