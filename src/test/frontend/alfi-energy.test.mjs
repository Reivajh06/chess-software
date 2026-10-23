import test from "node:test";
import assert from "node:assert/strict";
import { energyLevel, fullEnergy } from "../../main/resources/web/modules/alfi-boss.js";

test("Alfi's energy goes from fresh to tired, exhausted and asleep", () => {
  assert.equal(fullEnergy, 100);
  assert.equal(energyLevel(100).level, "fresh");
  assert.equal(energyLevel(60).level, "fresh");
  assert.equal(energyLevel(59).level, "tired");
  assert.equal(energyLevel(30).level, "tired");
  assert.equal(energyLevel(29).level, "exhausted");
  assert.equal(energyLevel(1).level, "exhausted");
  assert.equal(energyLevel(0).level, "asleep");
});

test("each level shows its own face, and only the lower ones make Alfi comment", () => {
  assert.deepEqual(["fresh", "tired", "exhausted", "asleep"].map((level) =>
    energyLevel({ fresh: 100, tired: 45, exhausted: 10, asleep: 0 }[level]).mood), ["normal", "cansado", "agotado", "durmiendo"]);
  assert.equal(energyLevel(100).line, undefined);
  assert.equal(energyLevel(0).line, "bossAsleep");
});
