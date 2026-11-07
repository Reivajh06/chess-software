import { pieceValues } from "./state.js";

export const cheatCosts = { double: 20, dodge: 15, rewind: 25, flip: 40 };
export const cheatChances = { double: 0.3, dodge: 0.5, rewind: 0.65, flip: 1 };
export const flipLead = 5;
export const cleanPlies = 6;

const files = "abcdefgh";

export function teleport(from, to) {
	return { from, to, moveId: 0, promotion: null };
}

export function neighbours(tile) {
	const file = files.indexOf(tile[0]);
	const rank = Number(tile[1]);
	const result = [];
	for(const df of [-1, 0, 1]) {
		for(const dr of [-1, 0, 1]) {
			if(!df && !dr) continue;
			const f = file + df;
			const r = rank + dr;
			if(f >= 0 && f < 8 && r >= 1 && r <= 8) result.push(`${files[f]}${r}`);
		}
	}
	return result;
}

export function freeNeighbours(pieces, tile) {
	return neighbours(tile).filter((square) => !pieces.has(square));
}

export function materialLead(pieces, color) {
	let lead = 0;
	for(const piece of pieces.values()) lead += (pieceValues[piece.type] ?? 0) * (piece.color === color ? 1 : -1);
	return lead;
}

export function secondMove(options, first, pieces) {
	const busy = new Set([first.from, first.to]);
	const candidates = options.filter((option) =>
		!busy.has(option.from) && !busy.has(option.to) && pieces.get(option.to)?.type !== "KING");
	return candidates[Math.floor(Math.random() * candidates.length)] ?? null;
}
