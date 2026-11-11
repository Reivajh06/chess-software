import { test } from "node:test";
import assert from "node:assert/strict";
import { profileInsights, streak, mergedMode } from "../../main/resources/web/modules/profile-insights.js";

const move = (ply, player, piece, to) => ({ ply, player, piece, from: "a1", to });
const game = (gamemode, tone, rival, moves = []) => ({ gamemode, rival, moves, result: { tone } });

// Newest first, as the history endpoint returns them.
const games = [
	game("PVAI", "loss", "stockfish", [move(1, "stockfish", "PAWN", "e4"), move(2, "me", "KNIGHT", "f6")]),
	game("PRACTICE", "neutral", "You vs yourself", [move(1, "me", "PAWN", "d4")]),
	game("PVP", "win", "marta", [move(1, "me", "PAWN", "e4"), move(2, "marta", "PAWN", "e5"), move(3, "me", "KNIGHT", "f3")]),
	game("PVCPU", "draw", "stockfish", [move(1, "me", "PAWN", "e4")]),
	game("AIVAI", "neutral", "berserk", []),
];

test("results and the win-rate line leave practice and machine-vs-machine games out", () => {
	const insights = profileInsights(games, "me");
	assert.deepEqual(insights.results, { win: 1, loss: 1, draw: 1 });
	assert.equal(insights.scored, 3);
	assert.deepEqual(insights.winRate.map(point => point.rate), [0, 50, 33]);
	assert.deepEqual(insights.form, ["draw", "win", "loss"]);
});

test("favourite first move, most played opponent and average length come from the moves", () => {
	const insights = profileInsights(games, "me");
	assert.deepEqual(insights.favouriteFirstMove, { piece: "PAWN", to: "e4", count: 2 });
	assert.deepEqual(insights.opponent, { name: "stockfish", games: 2, wins: 0 });
	assert.equal(insights.averageMoves, 2);
	assert.equal(insights.pieces[0].piece, "PAWN");
});

test("PvAI and PvCPU count as one machine mode", () => {
	const insights = profileInsights(games, "me");
	assert.equal(mergedMode("PVAI"), "PVCPU");
	const machine = insights.modes.find(entry => entry.mode === "PVCPU");
	assert.deepEqual(machine, { mode: "PVCPU", count: 2, scored: 2, winPercent: 0 });
	assert.equal(insights.modes.find(entry => entry.mode === "PRACTICE").winPercent, null);
});

test("streak reads the run at the end of the form and copes with no games", () => {
	assert.deepEqual(streak(["loss", "win", "win"]), { tone: "win", length: 2 });
	assert.equal(streak([]), null);
	assert.equal(profileInsights([], "me").favouriteFirstMove, null);
});
