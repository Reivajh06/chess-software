import test from "node:test";
import assert from "node:assert/strict";
import {
  accountIdentity, accountTone, newGame, palette, pluginIdentity, reserveTone, toneFor,
} from "../../main/resources/web/modules/identity.js";

test("an unknown participant has the neutral tone", () => {
  newGame();
  assert.equal(toneFor(null), "#8f9bb3");
});

test("a participant gets a palette colour and keeps it within a game", () => {
  newGame();
  const tone = toneFor("guest:ana");
  assert.ok(palette.includes(tone));
  assert.equal(toneFor("guest:ana"), tone);
  toneFor("guest:luis");
  assert.equal(toneFor("guest:ana"), tone);
});

test("participants get distinct colours while the palette has colours left", () => {
  newGame();
  const tones = palette.map((_, index) => toneFor(`guest:${index}`));
  assert.equal(new Set(tones).size, palette.length);
  assert.deepEqual(new Set(tones), new Set(palette));
});

test("a reserved tone fixes the participant's colour and leaves it out of the draw", () => {
  newGame();
  toneFor("account:ana");
  reserveTone("account:ana", palette[0]);
  assert.equal(toneFor("account:ana"), palette[0]);
  const others = palette.slice(1).map((_, index) => toneFor(`guest:${index}`));
  assert.equal(new Set(others).size, palette.length - 1);
  assert.ok(others.every((tone) => tone !== palette[0]));
  assert.equal(toneFor("account:ana"), palette[0]);
});

test("a new game forgets assignments, including reserved tones", (t) => {
  newGame();
  t.mock.method(Math, "random", () => 0);
  reserveTone("guest:returning", palette[0]);
  assert.equal(toneFor("guest:returning"), palette[0]);
  newGame();
  const others = palette.slice(1).map((_, index) => toneFor(`guest:new-${index}`));
  // Drawing the first free colour leaves the last one for the returning participant.
  const fresh = toneFor("guest:returning");
  assert.equal(fresh, palette.at(-1));
  assert.notEqual(fresh, palette[0]);
  assert.deepEqual(new Set([...others, fresh]), new Set(palette));
});

test("plugin identities normalize names and jar suffixes but keep models distinct", () => {
  assert.equal(pluginIdentity("Stockfish"), "plugin:stockfish");
  assert.equal(pluginIdentity("Stockfish"), pluginIdentity("stockfish.jar"));
  assert.notEqual(pluginIdentity("Ollama (qwen3:14b)"), pluginIdentity("Ollama (gemma4)"));
  assert.equal(pluginIdentity(""), null);
  assert.equal(pluginIdentity(null), null);
});

test("account identities include the username and are absent for an empty name", () => {
  assert.equal(accountIdentity("ana"), "account:ana");
  assert.equal(accountIdentity("Ana"), "account:Ana");
  assert.notEqual(accountIdentity("ana"), accountIdentity("luis"));
  assert.equal(accountIdentity(""), null);
  assert.equal(accountIdentity(null), null);
});

test("account tones persist independently and stay fixed across calls and games", (t) => {
  newGame();
  const store = new Map();
  globalThis.localStorage = {
    getItem: (key) => store.get(key) ?? null,
    setItem: (key, value) => store.set(key, String(value)),
  };
  t.after(() => { delete globalThis.localStorage; });
  const ana = accountTone("ana");
  assert.ok(palette.includes(ana));
  assert.deepEqual(JSON.parse(store.get("archchess.account-tones")), { ana });
  assert.equal(accountTone("ana"), ana);

  const luis = accountTone("luis");
  assert.ok(palette.includes(luis));
  assert.deepEqual(JSON.parse(store.get("archchess.account-tones")), { ana, luis });
  newGame();
  assert.equal(accountTone("ana"), ana);
  assert.equal(accountTone("luis"), luis);
  assert.deepEqual(JSON.parse(store.get("archchess.account-tones")), { ana, luis });
});

test("account tones still come from the palette when localStorage throws", (t) => {
  newGame();
  globalThis.localStorage = {
    getItem: () => { throw new Error("Storage unavailable"); },
    setItem: () => { throw new Error("Storage unavailable"); },
  };
  t.after(() => { delete globalThis.localStorage; });
  assert.ok(palette.includes(accountTone("ana")));
  assert.ok(palette.includes(accountTone("luis")));
});
