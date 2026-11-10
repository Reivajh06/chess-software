import test from "node:test";
import assert from "node:assert/strict";
import { enginePriority, engineRank, strongestEngine } from "../../main/resources/web/modules/engine-plugins.js";
import { cpuLevelsFor } from "../../main/resources/web/modules/modes.js";
import { SessionAccess } from "../../main/resources/web/modules/session.js";
import { machineName } from "../../main/resources/web/modules/thinking.js";
import { readFileSync } from "node:fs";
import { runInNewContext } from "node:vm";

test("known engine filenames match case-insensitively, including plugin and version suffixes", () => {
  for(const filename of ["StockFish.JAR", "archon.jar", "BerserkPlugin.jar", "chess-stockfish-17.jar", "lc0.jar"]) {
    assert.ok(engineRank(filename) >= 0, filename);
  }
});

test("unknown names, paths and LLM wrappers cannot qualify even if they mention an engine", () => {
  for(const filename of ["ollama.jar", "Ollama-Stockfish.jar", "stockfish-llm.jar", "qwen-berserk.jar",
    "claude-archon.jar", "gemini-stockfish.jar", "my-stockfisher.jar", "alfi.jar", "unknown.jar",
    "engines/stockfish.jar", "engines\\stockfish.jar", null, {}, 17]) {
    assert.equal(engineRank(filename), -1, String(filename));
  }
});

test("priority is fixed: Stockfish > Berserk > Archon > the other known engines", () => {
  const installed = enginePriority.map((name) => `${name}.jar`).reverse();
  for(const engine of enginePriority) {
    assert.equal(strongestEngine(installed), `${engine}.jar`);
    installed.splice(installed.indexOf(`${engine}.jar`), 1);
  }
  assert.equal(strongestEngine(installed), null);
});

test("selection is deterministic for multiple files of the same engine", () => {
  const files = ["stockfish-z.jar", "Stockfish-a.jar", "berserk.jar"];
  assert.equal(strongestEngine(files), "Stockfish-a.jar");
  assert.equal(strongestEngine(files.reverse()), "Stockfish-a.jar");
});

test("??? is absent without a recognized engine, including an LLM-only installation", () => {
  for(const plugins of [[], ["ollama.jar"], ["alfi.jar", "unknown.jar"], ["ollama-stockfish.jar"]]) {
    const levels = cpuLevelsFor(plugins);
    assert.deepEqual(levels.map((level) => level.id), ["EASY", "MEDIUM", "HARD", "MAX"]);
    assert.ok(!levels.some((level) => level.name === "???"));
  }
});

test("??? appears once alongside the four normal levels when any known engine is installed", () => {
  for(const plugins of [["ARCHON.jar"], ["ollama.jar", "berserk.jar"], ["stockfish.jar", "berserk.jar"]]) {
    const levels = cpuLevelsFor(plugins);
    assert.deepEqual(levels.map((level) => level.id), ["EASY", "MEDIUM", "HARD", "MAX", "ALFI"]);
    assert.equal(levels.filter((level) => level.name === "???").length, 1);
  }
  // Reopening with another list must not keep a previously rendered easter egg.
  assert.equal(cpuLevelsFor([]).length, 4);
});

function apiWith(plugins) {
  const creations = [];
  return {
    creations,
    plugins: async () => plugins,
    create: async (gamemode, difficulty, pluginNames) => {
      creations.push({ gamemode, difficulty, pluginNames });
      return { sessionId: "boss", gamemode, difficulty, playerColor: "BLACK", state: { moves: [] } };
    },
  };
}

test("Alfi sends MAX and exactly the highest-priority installed engine, independently of the selected plugin", async () => {
  const api = apiWith(["ollama.jar", "archon.jar", "berserk.jar", "Stockfish.JAR"]);
  const { session } = await new SessionAccess(api).open(null, "PVCPU", "ALFI", ["ollama.jar"]);
  assert.deepEqual(api.creations, [{ gamemode: "PVCPU", difficulty: "MAX", pluginNames: ["Stockfish.JAR"] }]);
  assert.equal(session.alfiBoss, true);
  assert.equal(session.alfi, "hidden");
  assert.equal(session.difficulty, "MAX");
  assert.deepEqual(session.cpu, { color: "WHITE" });
  assert.deepEqual(session.pluginNames, ["Stockfish.JAR"]);
  assert.equal(machineName(session), "???");
  assert.equal(machineName({ ...session, alfi: "revealed" }), "Alfi");
});

