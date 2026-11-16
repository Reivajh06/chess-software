import { pieceValues } from "./state.js";

export const cheatChances = { double: 0.15, dodge: 0.5, flip: 1 };
export const flipLead = 5;
export const cleanPlies = 6;


export function materialLead(pieces, color) {
	let lead = 0;
	for(const piece of pieces.values()) lead += (pieceValues[piece.type] ?? 0) * (piece.color === color ? 1 : -1);
	return lead;
}

