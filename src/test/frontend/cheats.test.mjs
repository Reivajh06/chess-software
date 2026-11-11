import test from "node:test";
import assert from "node:assert/strict";
import { freeNeighbours, materialLead, neighbours, teleport } from "../../main/resources/web/modules/cheats.js";

const piece = (type, color) => ({ type, color });

test("a teleport uses the first moveset index, which /moves applies to any empty square", () => {
  assert.deepEqual(teleport("d5", "e6"), { from: "d5", to: "e6", moveId: 0, promotion: null });
});

test("neighbours stay on the board and free ones skip occupied squares", () => {
  assert.deepEqual(neighbours("a1").sort(), ["a2", "b1", "b2"]);
  assert.equal(neighbours("e4").length, 8);
  const pieces = new Map([["d5", piece("PAWN", "WHITE")], ["e5", piece("KNIGHT", "BLACK")]]);
  assert.deepEqual(freeNeighbours(pieces, "e4").sort(), ["d3", "d4", "e3", "f3", "f4", "f5"]);
});

test("material lead counts one side against the other, kings excluded", () => {
  const pieces = new Map([
    ["e1", piece("KING", "WHITE")], ["d1", piece("QUEEN", "WHITE")], ["a1", piece("ROOK", "WHITE")],
    ["e8", piece("KING", "BLACK")], ["a8", piece("ROOK", "BLACK")], ["b7", piece("PAWN", "BLACK")],
  ]);
  assert.equal(materialLead(pieces, "WHITE"), 8);
  assert.equal(materialLead(pieces, "BLACK"), -8);
});
