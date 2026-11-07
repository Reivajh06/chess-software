const glyphs = {
	board: [
		"#.#.#.#",
		".#.#.#.",
		"#.#.#.#",
		".#.#.#.",
		"#.#.#.#",
		".#.#.#.",
		"#.#.#.#",
	],
	trophy: [
		"#######",
		"#.###.#",
		".#####.",
		"..###..",
		"...#...",
		"..###..",
		".#####.",
	],
	cross: [
		"##...##",
		".##.##.",
		"..###..",
		"...#...",
		"..###..",
		".##.##.",
		"##...##",
	],
	equal: [
		".......",
		"#######",
		"#######",
		".......",
		"#######",
		"#######",
		".......",
	],
	hourglass: [
		"#######",
		".#...#.",
		"..#.#..",
		"...#...",
		"..#.#..",
		".#####.",
		"#######",
	],
	scroll: [
		"######.",
		"#....#.",
		"#.##.#.",
		"#....#.",
		"#.##.#.",
		"#....##",
		".######",
	],
	play: [
		".#.....",
		".###...",
		".#####.",
		".######",
		".#####.",
		".###...",
		".#.....",
	],
	pause: [
		".##.##.",
		".##.##.",
		".##.##.",
		".##.##.",
		".##.##.",
		".##.##.",
		".##.##.",
	],
};

export function glyph(name) {
	const rows = glyphs[name];
	const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	svg.classList.add("pixel-glyph");
	svg.setAttribute("viewBox", `0 0 ${rows[0].length} ${rows.length}`);
	svg.setAttribute("shape-rendering", "crispEdges");
	svg.setAttribute("aria-hidden", "true");

	rows.forEach((row, y) => {
		[...row].forEach((cell, x) => {
			if(cell !== "#") return;
			const rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
			rect.setAttribute("x", x);
			rect.setAttribute("y", y);
			rect.setAttribute("width", 1);
			rect.setAttribute("height", 1);
			svg.append(rect);
		});
	});

	return svg;
}

export function fillGlyphs(root = document) {
	for(const slot of root.querySelectorAll("[data-glyph]")) {
		slot.replaceChildren(glyph(slot.dataset.glyph));
	}
}
