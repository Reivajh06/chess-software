export const gameModes = {
  PVP: {
    name: "Contra una persona",
    badge: "PvP",
    icon: "KNIGHT",
    detail: "Sala privada e invitación",
    action: "Crear sala",
    available: true,
  },
  PVAI: {
    name: "Contra IA",
    badge: "PvAI",
    icon: "QUEEN",
    detail: "Modelos locales de Ollama",
    action: "Jugar contra IA",
    available: false,
    reason: "El servidor todavía no expone modelos de Ollama ni ejecuta jugadas de IA.",
  },
  AIVAI: {
    name: "IA contra IA",
    badge: "Espectador",
    icon: "BISHOP",
    detail: "Observar una partida entre modelos",
    action: "Observar partida",
    available: false,
    reason: "La creación de partidas entre modelos todavía no está disponible en el servidor.",
  },
  PVCPU: {
    name: "Contra CPU",
    badge: "PvCPU",
    icon: "ROOK",
    detail: "Rival controlado por el servidor",
    action: "Jugar contra CPU",
    available: false,
    reason: "El proyecto define este modo, pero todavía no integra un motor ni sus dificultades.",
  },
  PRACTICE: {
    name: "Práctica",
    badge: "Libre",
    icon: "PAWN",
    detail: "Controlar blancas y negras",
    action: "Iniciar práctica",
    available: true,
  },
};

export function modeDefinition(id) {
  return gameModes[id] ?? gameModes.PVP;
}
