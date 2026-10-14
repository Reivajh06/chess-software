import test from "node:test";
import assert from "node:assert/strict";
import { ChessApi, ApiError } from "../../main/resources/web/modules/api.js";
import { SessionAccess } from "../../main/resources/web/modules/session.js";
import { GameStore, normalizeState, stateSignature } from "../../main/resources/web/modules/state.js";

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
  await api.move(created.sessionId, "e2", "e4", 1);
  assert.equal(calls[0].options.method, "POST");
  assert.ok(calls[0].path.endsWith("/legal-moves"));
  assert.deepEqual(JSON.parse(calls[0].options.body), { playerId, from: "e2" });
  assert.deepEqual(JSON.parse(calls[1].options.body), { playerId, from: "e2", to: "e4", moveId: 1 });
  assert.equal(calls[1].options.headers["X-Player-Token"], undefined);
});

const events = [
  {
    pieceMoved: { color: "WHITE", type: "PAWN" }, pieceCaptured: null,
    from: { file: "E", rank: "R2" }, to: { file: "E", rank: "R4" },
    gameStateFen: "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
  },
  {
    pieceMoved: { color: "BLACK", type: "PAWN" }, pieceCaptured: null,
    from: { file: "D", rank: "R7" }, to: { file: "D", rank: "R5" },
    gameStateFen: "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
  },
  {
    pieceMoved: { color: "WHITE", type: "PAWN" },
    pieceCaptured: { color: "BLACK", type: "PAWN" },
    from: { file: "E", rank: "R4" }, to: { file: "D", rank: "R5" },
    gameStateFen: "rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2",
  },
];

function currentState(ply, serialized = true) {
  const history = events.slice(0, ply).reverse();
  return {
    status: "ACTIVE", turnColor: ply % 2 ? "BLACK" : "WHITE", checkedColor: null,
    fen: ply ? events[ply - 1].gameStateFen : state.fen,
    eatenPieces: { WHITE: [], BLACK: ply === 3 ? [events[2].pieceCaptured] : [] },
    // GetReadCurrentState serializes the deque before adding it to its response.
    movesHistory: serialized ? JSON.stringify(history) : history,
  };
}

test("empty move responses refresh the board, chronological history and captures through the current POST route", async (t) => {
  let ply = 0;
  const calls = [];
  t.mock.method(globalThis, "fetch", async (path, options) => {
    calls.push({ path, options });
    assert.equal(options.method, "POST");
    assert.equal(options.cache, "no-store");
    if (path.endsWith("/moves")) {
      ply++;
      return new Response("", { status: 200 });
    }
    assert.equal(path, `/api/sessions/${created.sessionId}`);
    assert.deepEqual(JSON.parse(options.body), {});
    return new Response(JSON.stringify(currentState(ply)), {
      status: 200, headers: { "Content-Type": "text/plain" },
    });
  });
  const api = new ChessApi();
  api.playerId = playerId;
  const store = new GameStore();
  store.session = { ...created, playerId, playerColor: "WHITE" };
  store.apply(state);
  const moves = [["e2", "e4", 1], ["d7", "d5", 1], ["e4", "d5", 2]];
  for (const [index, [from, to, moveId]] of moves.entries()) {
    store.selected = from;
    store.setLegalMoves({ moves: [{ moveId, tiles: [to] }] });
    store.apply(await api.move(created.sessionId, from, to, moveId));
    assert.equal(store.pieces.has(from), false);
    assert.deepEqual(store.pieces.get(to), events[index].pieceMoved);
    assert.equal(store.state.moves.length, index + 1);
    assert.equal(store.state.turn, index % 2 ? "WHITE" : "BLACK");
    assert.equal(store.selected, null);
    assert.equal(store.targets.size, 0);
    assert.equal(calls.length, (index + 1) * 2);
  }
  assert.deepEqual(store.state.moves.map(move => move.notation), ["e2–e4", "d7–d5", "e4×d5"]);
  assert.deepEqual(store.state.moves.map(move => move.ply), [1, 2, 3]);
  assert.deepEqual(store.state.moves.map(move => move.capturedPiece),
    [null, null, { color: "BLACK", type: "PAWN" }]);
  assert.equal(store.pieces.size, 31);
  assert.deepEqual(store.state.eatenPieces.BLACK, [{ color: "BLACK", type: "PAWN" }]);
  assert.equal(store.snapshots.get(0).has("e2"), true);
  assert.equal(store.snapshots.get(1).has("e4"), true);
  assert.equal(store.snapshots.get(2).get("d5").color, "BLACK");
  assert.equal(store.snapshots.get(3).get("d5").color, "WHITE");
});

test("serialized and array histories normalize to the same state in supported envelopes", () => {
  const serialized = normalizeState(currentState(3));
  const structured = normalizeState(currentState(3, false));
  assert.equal(stateSignature(serialized), stateSignature(structured));
  for (const payload of [{ state: currentState(3) }, { gameState: currentState(3) },
    { session: { gameState: currentState(3) } }]) {
    assert.equal(stateSignature(normalizeState(payload)), stateSignature(serialized));
  }
  const initial = normalizeState(currentState(0));
  assert.equal(initial.moves.length, 0);
  assert.equal(initial.pieces.size, 32);
});

test("rejects malformed serialized histories instead of silently dropping moves", () => {
  for (const movesHistory of ["not JSON", "{}", "null", 42]) {
    assert.throws(() => normalizeState({ ...currentState(1), movesHistory }),
      /El historial de movimientos no es válido/);
  }
});

test("a corrected captured-piece identity triggers a history refresh", () => {
  const pawn = normalizeState(currentState(3, false));
  const changed = currentState(3, false);
  changed.movesHistory[0] = { ...changed.movesHistory[0], pieceCaptured: { color: "BLACK", type: "KNIGHT" } };
  const knight = normalizeState(changed);
  assert.deepEqual(knight.moves[2].capturedPiece, { color: "BLACK", type: "KNIGHT" });
  assert.notEqual(stateSignature(pawn), stateSignature(knight));
});

test("an inline updated state is consumed without a second request", async (t) => {
  let calls = 0;
  t.mock.method(globalThis, "fetch", async () => {
    calls++;
    return new Response(JSON.stringify({ state: currentState(1) }), { status: 200 });
  });
  const result = normalizeState(await new ChessApi().move(created.sessionId, "e2", "e4", 1));
  assert.equal(calls, 1);
  assert.equal(result.pieces.has("e2"), false);
  assert.equal(result.moves[0].to, "e4");
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
