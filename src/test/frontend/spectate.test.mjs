import test from "node:test";
import assert from "node:assert/strict";
import { ChessApi, ApiError, HttpStatus } from "../../main/resources/web/modules/api.js";
import { SessionAccess } from "../../main/resources/web/modules/session.js";
import { parseInvitation, parseWatchInvitation, readSession } from "../../main/resources/web/modules/preferences.js";
import { boardInteractive } from "../../main/resources/web/modules/selection.js";

const sessionId = "6e26bb12-02ae-4e08-a99f-c0af94d67608";
const state = {
  status: "ACTIVE", turn: "WHITE", checkedColor: null, waitingForOpponent: false,
  fen: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", moves: [],
};
const watcher = {
  sessionId, playerColor: null, gamemode: "SPECTATE", difficulty: "NOT_PLUGIN", watching: true,
};

test("watch reads the state without taking a seat and returns a spectator session", async (t) => {
  const calls = [];
  t.mock.method(globalThis, "fetch", async (path, options) => {
    calls.push({ path, options });
    return new Response(JSON.stringify(state), { status: 200 });
  });
  const result = await new SessionAccess(new ChessApi()).watch(sessionId);
  assert.deepEqual(result, { session: watcher, state });
  assert.equal(calls.length, 1);
  assert.equal(calls[0].path, `/api/sessions/${sessionId}`);
  assert.equal(calls[0].options.method, "GET");
  assert.equal(calls[0].options.body, undefined);
});

test("opening SPECTATE delegates to watch and never requests the join route", async (t) => {
  const calls = [];
  t.mock.method(globalThis, "fetch", async (path, options) => {
    calls.push({ path, options });
    return new Response(JSON.stringify(state), { status: 200 });
  });
  const sessions = new SessionAccess(new ChessApi());
  const watch = t.mock.method(sessions, "watch");
  assert.deepEqual(await sessions.open(sessionId, "SPECTATE"), { session: watcher, state });
  assert.equal(watch.mock.callCount(), 1);
  assert.deepEqual(watch.mock.calls[0].arguments, [sessionId]);
  assert.equal(calls.length, 1);
  assert.equal(calls[0].path, `/api/sessions/${sessionId}`);
  assert.equal(calls[0].options.method, "GET");
  assert.equal(calls.some(({ path }) => path.endsWith("/join")), false);
});

test("watch translates a missing game while preserving its error status and code", async (t) => {
  t.mock.method(globalThis, "fetch", async () => new Response(JSON.stringify({
    error: "SESSION_NOT_FOUND", message: "Unknown session on server",
  }), { status: HttpStatus.NOT_FOUND }));
  await assert.rejects(new SessionAccess(new ChessApi()).watch(sessionId),
    error => error instanceof ApiError && error.message === "La partida no existe." &&
      error.status === HttpStatus.NOT_FOUND && error.code === "SESSION_NOT_FOUND");
});

test("watch preserves other server errors", async (t) => {
  t.mock.method(globalThis, "fetch", async () => new Response(JSON.stringify({
    error: "UNAVAILABLE", message: "Try again later",
  }), { status: HttpStatus.SERVICE_UNAVAILABLE }));
  await assert.rejects(new SessionAccess(new ChessApi()).watch(sessionId),
    error => error instanceof ApiError && error.message === "Try again later" &&
      error.status === HttpStatus.SERVICE_UNAVAILABLE && error.code === "UNAVAILABLE");
});

test("invitations use watch for spectators and session for players by default", () => {
  const sessions = new SessionAccess(new ChessApi());
  const origin = "http://localhost:5000";
  assert.equal(sessions.invitation(sessionId, origin, "watch"), `${origin}/?watch=${sessionId}`);
  assert.equal(sessions.invitation(sessionId, origin, "play"), `${origin}/?session=${sessionId}`);
  assert.equal(sessions.invitation(sessionId, origin), `${origin}/?session=${sessionId}`);
  assert.equal(sessions.invitation(sessionId, origin, "other"), `${origin}/?session=${sessionId}`);
});

