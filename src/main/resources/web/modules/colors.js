let swapped = false;

export function setColorSwap(on) {
	swapped = on;
}

export function colorSwapped() {
	return swapped;
}

export function shownColor(color) {
	if(!swapped || !color) return color;
	return color === "WHITE" ? "BLACK" : "WHITE";
}
