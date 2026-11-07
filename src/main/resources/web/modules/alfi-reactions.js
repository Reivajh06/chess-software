import { materialLead } from "./cheats.js";

export function rivalKind(kind, { move, after, cpuColor }) {
	const byPlayer = move.color !== cpuColor;
	switch(kind) {
		case "check":
		case "checkAgain":
			return after.checkedColor === cpuColor ? "bossChecked" : "bossCheck";
		case "blunder":
			return "bossBlunder";
		case "queenWon":
			return "bossQueenLost";
		case "queenLost":
			return move.capturedPiece?.color === cpuColor ? "bossQueenLost" : "bossQueenWon";
		case "capture":
		case "minorWon":
			return byPlayer ? "bossLostPiece" : null;
		case "bigLead":
			return materialLead(after.pieces, cpuColor) > 0 ? "bossAhead" : "bossBehind";
		case "comeback":
			return "bossComeback";
		case "promotion":
			return byPlayer ? "bossPromotion" : null;
		case "quickMate":
			return null;
		default:
			return byPlayer ? "bossTaunt" : null;
	}
}
