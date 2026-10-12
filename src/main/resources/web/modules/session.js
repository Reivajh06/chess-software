import { saveSession, readSession } from "./preferences.js";

export class SessionAccess {
  constructor(api) {
    this.api = api;
  }

  async open(sessionId, mode = "PVP", invitedPlayerId = null, invitedPlayerColor = "WHITE") {
    if (sessionId && !invitedPlayerId)
      throw new Error("La invitación debe incluir el ID asignado al jugador.");
    const difficulty = mode === "PRACTICE" ? "PRACTICE" : "NOT_CPU";
    const response = sessionId
      ? await this.api.join(sessionId, invitedPlayerId)
      : await this.api.create(mode, difficulty);

    const gamemode = response.gamemode ?? mode;
    const humanIds = ["PVP", "PRACTICE"].includes(gamemode);
    if (!sessionId && humanIds && (!response.whiteId || !response.blackId))
      throw new Error("El servidor no ha proporcionado los IDs de los jugadores.");
    if (!sessionId && gamemode === "PRACTICE" && response.whiteId !== response.blackId)
      throw new Error("La práctica necesita el mismo ID para blancas y negras.");
    const playerColor = sessionId ? invitedPlayerColor : !humanIds || gamemode === "PRACTICE"
      ? "WHITE"
      : crypto.getRandomValues(new Uint8Array(1))[0] < 128 ? "WHITE" : "BLACK";
    const playerId = humanIds
      ? (invitedPlayerId ?? (playerColor === "WHITE" ? response.whiteId : response.blackId))
      : null;
    const resolvedSessionId = response.sessionId ?? response.id ?? sessionId;
    if (!resolvedSessionId)
      throw new Error("El servidor no ha proporcionado el ID de sesión.");

    // The creator registers its randomly assigned PvP ID; practice has no join step.
    if (!sessionId && gamemode === "PVP") {
      const joined = await this.api.join(resolvedSessionId, playerId);
      response.state = joined.state ?? joined.gameState ?? response.state ?? response.gameState;
    }
    const session = {
      sessionId: resolvedSessionId,
      playerId,
      // A local rendering preference; requests authorize with playerId only.
      playerColor: gamemode === "AIVAI" ? null : playerColor,
      invitedPlayerId: !sessionId && gamemode === "PVP"
        ? (playerColor === "WHITE" ? response.blackId : response.whiteId) : null,
      invitedPlayerColor: !sessionId && gamemode === "PVP"
        ? (playerColor === "WHITE" ? "BLACK" : "WHITE") : null,
      gamemode,
      difficulty: response.difficulty ?? difficulty,
    };

    this.api.playerId = session.playerId;
    const state =
      response.state ?? response.gameState ??
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
    url.hash = "";
    window.history.replaceState({}, "", url);
  }

  invitation(sessionId) {
    const url = new URL(location.href);
    url.search = "";
    url.hash = "";
    url.searchParams.set("session", sessionId);
    const session = this.saved(sessionId);
    if (session?.invitedPlayerId) {
      url.hash = new URLSearchParams({ playerId: session.invitedPlayerId,
        side: session.invitedPlayerColor }).toString();
    }
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
