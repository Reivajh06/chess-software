import { labels, pieceValues } from "./state.js";
import { shownColor } from "./colors.js";

const chance = {
	check: 1, promotion: 1, blunder: 1, queenLost: 1, queenWon: 1, castle: 1, enPassant: 1, comeback: 1,
	bongcloud: 1, quickMate: 1, bishopLost: 0.8, fianchetto: 0.6,
	openingCenter: 0.7, openingWeird: 1, openingOther: 0.5, checkAgain: 0.8,
	bigLead: 0.7, trade: 0.5, earlyQueen: 0.8, kingWalk: 0.5, knightRim: 0.5, pawnRush: 0.6,
	minorWon: 0.4, capture: 0.6, repeat: 0.6, milestone: 0.5, longGame: 1,
};
const priorities = {
	check: 2, checkAgain: 2, promotion: 2, blunder: 2, queenLost: 2, queenWon: 2, comeback: 2, quickMate: 3, bongcloud: 2,
};
const quietPlies = 3;

const fileOf = (square) => square?.charCodeAt(0) - 97;
const rankOf = (square) => Number(square?.[1]);

function material(pieces) {
	let lead = 0;
	for(const piece of pieces?.values() ?? []) lead += (pieceValues[piece.type] ?? 0) * (piece.color === "WHITE" ? 1 : -1);
	return lead;
}

export class Commentator {
	constructor() {
		this.reset();
	}

	reset() {
		this.lastSpokePly = -Infinity;
		this.said = new Set();
		this.queenMoved = new Set();
		this.lastWasCheck = false;
	}

	react({ move, before, after, userColors }) {
		if(!move) return null;
		const events = this.events({ move, before, after, userColors });
		this.lastWasCheck = after.status === "ACTIVE" && !!after.checkedColor;
		const ply = move.ply ?? after.moves.length;
		for(const event of events) {
			const priority = priorities[event.kind] ?? (event.kind === "check" ? 2 : 1);
			if(priority < 2 && ply - this.lastSpokePly < quietPlies) continue;
			if(Math.random() >= (chance[event.kind] ?? 0.5)) continue;
			this.lastSpokePly = ply;
			return { ...event, priority, vars: this.vars(move, after, event) };
		}
		return null;
	}

	events({ move, before, after, userColors }) {
		const events = [];
		const push = (kind, extra = {}) => events.push({ kind, ...extra });
		const ply = move.ply ?? after.moves.length;
		const victim = move.capture ? move.capturedPiece : null;
		const lost = victim ? pieceValues[victim.type] ?? 0 : 0;
		const paid = pieceValues[move.type] ?? 0;
		const promotion = move.type === "PAWN" && /[18]$/.test(move.to ?? "");
		const check = after.status === "ACTIVE" && !!after.checkedColor;

		const mate = ["WHITE_WIN", "BLACK_WIN"].includes(after.status);
		if(mate && ply <= 8) push("quickMate");
		if(check) push(this.lastWasCheck ? "checkAgain" : "check");
		if(promotion) push("promotion");
		if(victim && lost >= 3 && lost - paid >= 2 && userColors.includes(victim.color)) push("blunder", { stare: true });
		if(victim?.type === "QUEEN") {
			if(userColors.includes(move.color) && !userColors.includes(victim.color)) push("queenWon");
			else push("queenLost", { stare: userColors.includes(victim.color) });
		}

		const swingBefore = material(before?.pieces);
		const swingAfter = material(after.pieces);
		if(Math.sign(swingBefore) && Math.sign(swingAfter) && Math.sign(swingBefore) !== Math.sign(swingAfter) &&
				Math.abs(swingBefore) >= 3) push("comeback");

		if(move.type === "KING" && Math.abs(fileOf(move.from) - fileOf(move.to)) === 2 && !this.once(`castle-${move.color}`)) {
			push("castle");
		}
		if(move.type === "PAWN" && move.capture && before?.pieces && !before.pieces.has(move.to)) push("enPassant");

		if(ply === 1) {
			const center = ["e4", "d4", "e5", "d5"].includes(move.to);
			const flank = (move.type === "PAWN" && ["a", "h", "g", "b"].includes(move.from?.[0])) ||
				(move.type === "KNIGHT" && ["a", "h"].includes(move.to?.[0]));
			push(center ? "openingCenter" : flank ? "openingWeird" : "openingOther");
		}

		const previous = after.moves.at(-2);
		if(victim && previous?.capture && previous.to === move.to &&
				pieceValues[previous.capturedPiece?.type] === lost) push("trade");

		if(Math.abs(swingAfter) >= 5 && Math.abs(swingBefore) < 5) push("bigLead");
		if(victim && lost >= 5 && userColors.includes(move.color)) push("capture");
		if(victim && lost === 3 && userColors.includes(move.color)) push("minorWon");

		if(move.type === "QUEEN" && !this.queenMoved.has(move.color)) {
			this.queenMoved.add(move.color);
			if(ply <= 8) push("earlyQueen");
		}
		const kingStep = move.type === "KING" && Math.abs(fileOf(move.from) - fileOf(move.to)) < 2;
		if(kingStep && ply <= 6 && !before?.checkedColor) push("bongcloud");
		else if(kingStep && ply < 30 && !before?.checkedColor) push("kingWalk");
		if(move.type === "BISHOP" && ["b2", "g2", "b7", "g7"].includes(move.to) && !this.once(`fianchetto-${move.to}`)) {
			push("fianchetto");
		}
		if(victim?.type === "BISHOP") push("bishopLost");
		if(move.type === "KNIGHT" && ["a", "h"].includes(move.to?.[0])) push("knightRim");
		if(move.type === "PAWN" && (move.color === "WHITE" ? rankOf(move.to) >= 6 : rankOf(move.to) <= 3) && !promotion) {
			push("pawnRush");
		}

		const fourBack = after.moves.at(-5);
		if(fourBack && fourBack.from === move.from && fourBack.to === move.to) push("repeat");
		if(ply % 20 === 0) push("milestone");
		if(ply >= 80 && !this.once("long")) push("longGame");
		return events;
	}

	once(key) {
		if(this.said.has(key)) return true;
		this.said.add(key);
		return false;
	}

	vars(move, after) {
		const name = (type) => labels[type] ?? "piece";
		const lead = material(after.pieces);
		return {
			from: move.from ?? "",
			to: move.to ?? "",
			pieza: name(move.type),
			victima: move.capturedPiece ? name(move.capturedPiece.type) : "piece",
			bando: shownColor(lead > 0 ? "WHITE" : "BLACK") === "WHITE" ? "White" : "Black",
			ply: String(move.ply ?? after.moves.length),
			moves: String(Math.ceil((move.ply ?? after.moves.length) / 2)),
		};
	}
}
