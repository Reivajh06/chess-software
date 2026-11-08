import test from "node:test";
import assert from "node:assert/strict";
import { ChessApi } from "../../main/resources/web/modules/api.js";
import { abandonedSession } from "../../main/resources/web/modules/session.js";

const sessionId = "6e26bb12-02ae-4e08-a99f-c0af94d67608";
const player = { sessionId, gamemode: "PVP", playerColor: "WHITE" };

test("leaving a player's session returns its ID", () => {
  assert.equal(abandonedSession(player, null), sessionId);
});

test("a missing session or session ID leaves nothing to abandon", () => {
  for (const current of [null, undefined, {}, { sessionId: "" }, { sessionId: null }, { sessionId: undefined }]) {
    assert.equal(abandonedSession(current), null);
  }
});

test("a watcher never abandons the game", () => {
  assert.equal(abandonedSession({ ...player, watching: true }), null);
});

test("joining the same session leaves nothing to abandon", () => {
  assert.equal(abandonedSession(player, sessionId), null);
});

test("joining a different session abandons the current one", () => {
  assert.equal(abandonedSession(player, "another-session"), sessionId);
});

test("the default next session abandons the current game", () => {
  assert.equal(abandonedSession(player), sessionId);
});

test("abandon sends exactly one DELETE with an encoded ID and keepalive", async (t) => {
  const fetch = t.mock.method(globalThis, "fetch", async () => new Response("", { status: 200 }));
  assert.equal(await new ChessApi().abandon("a b/c"), undefined);
  assert.equal(fetch.mock.callCount(), 1);
  assert.deepEqual(fetch.mock.calls[0].arguments, [
    "/api/sessions/a%20b%2Fc", { method: "DELETE", keepalive: true, cache: "no-store" },
  ]);
});

test("abandon resolves to undefined when the network fails", async (t) => {
  t.mock.method(globalThis, "fetch", async () => { throw new TypeError("offline"); });
  assert.equal(await new ChessApi().abandon(sessionId), undefined);
});

for (const status of [404, 500]) {
  test(`abandon resolves to undefined on a ${status} response`, async (t) => {
    t.mock.method(globalThis, "fetch", async () => new Response("server error", { status }));
    assert.equal(await new ChessApi().abandon(sessionId), undefined);
  });
}

test("cancelling the API immediately after abandon does not cancel the DELETE", async (t) => {
  let finish;
  const fetch = t.mock.method(globalThis, "fetch", () => new Promise(resolve => { finish = resolve; }));
  const api = new ChessApi();
  const pending = api.abandon(sessionId);
  api.cancel();
  assert.equal(fetch.mock.callCount(), 1);
  const [path, options] = fetch.mock.calls[0].arguments;
  assert.equal(path, `/api/sessions/${sessionId}`);
  assert.equal(options.method, "DELETE");
  assert.equal("signal" in options, false);
  finish(new Response("", { status: 200 }));
  assert.equal(await pending, undefined);
});