test("watch invitations accept full URLs with valid IDs from one through 128 characters", () => {
  for (const id of ["a", "AZaz09_-", sessionId, "a".repeat(128)]) {
    assert.equal(parseWatchInvitation(`http://localhost:5000/?watch=${id}`), id);
  }
  assert.equal(parseWatchInvitation(`  https://example.com/?watch=${sessionId}  `), sessionId);
});

test("watch invitations reject player links, plain IDs, empty input and invalid IDs", () => {
  const invalid = [
    `http://localhost:5000/?session=${sessionId}`, sessionId, "", "   ", "junk text",
    "http://[invalid/?watch=abc", "http://localhost:5000/?watch=",
    `http://localhost:5000/?watch=${"a".repeat(129)}`,
    ...["bad id", "bad/id", "bad? id", "bad.id", "bad+id", "ñ"].map(id =>
      `http://localhost:5000/?watch=${encodeURIComponent(id)}`),
  ];
  for (const value of invalid) {
    assert.equal(parseWatchInvitation(value), null, JSON.stringify(value));
  }
});

test("player invitation parsing still uses session and ignores watch-only links", () => {
  assert.equal(parseInvitation(`http://localhost:5000/?session=${sessionId}`), sessionId);
  assert.equal(parseInvitation(`http://localhost:5000/?watch=${sessionId}`), null);
  assert.equal(parseInvitation(`http://localhost:5000/?watch=watcher&session=${sessionId}`), sessionId);
  assert.equal(parseInvitation(sessionId), sessionId);
});

function browserStubs(t, href) {
  const store = new Map([["unrelated", "keep"]]);
  const urls = [];
  t.after(() => {
    delete globalThis.location;
    delete globalThis.window;
    delete globalThis.sessionStorage;
  });
  globalThis.location = new URL(href);
  globalThis.window = { history: { replaceState(_, __, url) {
    urls.push(new URL(url, globalThis.location.href));
    globalThis.location.href = urls.at(-1).href;
  } } };
  globalThis.sessionStorage = {
    getItem: (key) => store.get(key) ?? null,
    setItem: (key, value) => store.set(key, String(value)),
    removeItem: (key) => store.delete(key),
  };
  const writes = t.mock.method(globalThis.sessionStorage, "setItem");
  return { store, urls, writes };
}

test("persisting a watcher replaces the player URL parameter without saving a seat", (t) => {
  const { store, urls, writes } = browserStubs(t,
    "http://localhost:5000/?session=old-player&watch=old-watch&theme=classic#board");
  new SessionAccess(new ChessApi()).persist(watcher);
  assert.equal(urls.length, 1);
  assert.equal(urls[0].href, `http://localhost:5000/?watch=${sessionId}&theme=classic`);
  assert.equal(urls[0].searchParams.has("session"), false);
  assert.equal(writes.mock.callCount(), 0);
  assert.deepEqual(store, new Map([["unrelated", "keep"]]));
  assert.equal(readSession(sessionId), null);
});

test("persisting a player saves the seat and replaces the watch URL parameter", (t) => {
  const { store, urls, writes } = browserStubs(t,
    "http://localhost:5000/?watch=old-watch&session=old-player&theme=classic#board");
  const player = { sessionId, playerColor: "WHITE", gamemode: "PVP", difficulty: "NOT_PLUGIN" };
  new SessionAccess(new ChessApi()).persist(player);
  assert.equal(urls.length, 1);
  assert.equal(urls[0].href, `http://localhost:5000/?session=${sessionId}&theme=classic`);
  assert.equal(urls[0].searchParams.has("watch"), false);
  assert.equal(writes.mock.callCount(), 1);
  assert.equal(store.get(`archchess.session.${sessionId}`), JSON.stringify(player));
  assert.deepEqual(readSession(sessionId), player);
  assert.equal(store.get("unrelated"), "keep");
});

test("watching blocks board interaction even in another active game mode", () => {
  assert.equal(boardInteractive(watcher, state), false);
  assert.equal(boardInteractive({ ...watcher, gamemode: "PVP", playerColor: "WHITE" }, state), false);
  assert.equal(boardInteractive({ sessionId, gamemode: "PVP", playerColor: "WHITE" }, state), true);
  assert.equal(boardInteractive({ sessionId, gamemode: "PVP", watching: false }, state), true);
});
