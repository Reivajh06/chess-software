import test from "node:test";
import assert from "node:assert/strict";
import { Commentator } from "../../main/resources/web/modules/commentary.js";
import { line } from "../../main/resources/web/modules/mascot.js";
import { alfiLines, themeLines } from "../../main/resources/web/modules/alfi-lines.js";

const board = (entries) => new Map(Object.entries(entries).map(([tile, piece]) => [tile, piece]));
const W = (type) => ({ type, color: "WHITE" });
const B = (type) => ({ type, color: "BLACK" });
const both = ["WHITE", "BLACK"];

function kinds(commentator, move, before, after, userColors = both) {
  return commentator.events({ move, before, after: { status: "ACTIVE", moves: [move], ...after }, userColors })
    .map((event) => event.kind);
}

test("the first move gets an opening remark by type", () => {
  const c = new Commentator();
  assert.ok(kinds(c, { ply: 1, type: "PAWN", color: "WHITE", from: "e2", to: "e4" }, {}, {}).includes("openingCenter"));
  assert.ok(kinds(c, { ply: 1, type: "PAWN", color: "WHITE", from: "h2", to: "h4" }, {}, {}).includes("openingWeird"));
});

test("castling is noticed once per side", () => {
  const c = new Commentator();
  const move = { ply: 9, type: "KING", color: "WHITE", from: "e1", to: "g1" };
  assert.ok(kinds(c, move, {}, {}).includes("castle"));
  assert.ok(!kinds(c, move, {}, {}).includes("castle"));
});

test("en passant is a pawn capture onto an empty square", () => {
  const c = new Commentator();
  const move = { ply: 9, type: "PAWN", color: "WHITE", from: "e5", to: "d6", capture: true, capturedPiece: B("PAWN") };
  assert.ok(kinds(c, move, { pieces: board({ e5: W("PAWN"), d5: B("PAWN") }) }, {}).includes("enPassant"));
});

test("a queen lost to a pawn is a blunder Alfi stares at", () => {
  const c = new Commentator();
  const move = { ply: 12, type: "PAWN", color: "BLACK", from: "e5", to: "d4", capture: true, capturedPiece: W("QUEEN") };
  const events = c.events({ move, before: {}, after: { status: "ACTIVE", moves: [move] }, userColors: ["WHITE"] });
  assert.equal(events[0].kind, "blunder");
  assert.equal(events[0].stare, true);
  assert.ok(events.some((event) => event.kind === "queenLost"));
});

test("an equal recapture on the same square is a trade", () => {
  const c = new Commentator();
  const first = { ply: 10, type: "KNIGHT", color: "WHITE", to: "d5", capture: true, capturedPiece: B("KNIGHT") };
  const second = { ply: 11, type: "PAWN", color: "BLACK", from: "c6", to: "d5", capture: true, capturedPiece: W("KNIGHT") };
  const events = c.events({ move: second, before: {}, after: { status: "ACTIVE", moves: [first, second] }, userColors: both });
  assert.ok(events.some((event) => event.kind === "trade"));
});

test("minor remarks keep quiet for a few plies after Alfi speaks", () => {
  const c = new Commentator();
  const random = Math.random;
  Math.random = () => 0;
  try {
    const rim = (ply) => ({ ply, type: "KNIGHT", color: "WHITE", from: "g1", to: "h3" });
    assert.ok(c.react({ move: rim(5), before: {}, after: { status: "ACTIVE", moves: [] }, userColors: both }));
    assert.equal(c.react({ move: rim(6), before: {}, after: { status: "ACTIVE", moves: [] }, userColors: both }), null);
  } finally {
    Math.random = random;
  }
});

test("king steps in the first moves are a Bongcloud, later just a walk", () => {
  const c = new Commentator();
  assert.ok(kinds(c, { ply: 3, type: "KING", color: "WHITE", from: "e1", to: "e2" }, {}, {}).includes("bongcloud"));
  assert.ok(kinds(c, { ply: 15, type: "KING", color: "WHITE", from: "e1", to: "e2" }, {}, {}).includes("kingWalk"));
});

test("Alfi notices fianchettos once and mourns captured bishops", () => {
  const c = new Commentator();
  const fianchetto = { ply: 6, type: "BISHOP", color: "WHITE", from: "f1", to: "g2" };
  assert.ok(kinds(c, fianchetto, {}, {}).includes("fianchetto"));
  assert.ok(!kinds(c, fianchetto, {}, {}).includes("fianchetto"));
  const capture = { ply: 9, type: "KNIGHT", color: "BLACK", to: "g2", capture: true, capturedPiece: W("BISHOP") };
  assert.ok(kinds(c, capture, {}, {}).includes("bishopLost"));
});

test("every detected event has lines and placeholders get filled", () => {
  const c = new Commentator();
  const source = c.events.toString() + c.react.toString();
  for (const [, kind] of source.matchAll(/push\("(\w+)"/g)) assert.ok(alfiLines[kind], kind);
  for (const kind of Object.keys(alfiLines)) {
    for (let i = 0; i < 20; i++) {
      const text = line(kind, { to: "h3", from: "g1", pieza: "knight", victima: "bishop", bando: "White", ply: "4", moves: "10" });
      assert.ok(text && !/\{\w+\}/.test(text), `${kind}: ${text}`);
    }
  }
});

test("theme lines only cover existing kinds and fill their placeholders", () => {
  for (const [theme, kinds] of Object.entries(themeLines)) {
    for (const kind of Object.keys(kinds)) {
      assert.ok(alfiLines[kind], `${theme}/${kind}`);
      for (let i = 0; i < 20; i++) {
        const text = line(kind, { to: "h3", victima: "bishop" }, theme);
        assert.ok(text && !/\{\w+\}/.test(text), `${theme}/${kind}: ${text}`);
      }
    }
  }
});
