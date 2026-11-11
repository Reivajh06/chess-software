import { strongestEngine } from "./engine-plugins.js";

export const gameModes = {
	PVP: {
		hint: "invite a friend. chess really tests the warranty on those.",
		name: "Play a person",
		badge: "PvP",
		detail: "By invitation or on this device",
	},
	LOCAL: {
		name: "Two on this device",
		badge: "Local",
		server: "PVP",
	},
	AIVAI: {
		hint: "two machines, one job. somehow you still get to call this a hobby.",
		name: "Machine vs machine",
		badge: "Machine vs Machine",
		detail: "Choose two plugins and watch",
		pluginPrompts: ["Which plugin plays White?", "Which plugin plays Black?"],
	},
	PVCPU: {
		hint: "pick a machine. finally, someone who can process all your excuses.",
		name: "Play a machine",
		badge: "vs Machine",
		detail: "Choose a plugin and difficulty",
		pluginPrompts: ["Which plugin will be your opponent?"],
	},
	SPECTATE: {
		name: "Spectator",
		badge: "Spectator",
	},
	PRACTICE: {
		hint: "move both sides. finally, someone who agrees with your excuses.",
		name: "Practice",
		badge: "Free play",
		detail: "Move both colors freely",
	},
};

export const cpuLevels = [
	{ id: "EASY", name: "Easy", detail: "A warm-up" },
	{ id: "MEDIUM", name: "Medium", detail: "Starting to bite" },
	{ id: "HARD", name: "Hard", detail: "An unforgiving opponent" },
	{ id: "MAX", name: "Max", detail: "The computer at full strength" },
];

export function cpuLevelsFor(plugins) {
	return strongestEngine(plugins)
		? [...cpuLevels, { id: "ALFI", name: "???", detail: "…" }]
		: [...cpuLevels];
}

export const menuOrder = ["PVP", "PRACTICE", "PVCPU", "AIVAI"];

export const pluginLabel = (name) => name.replace(/\.jar$/i, "");

export function modeDefinition(id) {
	return gameModes[id === "PVAI" ? "PVCPU" : id] ?? gameModes.PVP;
}

export const modeLabel = (id) => ["PVAI", "PVCPU", "AIVAI"].includes(id)
	? modeDefinition(id).badge : modeDefinition(id).name;
export const modeIcon = (id) => `mode-${({ PVAI: "PVCPU", LOCAL: "PVP", SPECTATE: "AIVAI" }[id] ?? id ?? "PRACTICE").toLowerCase()}`;
