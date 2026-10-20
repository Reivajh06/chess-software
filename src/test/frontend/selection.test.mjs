import test from "node:test";
import assert from "node:assert/strict";
import { Phase, Selection, boardInteractive } from "../../main/resources/web/modules/selection.js";
import { parseFen, initialFen } from "../../main/resources/web/modules/state.js";

const pieces = parseFen(initialFen);
const white = { pieces, interactive: true };
const active = { status: "ACTIVE", turn: "WHITE", waitingForOpponent: false };

function selectedAt(from, moves) {
  const selection = new Selection();
  const { token } = selection.click(from, white);
  assert.ok(selection.loaded(token, { moves }));
  return selection;
}

test("turn and piece ownership are left to the server", () => {
  const session = { gamemode: "PVP", playerColor: "BLACK" };
  assert.equal(boardInteractive(session, active), true);
  assert.equal(boardInteractive(session, { ...active, turn: "BLACK" }), true);
});

test("nobody moves in finished, waiting, spectated or missing games", () => {
  const session = { gamemode: "PVP", playerColor: "WHITE" };
  assert.equal(boardInteractive(session, { ...active, status: "DRAW" }), false);
  assert.equal(boardInteractive(session, { ...active, waitingForOpponent: true }), false);
  assert.equal(boardInteractive({ gamemode: "AIVAI" }, active), false);
  assert.equal(boardInteractive(null, active), false);
  assert.equal(boardInteractive(session, null), false);
});

test("any piece can be selected while the board is interactive", () => {
  const selection = new Selection();
  assert.equal(selection.click("e4", white).type, "ignore");
  assert.equal(selection.click("e2", { pieces, interactive: false }).type, "ignore");
  assert.equal(selection.phase, Phase.IDLE);
  assert.equal(selection.click("e2", white).type, "load");
  assert.equal(selection.phase, Phase.LOADING);
  assert.equal(selection.selected, "e2");
});

test("only server-provided destinations become targets and produce a move", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }, { moveId: 1, tiles: [{ file: "E", rank: "R4" }] }]);
  assert.deepEqual([...selection.targets].sort(), ["e3", "e4"]);
  assert.deepEqual(selection.click("e4", white), { type: "move", from: "e2", to: "e4", moveId: 1 });
  assert.equal(selection.busy, true);
  assert.equal(selection.targets.size, 0);
});

test("clicking outside the legal map deselects instead of moving", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }]);
  assert.equal(selection.click("e5", white).type, "deselect");
  assert.equal(selection.phase, Phase.IDLE);
  assert.equal(selection.selected, null);
});

test("clicking the selected piece again deselects it", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }]);
  assert.equal(selection.click("e2", white).type, "deselect");
  assert.equal(selection.phase, Phase.IDLE);
});

test("a piece of either color can be selected; the server decides its moves", () => {
  const selection = new Selection();
  assert.equal(selection.click("e7", white).type, "load");
});

test("selecting another own piece discards the previous legal map", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }]);
  const command = selection.click("g1", white);
  assert.equal(command.type, "load");
  assert.equal(selection.targets.size, 0);
  assert.equal(selection.click("e3", white).type, "deselect", "the stale e2 target must not move g1");
});

test("a stale legal-moves response cannot overwrite a newer selection", () => {
  const selection = new Selection();
  const first = selection.click("e2", white);
  const second = selection.click("g1", white);
  assert.equal(selection.loaded(first.token, { moves: [{ moveId: 0, tiles: ["e3"] }] }), false);
  assert.equal(selection.failed(first.token), false);
  assert.ok(selection.loaded(second.token, { moves: [{ moveId: 0, tiles: ["f3"] }] }));
  assert.deepEqual([...selection.targets], ["f3"]);
});

test("a reset invalidates any in-flight request", () => {
  const selection = new Selection();
  const { token } = selection.click("e2", white);
  selection.reset();
  assert.equal(selection.loaded(token, { moves: [] }), false);
  assert.equal(selection.phase, Phase.IDLE);
});

test("a failed request returns to idle", () => {
  const selection = new Selection();
  const { token } = selection.click("e2", white);
  assert.ok(selection.failed(token));
  assert.equal(selection.phase, Phase.IDLE);
});

test("an empty legal map keeps the outline without targets", () => {
  const selection = selectedAt("a1", []);
  assert.equal(selection.phase, Phase.SELECTED);
  assert.equal(selection.selected, "a1");
  assert.equal(selection.targets.size, 0);
});

test("a malformed legal-moves payload is rejected and leaves the request pending", () => {
  const selection = new Selection();
  const { token } = selection.click("e2", white);
  assert.throws(() => selection.loaded(token, { moves: [{ moveId: 1.5, tiles: ["e3"] }] }));
  assert.ok(selection.failed(token));
});

test("several rules on one destination require an explicit choice", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }, { moveId: 3, tiles: ["e3"] }]);
  const choice = selection.click("e3", white);
  assert.equal(choice.type, "choose");
  assert.deepEqual(choice.options.map((option) => option.moveId), [0, 3]);
  assert.equal(selection.click("g1", white).type, "ignore", "the board is locked while choosing");
  assert.equal(selection.choose(7), null, "an unoffered rule cannot be chosen");
  assert.deepEqual(selection.choose(3), { type: "move", from: "e2", to: "e3", moveId: 3 });
});

test("cancelling a choice returns to the selected piece", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }, { moveId: 3, tiles: ["e3"] }]);
  selection.click("e3", white);
  assert.ok(selection.cancelChoice());
  assert.equal(selection.phase, Phase.SELECTED);
  assert.ok(selection.targets.has("e3"));
});

test("a promotion destination asks for the new piece before moving", () => {
  const promotion = parseFen("7k/P7/8/8/8/8/8/K7");
  const selection = new Selection();
  const context = { pieces: promotion, interactive: true };
  const { token } = selection.click("a7", context);
  selection.loaded(token, { moves: [{ moveId: 0, tiles: ["a8"] }] });
  assert.deepEqual(selection.click("a8", context), { type: "promote", from: "a7", to: "a8" });
  assert.equal(selection.phase, Phase.CHOOSING);
  assert.deepEqual(selection.promote("QUEEN"),
    { type: "move", from: "a7", to: "a8", moveId: 0, promotion: "QUEEN" });
  assert.equal(selection.phase, Phase.MOVING);
});

test("the board ignores clicks while a move is in flight", () => {
  const selection = selectedAt("e2", [{ moveId: 0, tiles: ["e3"] }]);
  selection.click("e3", white);
  assert.equal(selection.click("g1", white).type, "ignore");
  assert.equal(selection.click("e2", white).type, "ignore");
});
