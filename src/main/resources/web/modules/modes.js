export const gameModes = {
	PVP: {
		hint: "Invita a un amigo. O a un enemigo, yo no juzgo.",
		name: "Contra una persona",
		badge: "PvP",
		detail: "Invitación o en este equipo",
		available: true,
	},
	LOCAL: {
		name: "Dos en este equipo",
		badge: "Local",
		available: true,
		server: "PVP",
	},
	PVAI: {
		hint: "La IA aún está aprendiendo. Como Clippy, pero con ganas.",
		name: "Contra IA",
		badge: "PvAI",
		detail: "Elige un plugin instalado",
		available: true,
		pluginPrompts: ["¿Qué plugin será tu rival?"],
	},
	AIVAI: {
		hint: "Dos IAs peleándose. Ve preparando palomitas.",
		name: "IA contra IA",
		badge: "Espectador",
		detail: "Elige dos plugins y observa",
		available: true,
		pluginPrompts: ["¿Qué plugin juega con blancas?", "¿Qué plugin juega con negras?"],
	},
	PVCPU: {
		hint: "Contra la máquina. No se cansa, no se queja y no acepta sobornos.",
		name: "Contra CPU",
		badge: "PvCPU",
		detail: "Elige plugin y dificultad",
		available: true,
		pluginPrompts: ["¿Qué plugin moverá la CPU?"],
	},
	PRACTICE: {
		hint: "Juegas contra ti mismo. Por fin un rival a tu altura.",
		name: "Práctica",
		badge: "Libre",
		detail: "Mueve ambos colores libremente",
		available: true,
	},
};

export const cpuLevels = [
	{ id: "EASY", name: "Fácil", detail: "Para calentar" },
	{ id: "MEDIUM", name: "Media", detail: "Ya muerde un poco" },
	{ id: "HARD", name: "Difícil", detail: "La máquina a pleno rendimiento" },
];

export const menuOrder = ["PVP", "PRACTICE", "PVAI", "PVCPU", "AIVAI"];

export const pluginLabel = (name) => name.replace(/\.jar$/i, "");

export function modeDefinition(id) {
	return gameModes[id] ?? gameModes.PVP;
}
