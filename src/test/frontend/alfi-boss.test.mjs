import test from "node:test";
import assert from "node:assert/strict";
import { AlfiBoss } from "../../main/resources/web/modules/alfi-boss.js";
import { alfiTrack, alfiTracks } from "../../main/resources/web/modules/alfi-theme.js";

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

test("the full boss soundtrack follows game progress, checks and pauses", () => {
  const played = [];
  const boss = Object.assign(Object.create(AlfiBoss.prototype), {
    music: { play: (track) => played.push(track) },
  });
  Object.defineProperty(boss, "active", { value: true });
  const state = { pieces: new Map(Array.from({ length: 32 }, (_, index) => [index, {}])), moves: [], checkedColor: null };
  boss.followGame(state);
  state.moves.length = 20;
  boss.followGame(state);
  state.pieces.clear();
  boss.followGame(state);
  state.checkedColor = "WHITE";
  boss.followGame(state);
  boss.followGame(state, true);
  assert.deepEqual(played, ["fresh", "tired", "exhausted", "check", "asleep"].map(alfiTrack));
  assert.equal(new Set(played).size, alfiTracks.length);
});
