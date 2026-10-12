import test from "node:test";
import assert from "node:assert/strict";
import { ChessApi, ApiError } from "../../main/resources/web/modules/api.js";
import { SessionAccess } from "../../main/resources/web/modules/session.js";
import { GameStore } from "../../main/resources/web/modules/state.js";

const playerId = "f3435c46-1a64-40c5-9b03-4b0bb3390020";
const state = {
  status: "ACTIVE", turn: "WHITE", checkedColor: null, waitingForOpponent: true,
  fen: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", moves: [],
};
const created = {
  sessionId: "6e26bb12-02ae-4e08-a99f-c0af94d67608", gamemode: "PRACTICE",
  difficulty: "PRACTICE", whiteId: playerId, blackId: playerId, state,
};

test("accepts the backend's text/plain JSON creation response and shared practice ID", async (t) => {
  const calls = [];
  t.mock.method(globalThis, "fetch", async (path, options) => {
    calls.push({ path, options });
    return new Response(JSON.stringify(created), {
      status: 201, headers: { "Content-Type": "text/plain" },
    });
  });
  const api = new ChessApi();
  const { session, state: initial } = await new SessionAccess(api).open(null, "PRACTICE");
  assert.equal(session.playerId, playerId);
  assert.equal(api.playerId, playerId);
  assert.equal(calls.length, 1);
  assert.deepEqual(JSON.parse(calls[0].options.body), { gamemode: "PRACTICE", difficulty: "PRACTICE" });
  const store = new GameStore();
  store.session = session;
  store.apply(initial);
  assert.equal(store.pieces.size, 32);
  assert.equal(store.interactive, true);
});

test("selection and moves send only the acting player ID, without token headers or color", async (t) => {
  const calls = [];
  t.mock.method(globalThis, "fetch", async (path, options) => {
    calls.push({ path, options });
    return new Response('{"moves":[]}', { status: 200 });
  });
  const api = new ChessApi();
  api.playerId = playerId;
  await api.legal(created.sessionId, "e2");
  await api.move(created.sessionId, "e2", "e4");
  assert.equal(calls[0].options.method, "POST");
  assert.ok(calls[0].path.endsWith("/legal-moves"));
  assert.deepEqual(JSON.parse(calls[0].options.body), { playerId, from: "e2" });
  assert.deepEqual(JSON.parse(calls[1].options.body), { playerId, from: "e2", to: "e4" });
  assert.equal(calls[1].options.headers["X-Player-Token"], undefined);
});

test("does not accept a 501 response even if it contains valid session JSON", async (t) => {
  t.mock.method(globalThis, "fetch", async () => new Response(JSON.stringify(created), { status: 501 }));
  await assert.rejects(new ChessApi().create("PVP", "NOT_CPU"),
    error => error instanceof ApiError && error.status === 501);
});

test("rejects malformed successful responses with the correct diagnostic", async (t) => {
  t.mock.method(globalThis, "fetch", async () => new Response("<html>not JSON</html>", { status: 201 }));
  await assert.rejects(new ChessApi().create("PRACTICE", "PRACTICE"), /Respuesta del servidor no válida/);
});

test("PvP creation registers exactly one randomly assigned ID and retains the other for invitations", async () => {
  const blackId = "68eab6b1-974f-4e83-9c16-1ad28bd98c87";
  const registrations = [];
  const api = {
    create: async () => ({ ...created, gamemode: "PVP", blackId }),
    join: async (id, actor) => { registrations.push([id, actor]); return { state }; },
  };
  const { session } = await new SessionAccess(api).open(null, "PVP");
  assert.deepEqual(registrations, [[created.sessionId, session.playerId]]);
  assert.ok([playerId, blackId].includes(session.playerId));
  assert.notEqual(session.invitedPlayerId, session.playerId);
  assert.equal(session.playerColor, session.playerId === playerId ? "WHITE" : "BLACK");
});

test("joining uses the invitation ID and local orientation without generating a new assignment", async () => {
  const api = { join: async (id, actor) => {
    assert.equal(actor, playerId);
    return { gamemode: "PVP", difficulty: "NOT_CPU", state };
  } };
  const { session } = await new SessionAccess(api).open(created.sessionId, "PVP", playerId, "BLACK");
  assert.equal(session.playerId, playerId);
  assert.equal(session.playerColor, "BLACK");
});
