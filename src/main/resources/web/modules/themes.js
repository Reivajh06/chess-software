import { element } from "./ui.js";
export const themes = {
	classic: {
		name: "Classic",
		group: "Basic",
		pieces: "classic_pieces.png",
		tiles: "classic_tiles.png",
		colors: {
			light: "#ded0b8",
			dark: "#333d54",
		},
	},
	warm: {
		name: "Warm",
		group: "Basic",
		pieces: "warm_pieces.png",
		tiles: "warm_tiles.png",
		colors: {
			light: "#ead9b8",
			dark: "#713b3e",
		},
	},
	cold: {
		name: "Cold",
		group: "Basic",
		pieces: "cold_pieces.png",
		tiles: "cold_tiles.png",
		colors: {
			light: "#c4dae9",
			dark: "#2c466b",
		},
	},
	gothic: {
		name: "Gothic",
		group: "Fantasy",
		pieces: "gothic_pieces.png",
		tiles: "gothic_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#b9aec9",
			dark: "#45344f",
		},
	},
	medieval: {
		name: "Medieval",
		group: "Fantasy",
		pieces: "medieval_pieces.png",
		tiles: "medieval_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#d9c391",
			dark: "#515847",
		},
	},
	halloween: {
		name: "Halloween",
		group: "Seasonal",
		pieces: "halloween_pieces.png",
		tiles: "halloween_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#d6b087",
			dark: "#50375e",
		},
	},
	christmas: {
		name: "Christmas",
		group: "Seasonal",
		pieces: "christmas_pieces.png",
		tiles: "christmas_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#e0dac6",
			dark: "#354f42",
		},
	},
	python: {
		name: "Python",
		group: "Languages",
		pieces: "python_pieces.png",
		tiles: "python_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#e4d198",
			dark: "#344f68",
		},
	},
	java: {
		name: "Java",
		group: "Languages",
		pieces: "java_pieces.png",
		tiles: "java_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#dedbd5",
			dark: "#57413b",
		},
	},
	warhammer: {
		name: "Warhammer 40k",
		group: "Crossover",
		pieces: "warhammer_pieces.png",
		tiles: "warhammer_tiles.png",
		spriteLayout: "redesigned",
		colors: {
			light: "#c9c3b6",
			dark: "#5a2547",
		},
	},
};

// Board themes only change the board and the pieces; the castle chrome keeps its own palette.
export function applyTheme(id) {
	const theme = themes[id] ?? themes.classic;
	document.documentElement.dataset.theme = id;
	document.documentElement.style.setProperty("--light", theme.colors.light);
	document.documentElement.style.setProperty("--dark", theme.colors.dark);
}

export function populateThemes(select) {
	let group;
	for(const [id, theme] of Object.entries(themes)) {
		if(group?.label !== theme.group) {
			group = document.createElement("optgroup");
			group.label = theme.group;
			select.append(group);
		}
		const option = element("option");
		option.value = id;
		option.textContent = theme.name;
		group.append(option);
	}
}
