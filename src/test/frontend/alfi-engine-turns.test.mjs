import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { runInNewContext } from "node:vm";
import { MoveQueue } from "../../main/resources/web/modules/move-queue.js";

// Exercise the actual app orchestration with a small boundary stub, without a
// browser, audio, or animation timers. No copy of the turn implementation.
const app = readFileSync(new URL("../../main/resources/web/modules/machine-player.js", import.meta.url), "utf8");
const game = readFileSync(new URL("../../main/resources/web/modules/game.js", import.meta.url), "utf8");
const turnFunctions = app.slice(app.indexOf("async function playCpu("), app.indexOf("async function alfiFlip("));
const move = { from: "e7", to: "e5" };
function runtime(overrides = {}) {
  const calls = [];
  const machine = { color: "BLACK" };
  const context = {
    sessionVersion: 1, cpuFailures: 0, silentMachineFailures: 4,
    store: { session: { alfiBoss: true, cpu: machine }, cpuBusy: false,
      state: { status: "ACTIVE", turn: "BLACK", pieces: new Map() } },
    machineToMove: () => machine,
    alfiFlip: async () => false,
    wantsCheat: () => false,
    alfi: { cheat: () => { calls.push("cheat"); return true; } },
    thinkingLog: { start() { calls.push("log-start"); }, end() { calls.push("log-end"); } },
    machineName: () => "hidden engine",
    submitMachineMove: async () => { calls.push("engine"); return move; },
    api: { state: async () => { calls.push("state"); return {}; } },
    applyGameState() {},
    legalOptions: async () => { calls.push("legal"); return [{ from: "d7", to: "d5", moveId: 1 }]; },
    submitMove: async () => { calls.push("fallback"); return true; },
    renderGame() {}, scheduleCpu() {}, schedulePoll() {}, wait: async () => {},
    ...overrides,
  };
  context.version = () => context.sessionVersion;
  runInNewContext(`${turnFunctions}\nthis.playCpu = playCpu; this.playEngineMove = playEngineMove;`, context);
  return { context, calls, machine };
}

test("Alfi uses the engine and never logs its identity/details or reads random legal moves on success", async () => {
  const { context, calls, machine } = runtime();
  await context.playCpu(1, machine);
  assert.deepEqual(calls, ["engine"]);
  assert.equal(context.store.cpuBusy, false);
});

test("Alfi only uses fresh random legal moves after four failed engine attempts and a state reread", async () => {
  const { context, calls, machine } = runtime();
  context.submitMachineMove = async () => { calls.push("engine-failed"); return null; };
  for(let attempt = 1; attempt <= 3; attempt++) {
    await context.playEngineMove(1, machine);
    assert.equal(calls.includes("legal"), false);
  }
  await context.playEngineMove(1, machine);
  assert.deepEqual(calls.slice(-4), ["engine-failed", "state", "legal", "fallback"]);
  assert.equal(context.cpuFailures, 0);
  await context.playEngineMove(1, machine);
  assert.equal(calls.at(-2), "engine-failed"); // the next turn tries the engine again
});

test("fallback never moves on the human's turn after a failed reply that actually advanced the board", async () => {
  const { context, calls, machine } = runtime({ cpuFailures: 3 });
  context.submitMachineMove = async () => null;
  context.applyGameState = () => { context.store.state.turn = "WHITE"; };
  await context.playEngineMove(1, machine);
  assert.deepEqual(calls, ["state"]);
});

test("fallback waits for reliable state and does not use stale moves after session replacement", async () => {
  for(const failure of ["offline", "new-session"]) {
    const { context, calls, machine } = runtime({ cpuFailures: 3 });
    context.submitMachineMove = async () => null;
    context.api.state = async () => {
      if(failure === "offline") throw new Error("offline");
      context.sessionVersion++;
      return {};
    };
    await context.playEngineMove(1, machine);
    assert.deepEqual(calls, []);
  }
});

test("normal CPU failures never activate Alfi's fallback", async () => {
  const { context, calls, machine } = runtime({ cpuFailures: 20 });
  context.store.session.alfiBoss = false;
  context.submitMachineMove = async () => null;
  await context.playEngineMove(1, machine);
  assert.deepEqual(calls, []);
});

