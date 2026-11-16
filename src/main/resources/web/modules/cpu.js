import { normalizeLegalMoves } from "./selection.js";

export async function legalOptions(api, sessionId, pieces, color) {
	const tiles = [...pieces].filter(([, piece]) => piece.color === color).map(([tile]) => tile);
	const answers = [];
	for(const from of tiles) {
		answers.push(await api.legal(sessionId, from).then((payload) => ({ from, payload }), () => null));
	}

	const options = [];
	for(const answer of answers) {
		if(!answer) continue;
		let destinations;
		try {
			destinations = normalizeLegalMoves(answer.payload);
		} catch {
			continue;
		}
		for(const [to, moves] of destinations) {
			for(const { moveId } of moves) options.push({ from: answer.from, to, moveId });
		}
	}
	return options.map((move) => {
		const promotes = pieces.get(move.from)?.type === "PAWN" && /[18]$/.test(move.to);
		return { ...move, promotion: promotes ? "QUEEN" : null };
	});
}
