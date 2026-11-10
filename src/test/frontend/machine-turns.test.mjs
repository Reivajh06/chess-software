import test from "node:test";
import assert from "node:assert/strict";
import { normalizeMachineTurns, normalizeState, stateSignature } from "../../main/resources/web/modules/state.js";

const state = {
  fen: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  status: "ACTIVE",
};
const turn = { pluginName: "Stockfish", moveId: 1, from: "e2", to: "e4", promoteTo: null };
const normalizedTurn = {
  plugin: "Stockfish", color: null, move: { from: "e2", to: "e4", promotion: null }, details: {},
};

test("machine turns trim pluginName and plugin names and preserve the received order", () => {
  assert.deepEqual(normalizeMachineTurns([
    { ...turn, pluginName: " Stockfish ", plugin: "Ignored" },
    { plugin: " Other engine ", from: "e7", to: "e5" },
  ]), [
    normalizedTurn,
    { plugin: "Other engine", color: null, move: { from: "e7", to: "e5", promotion: null }, details: {} },
  ]);
});

test("blank, missing and non-string machine plugin names normalize to null", () => {
  for (const names of [{ pluginName: "" }, { pluginName: " \t " }, { plugin: " " }, {},
    { pluginName: null }, { pluginName: 42 }, { plugin: false }]) {
    assert.deepEqual(normalizeMachineTurns([{ from: "e2", to: "e4", ...names }]), [
      { ...normalizedTurn, plugin: null },
    ]);
  }
});

test("machine turns accept case-insensitive string squares and Tile object squares", () => {
  for (const [from, to] of [
    ["E2", "E4"],
    [{ file: "E", rank: "R2" }, { file: "E", rank: "R4" }],
    [{ file: "File.E", rank: 2 }, { file: { name: "E" }, rank: { number: 4 } }],
    [{ file: { name: "File.E" }, rank: { name: "R2" } }, "e4"],
  ]) {
    assert.deepEqual(normalizeMachineTurns([{ ...turn, from, to }]), [normalizedTurn]);
  }
});

test("machine promotions normalize promoteTo and its aliases to uppercase or null", () => {
  for (const [promotion, expected] of [
    [{ promoteTo: "queen" }, "QUEEN"],
    [{ promoteTo: null }, null],
    [{ promote_to: "rook" }, "ROOK"],
    [{ promotion: "knight" }, "KNIGHT"],
    [{}, null],
  ]) {
    assert.deepEqual(normalizeMachineTurns([{ from: "e7", to: "e8", ...promotion }]), [
      { plugin: null, color: null, move: { from: "e7", to: "e8", promotion: expected }, details: {} },
    ]);
  }
});

test("machine details accept flat maps and maps wrapped once as details.details", () => {
  const details = { depth: "25", reason: " Develop a piece " };
  for (const value of [details, { details }]) {
    assert.deepEqual(normalizeMachineTurns([{ ...turn, details: value }]), [
      { ...normalizedTurn, details: { depth: "25", reason: "Develop a piece" } },
    ]);
  }
});

test("machine details drop empty strings and unsupported non-string values", () => {
  const details = {
    depth: "25", empty: "", blank: " \t ", flag: false, enabled: true, missing: undefined,
    nil: null, object: { text: "ignored" }, array: ["ignored"], nan: NaN, infinite: Infinity,
  };
  for (const value of [details, { details }]) {
    assert.deepEqual(normalizeMachineTurns([{ ...turn, details: value }]), [
      { ...normalizedTurn, details: { depth: "25" } },
    ]);
  }
});

test("machine details retain finite numbers as strings", () => {
  assert.deepEqual(normalizeMachineTurns([{ ...turn, details: { depth: 25, time: 0 } }]), [
    { ...normalizedTurn, details: { depth: "25", time: "0" } },
  ]);
});

test("missing or malformed machine details normalize to an empty map", () => {
  for (const details of [undefined, null, "text", [], false]) {
    assert.deepEqual(normalizeMachineTurns([{ ...turn, details }]), [normalizedTurn]);
  }
});