test("double moves request the engine again only when the server leaves Alfi the turn", async () => {
  for(const retainedTurn of [false, true]) {
    const { context, calls, machine } = runtime({ wantsCheat: () => true });
    context.submitMachineMove = async () => {
      calls.push("engine");
      context.store.state.turn = retainedTurn ? "BLACK" : "WHITE";
      return move;
    };
    await context.playCpu(1, machine);
    assert.deepEqual(calls, retainedTurn ? ["engine", "cheat", "engine"] : ["engine"]);
  }
});

test("a duplicate CPU callback cannot start a second request while the first is running", async () => {
  const { context, calls, machine } = runtime();
  let finish;
  context.submitMachineMove = () => new Promise((resolve) => { calls.push("engine"); finish = () => resolve(move); });
  const first = context.playCpu(1, machine);
  await Promise.resolve();
  await context.playCpu(1, machine);
  finish();
  await first;
  assert.deepEqual(calls, ["engine"]);
});

test("the mutation queue serializes engine requests, state reads and teleports", async () => {
  const queue = new MoveQueue();
  const calls = [];
  let finishEngine, finishState;
  const engine = queue.run(async () => {
    calls.push("engine-start");
    await new Promise((resolve) => { finishEngine = resolve; });
    calls.push("state-start");
    await new Promise((resolve) => { finishState = resolve; });
    calls.push("engine-complete");
  });
  const teleport = queue.run(() => { calls.push("teleport"); });
  const second = queue.run(() => { calls.push("engine-again"); });
  await Promise.resolve();
  assert.deepEqual(calls, ["engine-start"]);
  finishEngine();
  await Promise.resolve();
  assert.deepEqual(calls, ["engine-start", "state-start"]);
  finishState();
  await Promise.all([engine, teleport, second]);
  assert.deepEqual(calls, ["engine-start", "state-start", "engine-complete", "teleport", "engine-again"]);
});

test("a failed mutation does not block the next queued action", async () => {
  const queue = new MoveQueue();
  const failed = queue.run(() => { throw new Error("failed"); });
  const next = queue.run(() => "played");
  await assert.rejects(failed, /failed/);
  assert.equal(await next, "played");
});

test("Alfi discards all returned plugin details and identity, including during the hidden phase", async () => {
  const submit = app.slice(app.indexOf("async function submitMachineMove("), app.indexOf("function reset("));
  for(const alfiBoss of [true, false]) {
    const context = {
      sessionVersion: 1, cpuFailures: 0, silentMachineFailures: 4,
      machineDetails: {}, machinePlugin: null,
      store: { session: { sessionId: "boss", alfiBoss, alfi: alfiBoss ? "hidden" : null } },
      sendMove: (request) => request(),
      api: { machineMove: async () => ({ move, state: {}, plugin: "Stockfish", details: { depth: "25", reason: "engine secret" } }) },
    };
    context.version = () => context.sessionVersion;
    runInNewContext(`${submit}\nthis.submitMachineMove = submitMachineMove;`, context);
    assert.deepEqual(await context.submitMachineMove(), move);
    assert.equal(context.machinePlugin, alfiBoss ? null : "Stockfish");
    assert.equal(Object.keys(context.machineDetails).length, alfiBoss ? 0 : 2);
  }
});

test("a queued move for a replaced session is dropped before it touches the board", async () => {
  const send = game.slice(game.indexOf("function sendMove("), game.indexOf("async function performMove("));
  const queue = new MoveQueue();
  let release;
  const first = queue.run(() => new Promise((resolve) => { release = resolve; }));
  const calls = [];
  const context = { sessionVersion: 1, moveQueue: queue, performMove: (request) => request() };
  runInNewContext(`${send}\nthis.sendMove = sendMove;`, context);
  const pending = context.sendMove(() => { calls.push("old-board-mutation"); });
  await Promise.resolve();
  context.sessionVersion++;
  release();
  await first;
  assert.equal(await pending, false);
  assert.deepEqual(calls, []);
});
