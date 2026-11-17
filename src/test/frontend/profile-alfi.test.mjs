import { test } from "node:test";
import assert from "node:assert/strict";
import { colorAt, creditAlfi, playerColor, resultOf, rivalOf } from "../../main/resources/web/modules/profile.js";

// A FEN with only the given pieces ({ e4: "P", e5: "p" }) and the side to move ("w" / "b").
function fen(pieces, side) {
	const rows = [];
	for(let rank = 8; rank >= 1; rank--) {
		let row = "", empty = 0;
		for(const file of "abcdefgh") {
			const piece = pieces[`${file}${rank}`];
			if(!piece) { empty++; continue; }
			if(empty) { row += empty; empty = 0; }
			row += piece;
		}
		rows.push(row + (empty || ""));
	}
	return `${rows.join("/")} ${side} - - 0 1`;
}

// One history move: who sent it, where the piece landed, the piece left there and the side to move afterwards.
const move = (ply, player, to, piece, side) => ({ ply, player, to, from: "a1", piece: "PAWN", fen: fen({ [to]: piece }, side) });
const engine = "Stockfish#node0abc";
const alfiGame = (status, moves) => ({ gamemode: "PVCPU", alfiBoss: true, creator: "me", rival: null, status, moves });
const scored = (game) => {
	const credited = creditAlfi(game, "me");
	const color = playerColor(credited, "me");
	return { color, result: resultOf(credited, color).label, rival: rivalOf(credited, "me"), credited };
};

test("colorAt reads the colour of the piece on a square", () => {
	assert.equal(colorAt("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 2", "d4"), "WHITE");
	assert.equal(colorAt("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 2", "e5"), "BLACK");
	assert.equal(colorAt("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 2", "e4"), null);
});

test("Alfi's opening move does not make the player Black: Alfi winning as Black is a defeat", () => {
	const { color, result, rival } = scored(alfiGame("BLACK_WIN", [
		move(1, engine, "e5", "p", "w"),
		move(2, "me", "d4", "P", "b"),
		move(3, engine, "d4", "p", "w"),
	]));
	assert.equal(color, "WHITE");
	assert.equal(result, "Defeat");
	assert.equal(rival, "Alfi");
});

test("after a seat flip the player is scored with the colour they ended with", () => {
	const moves = [
		move(1, engine, "e5", "p", "w"),
		move(2, "me", "e4", "P", "b"),
		// Flip on Alfi's (Black's) turn: the turn stays Black, now the player's.
		move(3, "me", "c5", "p", "w"),
		move(4, engine, "h5", "Q", "b"),
	];
	const won = scored(alfiGame("WHITE_WIN", moves));
	assert.equal(won.color, "BLACK");
	assert.equal(won.result, "Defeat");
	assert.equal(scored(alfiGame("BLACK_WIN", moves)).result, "Victory");
});

test("a dodge saved under the player's name is credited to Alfi and does not change the player's colour", () => {
	const { color, result, credited } = scored(alfiGame("BLACK_WIN", [
		move(1, engine, "e5", "p", "w"),
		move(2, "me", "d4", "P", "b"),
		move(3, engine, "c6", "n", "w"),
		// Dodge: on White's (the player's) turn, Alfi's black knight runs; the turn stays with White.
		move(4, "me", "a5", "n", "w"),
		move(5, "me", "d5", "P", "b"),
	]));
	assert.deepEqual(credited.moves.map(m => m.player), ["Alfi", "me", "Alfi", "Alfi", "me"]);
	assert.equal(color, "WHITE");
	assert.equal(result, "Defeat");
});

test("machine games without a rival account are named after their mode", () => {
	assert.equal(rivalOf({ gamemode: "PVCPU", creator: "me", rival: null, moves: [] }, "me"), "vs Machine");
	assert.equal(rivalOf({ gamemode: "AIVAI", creator: "me", rival: null, moves: [] }, "me"), "Machine vs Machine");
	assert.equal(rivalOf({ gamemode: "PVP", creator: "me", rival: "marta", moves: [] }, "me"), "marta");
});

test("games that are not Alfi's keep their moves as saved", () => {
	const game = { gamemode: "PVCPU", creator: "me", rival: null, status: "ACTIVE", moves: [move(1, "me", "e4", "P", "b")] };
	assert.equal(creditAlfi(game, "me"), game);
});
