export const gameModes = {
  PVP: {
    hint: "Invita a un amigo. O a un enemigo, yo no juzgo.",
    name: "Contra una persona",
    badge: "PvP",
    detail: "Invitación o en este equipo",
    available: true,
  },
  // Se elige desde el diálogo de PvP, no aparece en el menú principal.
  LOCAL: {
    name: "Dos en este equipo",
    badge: "Local",
    available: true,
    // El servidor no distingue este modo: es una sala PvP con los dos asientos ocupados.
    server: "PVP",
  },
  PVAI: {
    hint: "La IA aún está aprendiendo. Como Clippy, pero con ganas.",
    name: "Contra IA",
    badge: "PvAI",
    detail: "Modelos locales de Ollama",
    available: false,
    reason: "El servidor todavía no expone modelos de Ollama ni ejecuta jugadas de IA.",
  },
  AIVAI: {
    hint: "Dos IAs peleándose. Pronto. Ve preparando palomitas.",
    name: "IA contra IA",
    badge: "Espectador",
    detail: "Observar una partida entre modelos",
    available: false,
    reason: "La creación de partidas entre modelos todavía no está disponible en el servidor.",
  },
  PVCPU: {
    hint: "La CPU sigue en el horno. Huele a silicio quemado.",
    name: "Contra CPU",
    badge: "PvCPU",
    detail: "Rival controlado por el servidor",
    available: false,
    reason: "El proyecto define este modo, pero todavía no integra un motor ni sus dificultades.",
  },
  PRACTICE: {
    hint: "Juegas contra ti mismo. Por fin un rival a tu altura.",
    name: "Práctica",
    badge: "Libre",
    detail: "Mueve ambos colores libremente",
    available: true,
  },
};

export const menuOrder = ["PVP", "PRACTICE", "PVAI", "PVCPU", "AIVAI"];

export function modeDefinition(id) {
  return gameModes[id] ?? gameModes.PVP;
}