test("machine turns without two valid squares are dropped without dropping valid neighbours", () => {
  assert.deepEqual(normalizeMachineTurns([
    { to: "e4" }, { from: "e2" }, null, {},
    { from: "i2", to: "e4" }, { from: "e2", to: "e9" },
    { from: { file: "E", rank: "R0" }, to: "e4" }, turn,
  ]), [normalizedTurn]);
});

test("non-array machine turns and an empty list normalize to an empty list", () => {
  for (const value of [undefined, null, {}, { requestMoves: [turn] }, "text", 1, false, []]) {
    assert.deepEqual(normalizeMachineTurns(value), []);
  }
});

test("state normalizes machine turns from requestMoves", () => {
  assert.deepEqual(normalizeState({ ...state, requestMoves: [turn] }).machineTurns, [normalizedTurn]);
});

test("state falls back to machineTurns when requestMoves is absent or null", () => {
  for (const fields of [{}, { requestMoves: null }]) {
    assert.deepEqual(normalizeState({ ...state, ...fields, machineTurns: [turn] }).machineTurns,
      [normalizedTurn]);
  }
});

test("requestMoves wins over machineTurns, including an explicitly empty requestMoves", () => {
  const alternative = { plugin: "Other engine", from: "d2", to: "d4" };
  assert.deepEqual(normalizeState({ ...state, requestMoves: [turn], machineTurns: [alternative] }).machineTurns,
    [normalizedTurn]);
  assert.deepEqual(normalizeState({ ...state, requestMoves: [], machineTurns: [alternative] }).machineTurns, []);
});

test("state exposes an empty machineTurns list when neither source is present", () => {
  assert.deepEqual(normalizeState(state).machineTurns, []);
});

test("state signature changes when only the machine turn count changes", () => {
  const withoutTurns = normalizeState(state);
  const withOneTurn = { ...withoutTurns, machineTurns: [normalizedTurn] };
  const withTwoTurns = { ...withoutTurns, machineTurns: [normalizedTurn, normalizedTurn] };
  assert.notEqual(stateSignature(withoutTurns), stateSignature(withOneTurn));
  assert.notEqual(stateSignature(withOneTurn), stateSignature(withTwoTurns));
});

test("identical normalized states have equal signatures", () => {
  assert.equal(stateSignature(normalizeState({ ...state, requestMoves: [turn] })),
    stateSignature(normalizeState({ ...state, requestMoves: [turn] })));
});

test("the machine turns are read from machineMoves, the name the backend uses, before the other names", async () => {
  const { normalizeState } = await import("../../main/resources/web/modules/state.js");
  const base = { fen: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", status: "ACTIVE" };
  const entry = (name) => ({ pluginName: name, moveId: 1, from: "e2", to: "e4", promoteTo: null, details: { depth: "9" } });

  assert.deepEqual(normalizeState({ ...base, machineMoves: [entry("Stockfish")] }).machineTurns.map((t) => t.plugin), ["Stockfish"]);
  assert.deepEqual(
    normalizeState({ ...base, machineMoves: [entry("A")], requestMoves: [entry("B")], machineTurns: [entry("C")] })
      .machineTurns.map((t) => t.plugin),
    ["A"],
  );
  // the backend sends null for a game without plugins
  assert.deepEqual(normalizeState({ ...base, machineMoves: null }).machineTurns, []);
});

test("a machine turn keeps the colour the server reports and ignores anything else", () => {
  const entry = (extra) => ({ pluginName: "Ollama", from: "e2", to: "e4", ...extra });
  const colors = normalizeMachineTurns([
    entry({ color: "white" }), entry({ turnColor: "BLACK" }), entry({ color: "green" }), entry({}),
  ]).map((turn) => turn.color);
  assert.deepEqual(colors, ["WHITE", "BLACK", null, null]);
});
