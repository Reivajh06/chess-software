// Profile insights computed from the history the profile already loads (no dates, no extra requests).
// `games` come newest first, each with { gamemode, moves, rival, result: { tone } }.

const outcomes = ["win", "loss", "draw"];
const unscoredModes = ["PRACTICE", "AIVAI"];

export const mergedMode = (mode) => (mode === "PVAI" ? "PVCPU" : mode);

function tally(values) {
	const counts = new Map();
	for(const value of values) counts.set(value, (counts.get(value) ?? 0) + 1);
	return [...counts].sort((a, b) => b[1] - a[1]);
}

export function profileInsights(games, username) {
	const oldestFirst = [...games].reverse();
	const scored = oldestFirst.filter(game =>
		!unscoredModes.includes(game.gamemode) && outcomes.includes(game.result?.tone));
	const results = Object.fromEntries(outcomes.map(tone => [tone, scored.filter(game => game.result.tone === tone).length]));

	let wins = 0;
	const winRate = scored.map((game, index) => {
		if(game.result.tone === "win") wins++;
		return { game: index + 1, rate: Math.round((wins / (index + 1)) * 100), rival: game.rival, tone: game.result.tone };
	});

	const played = games.filter(game => game.moves.length > 0);
	const averageMoves = played.length
		? Math.round(played.reduce((sum, game) => sum + game.moves.length, 0) / played.length) : 0;

	const mine = (game) => game.moves.filter(move => move.player === username);
	const openings = tally(games.map(game => mine(game)[0]).filter(Boolean).map(move => `${move.piece} ${move.to}`));
	const favouriteFirstMove = openings.length
		? { piece: openings[0][0].split(" ")[0], to: openings[0][0].split(" ")[1], count: openings[0][1] } : null;

	const rivals = tally(scored.map(game => game.rival));
	const opponent = rivals.length ? {
		name: rivals[0][0],
		games: rivals[0][1],
		wins: scored.filter(game => game.rival === rivals[0][0] && game.result.tone === "win").length,
	} : null;

	const modes = tally(games.map(game => mergedMode(game.gamemode))).map(([mode, count]) => {
		const inMode = scored.filter(game => mergedMode(game.gamemode) === mode);
		const modeWins = inMode.filter(game => game.result.tone === "win").length;
		return { mode, count, scored: inMode.length, winPercent: inMode.length ? Math.round((modeWins / inMode.length) * 100) : null };
	});

	const pieces = tally(games.flatMap(game => mine(game).map(move => move.piece)))
		.map(([piece, count]) => ({ piece, count }));

	return {
		games: games.length,
		scored: scored.length,
		results,
		winRate,
		form: scored.slice(-15).map(game => game.result.tone),
		averageMoves,
		favouriteFirstMove,
		opponent,
		modes,
		pieces,
	};
}

// Current run of identical results at the end of the recent form, e.g. { tone: "win", length: 3 }.
export function streak(form) {
	if(!form.length) return null;
	const tone = form.at(-1);
	let length = 0;
	for(let index = form.length - 1; index >= 0 && form[index] === tone; index--) length++;
	return { tone, length };
}
