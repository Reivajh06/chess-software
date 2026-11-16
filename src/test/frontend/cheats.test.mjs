import test from "node:test";
import assert from "node:assert/strict";
import { materialLead } from "../../main/resources/web/modules/cheats.js";

const piece = (type, color) => ({ type, color });

test("material lead counts one side against the other, kings excluded", () => {
  const pieces = new Map([
    ["e1", piece("KING", "WHITE")], ["d1", piece("QUEEN", "WHITE")], ["a1", piece("ROOK", "WHITE")],
    ["e8", piece("KING", "BLACK")], ["a8", piece("ROOK", "BLACK")], ["b7", piece("PAWN", "BLACK")],
  ]);
  assert.equal(materialLead(pieces, "WHITE"), 8);
  assert.equal(materialLead(pieces, "BLACK"), -8);
});
