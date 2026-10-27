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
    hint: "Contra la máquina. No se cansa, no se queja y no acepta sobornos.",
    name: "Contra CPU",
    badge: "PvCPU",
    detail: "Elige la dificultad",
    available: true,
  },
  PRACTICE: {
    hint: "Juegas contra ti mismo. Por fin un rival a tu altura.",
    name: "Práctica",
    badge: "Libre",
    detail: "Mueve ambos colores libremente",
    available: true,
  },
};

// Dificultades del servidor (enum Difficulty). La máxima esconde una sorpresa.
export const cpuLevels = [
  { id: "EASY", name: "Fácil", detail: "Para calentar" },
  { id: "MEDIUM", name: "Media", detail: "Ya muerde un poco" },
  { id: "HARD", name: "Difícil", detail: "La máquina a pleno rendimiento" },
];

// Tipo de jugador que se envía a /moves cuando mueve una máquina.
export const machineTypes = { PVCPU: "CPU", PVAI: "AI", AIVAI: "AI" };

export const menuOrder = ["PVP", "PRACTICE", "PVAI", "PVCPU", "AIVAI"];

export function modeDefinition(id) {
  return gameModes[id] ?? gameModes.PVP;
}