test("Alfi rechecks availability and refuses creation if the engine disappeared", async () => {
  const api = apiWith(["ollama.jar"]);
  await assert.rejects(new SessionAccess(api).open(null, "PVCPU", "ALFI", ["stockfish.jar"]), /No chess engine/);
  assert.equal(api.creations.length, 0);
});

test("HARD stays a normal selected CPU and MAX is a normal highest-priority engine", async () => {
  const api = apiWith(["stockfish.jar", "archon.jar"]);
  for(const difficulty of ["HARD", "MAX"]) {
    const { session } = await new SessionAccess(api).open(null, "PVCPU", difficulty, ["archon.jar"]);
    assert.equal(session.alfiBoss, false);
    assert.equal(session.alfi, null);
    assert.equal(session.difficulty, difficulty);
    assert.deepEqual(session.pluginNames, [difficulty === "MAX" ? "stockfish.jar" : "archon.jar"]);
  }
});

test("saved boss flag and phase survive resume; HARD alone never becomes Alfi", (t) => {
  const saved = new Map();
  const stubs = {
    sessionStorage: { getItem: (key) => saved.get(key) ?? null, setItem: (key, value) => saved.set(key, value) },
    location: { href: "http://localhost/" },
    window: { history: { replaceState() {} } },
  };
  for(const [key, value] of Object.entries(stubs)) {
    const original = Object.getOwnPropertyDescriptor(globalThis, key);
    Object.defineProperty(globalThis, key, { value, configurable: true, writable: true });
    t.after(() => original ? Object.defineProperty(globalThis, key, original) : delete globalThis[key]);
  }
  const sessions = new SessionAccess({});
  const boss = { sessionId: "boss", alfiBoss: true, alfi: "revealed", difficulty: "MAX",
    cpu: { color: "BLACK" }, pluginNames: ["stockfish.jar"] };
  sessions.persist(boss);
  assert.deepEqual(sessions.saved("boss"), boss);
  saved.set("archchess.session.normal", JSON.stringify({ sessionId: "normal", difficulty: "HARD" }));
  assert.equal(sessions.saved("normal").alfiBoss, false);
  saved.set("archchess.session.legacy", JSON.stringify({ sessionId: "legacy", difficulty: "HARD", alfi: "revealed" }));
  assert.equal(sessions.saved("legacy").alfiBoss, true);
});

test("the difficulty dialog renders ??? only with engines and clicking it directly opens the boss session", () => {
  const app = readFileSync(new URL("../../main/resources/web/modules/navigation.js", import.meta.url), "utf8");
  const render = app.slice(app.indexOf("function renderCpuLevels("), app.indexOf('for(const [id, mode] of [["pvp-invite"'));
  const buttons = [];
  const calls = [];
  const context = {
    cpuLevelsFor, cpuPlugins: ["ollama.jar"],
    optionButton: (name, detail, icon, click) => ({ name, detail, icon, click }),
    dialog: () => calls.push("close"),
    byId: () => ({ replaceChildren(...next) { buttons.splice(0, buttons.length, ...next); } }),
    openSession: (...args) => calls.push(args),
  };
  runInNewContext(`${render}\nthis.renderCpuLevels = renderCpuLevels;`, context);
  context.renderCpuLevels(["ollama.jar"]);
  assert.deepEqual(buttons.map((button) => button.name), ["Easy", "Medium", "Hard", "Max"]);
  context.renderCpuLevels(["ollama.jar", "berserk.jar"]);
  assert.equal(buttons.at(-1).name, "???");
  buttons.at(-1).click();
  assert.deepEqual(calls, ["close", [null, "PVCPU", "ALFI", ["ollama.jar"]]]);
  context.renderCpuLevels([]);
  assert.equal(buttons.length, 4);
});
