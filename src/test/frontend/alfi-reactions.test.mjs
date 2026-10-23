import test from "node:test";
import assert from "node:assert/strict";
import { rivalKind } from "../../main/resources/web/modules/alfi-reactions.js";
import { profileLine } from "../../main/resources/web/modules/alfi-profile.js";
import { alfiLines } from "../../main/resources/web/modules/alfi-lines.js";

const cpuColor = "BLACK";
const playerMove = { color: "WHITE" };
const alfiMove = { color: "BLACK" };
const after = (extra = {}) => ({ pieces: new Map(), checkedColor: null, ...extra });

test("checks are told apart by who is in check", () => {
  assert.equal(rivalKind("check", { move: playerMove, after: after({ checkedColor: "BLACK" }), cpuColor }), "bossChecked");
  assert.equal(rivalKind("checkAgain", { move: alfiMove, after: after({ checkedColor: "WHITE" }), cpuColor }), "bossCheck");
});

test("queens, captures and promotions depend on whose piece it was", () => {
  const queen = (color) => ({ ...playerMove, color: color === "WHITE" ? "BLACK" : "WHITE", capturedPiece: { type: "QUEEN", color } });
  assert.equal(rivalKind("queenLost", { move: queen("BLACK"), after: after(), cpuColor }), "bossQueenLost");
  assert.equal(rivalKind("queenLost", { move: queen("WHITE"), after: after(), cpuColor }), "bossQueenWon");
  assert.equal(rivalKind("capture", { move: playerMove, after: after(), cpuColor }), "bossLostPiece");
  assert.equal(rivalKind("capture", { move: alfiMove, after: after(), cpuColor }), null);
  assert.equal(rivalKind("promotion", { move: playerMove, after: after(), cpuColor }), "bossPromotion");
});

test("a big lead is gloating or worry depending on who leads, and only the player's oddities get taunted", () => {
  const ahead = new Map([["d8", { type: "QUEEN", color: "BLACK" }]]);
  const behind = new Map([["d1", { type: "QUEEN", color: "WHITE" }]]);
  assert.equal(rivalKind("bigLead", { move: alfiMove, after: after({ pieces: ahead }), cpuColor }), "bossAhead");
  assert.equal(rivalKind("bigLead", { move: playerMove, after: after({ pieces: behind }), cpuColor }), "bossBehind");
  assert.equal(rivalKind("knightRim", { move: playerMove, after: after(), cpuColor }), "bossTaunt");
  assert.equal(rivalKind("knightRim", { move: alfiMove, after: after(), cpuColor }), null);
  assert.equal(rivalKind("quickMate", { move: playerMove, after: after(), cpuColor }), null);
});

test("every rival kind has lines", () => {
  for (const kind of ["bossChecked", "bossCheck", "bossBlunder", "bossQueenLost", "bossQueenWon", "bossLostPiece",
    "bossAhead", "bossBehind", "bossComeback", "bossPromotion", "bossTaunt",
    "bossAgain", "bossAgainMany", "bossWaiting", "bossReturn", "bossSleepTalk"]) {
    assert.ok(alfiLines[kind]?.calm?.length, kind);
  }
});

test("profile comments need games played and fill in their details", () => {
  assert.equal(profileLine(null), null);
  assert.equal(profileLine({ games: 0, winsVsAlfi: 0 }), null);
  assert.deepEqual(profileLine({ games: 4, winsVsAlfi: 0 }), { kind: "bossProfileLosses", vars: { partidas: "4" } });
  const seen = new Set();
  for (let index = 0; index < 50; index++) {
    seen.add(profileLine({ games: 9, winsVsAlfi: 2, favoriteFirstMove: "e2-e4", favoritePiece: "KNIGHT" }).kind);
  }
  assert.deepEqual([...seen].sort(), ["bossProfileFirstMove", "bossProfilePiece", "bossProfileWins"]);
});
