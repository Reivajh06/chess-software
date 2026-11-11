import test from "node:test";
import assert from "node:assert/strict";
import {
  baseName, describeMove, detailRows, displayName, elapsedLabel, initials, machineName, modelName, pluginTone, subName,
} from "../../main/resources/web/modules/thinking.js";
import { newGame, palette } from "../../main/resources/web/modules/identity.js";

test("a machine move is described with its squares and promotion", () => {
  assert.equal(describeMove({ from: "e2", to: "e4", promotion: null }), "e2 → e4");
  assert.equal(describeMove({ from: "a7", to: "a8", promotion: "QUEEN" }), "a7 → a8 = Q");
});

test("the elapsed time is shown in seconds with one decimal", () => {
  assert.equal(elapsedLabel(0), "0.0 s");
  assert.equal(elapsedLabel(3456), "3.5 s");
  assert.equal(elapsedLabel(-20), "0.0 s");
});

test("the plugin is known from the start only against a machine", () => {
  assert.equal(machineName({ cpu: { color: "BLACK" }, pluginNames: ["stockfish.jar"] }), "stockfish.jar");
  assert.equal(machineName({ machines: [{ color: "WHITE" }, { color: "BLACK" }], pluginNames: ["a.jar", "b.jar"] }), null);
  assert.equal(machineName({}), null);
  assert.equal(machineName(undefined), null);
});

test("plugin names are split into the name and the model, and get initials", () => {
  assert.equal(baseName("Ollama (qwen3:14b)"), "Ollama");
  assert.equal(modelName("Ollama (qwen3:14b)"), "qwen3:14b");
  assert.equal(baseName("stockfish.jar"), "stockfish");
  assert.equal(modelName("stockfish.jar"), "");
  assert.equal(initials("Stockfish"), "ST");
  assert.equal(initials("Ollama (qwen3:14b)"), "QW");
  assert.equal(initials(null), "··");
});

test("the model is the display name and the plugin is its sub-name", () => {
  assert.equal(displayName("Ollama (qwen3:14b)"), "qwen3:14b");
  assert.equal(subName("Ollama (qwen3:14b)"), "Ollama");
  assert.equal(displayName("Stockfish"), "Stockfish");
  assert.equal(subName("Stockfish"), "");
  assert.equal(displayName("stockfish.jar"), "stockfish");
  assert.equal(subName("stockfish.jar"), "");
  assert.equal(displayName(null), "");
  assert.equal(subName(null), "");
});

test("every plugin and model keeps its own palette colour within a game", () => {
  newGame();
  const stockfish = pluginTone("Stockfish");
  assert.equal(pluginTone("Stockfish"), stockfish);
  assert.equal(pluginTone("stockfish.jar"), stockfish);
  const qwen = pluginTone("Ollama (qwen3:14b)");
  const gemma = pluginTone("Ollama (gemma4)");
  assert.equal(pluginTone("Ollama (qwen3:14b)"), qwen);
  assert.equal(pluginTone("Ollama (gemma4)"), gemma);
  assert.equal(new Set([stockfish, qwen, gemma]).size, 3);
  for (const tone of [stockfish, qwen, gemma, pluginTone("Berserk")]) {
    assert.ok(palette.includes(tone));
  }
  assert.equal(pluginTone(null), "#8f9bb3");
});

test("plugin details become labelled rows, with the long texts last and optional", () => {
  const details = { thinking: "Primero e4...", depth: "25", nodes: "1174167", reason: "Controla el centro.", time: "1000", custom_key: "x" };
  const rows = detailRows(details);
  assert.deepEqual(rows.map((row) => row.key), ["depth", "nodes", "reason", "time", "custom_key", "thinking"]);
  assert.equal(rows[0].label, "Depth");
  assert.equal(rows[1].value, Number(1174167).toLocaleString("en-GB"));
  assert.equal(rows[3].value, "1000 ms");
  assert.equal(rows[4].label, "Custom key");
  assert.equal(rows.at(-1).long, true);
  // Against a person the thinking is hidden, the rest stays.
  assert.deepEqual(detailRows(details, { thinking: false }).map((row) => row.key), ["depth", "nodes", "reason", "time", "custom_key"]);
  assert.deepEqual(detailRows({}), []);
  assert.deepEqual(detailRows(undefined), []);
});
