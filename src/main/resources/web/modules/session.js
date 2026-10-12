import { saveSession, readSession } from "./preferences.js";

export class SessionAccess {
  constructor(api) {
    this.api = api;
  }

  async open(sessionId, mode = "PVP") {
    const response = sessionId
      ? await this.api.join(sessionId)
      : await this.api.create(mode, "NOT_CPU");

    const session = {
      sessionId: response.sessionId ?? response.id ?? sessionId,
      playerToken: response.playerToken ?? null,
      playerColor: response.playerColor ?? null,
      gamemode: response.gamemode ?? mode,
    };

    if (!session.sessionId) {
      throw new Error("El servidor no ha proporcionado el ID de sesión.");
    }

    this.api.token = session.playerToken;
    const state =
      response.state ??
      (response.pieces || response.board || response.fen
        ? response
        : await this.api.state(session.sessionId));
    return { session, state };
  }

  saved(sessionId) {
    return readSession(sessionId);
  }

  persist(session) {
    saveSession(session);
    const url = new URL(location.href);
    url.searchParams.set("session", session.sessionId);
    window.history.replaceState({}, "", url);
  }

  invitation(sessionId) {
    const url = new URL(location.href);
    url.search = "";
    url.hash = "";
    url.searchParams.set("session", sessionId);
    return url.href;
  }
}

export class SessionPolling {
  constructor(api, { sessionId, canPoll, onState, onError }) {
    this.api = api;
    this.sessionId = sessionId;
    this.canPoll = canPoll;
    this.onState = onState;
    this.onError = onError;
    this.timer = null;
    this.generation = 0;
    this.inFlight = false;
    this.failures = 0;
  }

  stop() {
    this.generation++;
    clearTimeout(this.timer);
    this.inFlight = false;
    this.failures = 0;
  }

  schedule(delay = 1000) {
    clearTimeout(this.timer);
    if (this.canPoll()) {
      this.timer = setTimeout(() => this.refresh(), delay);
    }
  }

  async refresh() {
    if (!this.canPoll() || this.inFlight) return;
    const generation = this.generation;
    const id = this.sessionId();
    this.inFlight = true;

    try {
      const response = await this.api.state(id);
      if (generation !== this.generation || id !== this.sessionId()) return;
      this.failures = 0;
      this.onState(response);
    } catch (error) {
      if (generation !== this.generation || error.name === "AbortError") return;
      this.failures++;
      this.onError(error);
    } finally {
      if (generation === this.generation) {
        this.inFlight = false;
        this.schedule(Math.min(15000, 1000 * 2 ** this.failures));
      }
    }
  }
}
