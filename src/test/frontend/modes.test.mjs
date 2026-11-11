import test from "node:test";
import assert from "node:assert/strict";
import { gameModes, modeLabel, modeIcon, menuOrder } from "../../main/resources/web/modules/modes.js";
import { TitleScreen } from "../../main/resources/web/modules/interface.js";
import { SessionAccess } from "../../main/resources/web/modules/session.js";

test("old and new human-machine games share a label and icon", () => {
  for(const mode of ["PVAI", "PVCPU"]) {
    assert.equal(modeLabel(mode), "vs Machine");
    assert.equal(modeIcon(mode), "mode-pvcpu");
  }
  assert.equal(modeLabel("AIVAI"), "Machine vs Machine");
});

test("the menu has five entries with Join a game still last", () => {
  assert.deepEqual(menuOrder, ["PVP", "PRACTICE", "PVCPU", "AIVAI"]);
  const entries = TitleScreen.prototype.entries.call({});
  assert.deepEqual(entries.map((entry) => entry.label), [
    "Play a person", "Practice", "Play a machine", "Machine vs machine", "Join a game",
  ]);
  for(const mode of ["PVCPU", "AIVAI"]) {
    const hint = gameModes[mode].hint;
    assert.equal(hint, hint.toLowerCase());
    assert.ok(hint.length < 90);
    assert.doesNotMatch(hint, /skeleton|undertale|chess|opening|check|pawn/);
  }
});

test("rematches of old PVAI games create PVCPU; two machines still create AIVAI", async () => {
  const calls = [];
  const api = {
    create: async (gamemode, difficulty, pluginNames) => {
      calls.push({ gamemode, difficulty, pluginNames });
      return { sessionId: "machine", gamemode, difficulty, playerColor: "WHITE", state: {} };
    },
  };
  const sessions = new SessionAccess(api);
  const { session } = await sessions.open(null, "PVAI", "HARD", ["ollama.jar"]);
  assert.equal(session.cpu.color, "BLACK");
  await sessions.open(null, "AIVAI", undefined, ["first.jar", "second.jar"]);
  assert.deepEqual(calls, [
    { gamemode: "PVCPU", difficulty: "HARD", pluginNames: ["ollama.jar"] },
    { gamemode: "AIVAI", difficulty: "MAX", pluginNames: ["first.jar", "second.jar"] },
  ]);
});
