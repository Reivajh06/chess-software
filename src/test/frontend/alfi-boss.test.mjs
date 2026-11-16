import test from "node:test";
import assert from "node:assert/strict";
import { AlfiBoss } from "../../main/resources/web/modules/alfi-boss.js";
import { alfiTrack } from "../../main/resources/web/modules/alfi-theme.js";

test("every boss cheat stays available after repeated uses without stamina", (t) => {
  const original = Object.getOwnPropertyDescriptor(globalThis, "document");
  const effects = [];
  const frame = { classList: { remove() {}, add: (name) => effects.push(name) }, offsetWidth: 1 };
  Object.defineProperty(globalThis, "document", { configurable: true, value: { querySelector: () => frame } });
  t.after(() => original ? Object.defineProperty(globalThis, "document", original) : delete globalThis.document);
  const spoken = [];
  const boss = Object.assign(Object.create(AlfiBoss.prototype), {
    audio: { play() {} }, seat: { speak: (text) => { spoken.push(text); return Promise.resolve(); } },
  });
  for(let round = 0; round < 10; round++) {
    for(const kind of ["double", "dodge", "rewind", "flip"]) boss.cheat(kind);
  }
  assert.equal(spoken.length, 40);
  assert.equal(effects.length, 40);
  assert.ok(spoken.every((text) => typeof text === "string" && text.length));
});

test("the boss match has a single theme and never switches tracks", () => {
  assert.match(alfiTrack.url, /alfi-mal-movimiento\.mp3$/);
  assert.equal(typeof AlfiBoss.prototype.followGame, "undefined");
});
