import { saveSession, readSession, forgetSession } from "./preferences.js";
import { ApiError } from "./api.js";

const colors = ["WHITE", "BLACK"];

export class SessionAccess {
  constructor(api) {
    this.api = api;
  }

  // Creates a session (no sessionId) or joins an existing one. Seats belong
  // to the server: creation seats the creator on a random colour and /join,
  // called only by the invited player, hands out the remaining one. Each
  // seat comes with the ID that authorizes that player's moves.
  async open(sessionId, mode = "PVP") {
    const difficulty = mode === "PRACTICE" ? "PRACTICE" : "NOT_CPU";
    const created = sessionId ? null : await this.api.create(mode, difficulty);
    const id = sessionId ?? created.sessionId ?? created.id;
    if (!id)
      throw new Error("El servidor no ha proporcionado el ID de sesión.");

    let gamemode = created?.gamemode ?? mode;
    let seat;
    if (sessionId) {
      seat = await this.join(id);
      gamemode = seat.gamemode ?? gamemode;
    } else if (gamemode === "PVP" || gamemode === "PRACTICE") {
      seat = this.creatorSeat(created, gamemode);
    } else {
      seat = { playerId: null, playerColor: gamemode === "AIVAI" ? null : "WHITE" };
    }

    const session = {
      sessionId: id,
      playerId: seat.playerId,
      // A local rendering preference; requests authorize with playerId only.
      playerColor: seat.playerColor,
      gamemode,
      difficulty: seat.difficulty ?? created?.difficulty ?? difficulty,
    };

    this.api.playerId = session.playerId;
    const state = seat.state ?? seat.gameState ?? created?.state ?? created?.gameState ??
      await this.api.state(id);
    return { session, state };
  }

  creatorSeat(created, gamemode) {
    if (created.playerId && colors.includes(created.playerColor))
      return { playerId: created.playerId, playerColor: created.playerColor };
    // Practice moves both colours with one ID; older backends send it as whiteId.
    if (gamemode === "PRACTICE" && (created.playerId ?? created.whiteId))
      return { playerId: created.playerId ?? created.whiteId, playerColor: "WHITE" };
    throw new Error("El servidor no ha asignado un color al creador de la sala.");
  }

  async join(sessionId) {
    let seat;
    try {
      seat = await this.api.join(sessionId);
    } catch (error) {
      if (error.status === 409)
        throw new ApiError("La sala ya está completa.", 409);
      if (error.status === 404)
        throw new ApiError("La sala no existe o el servidor todavía no permite unirse.", 404);
      if (error.status === 500)
        throw new ApiError("No se pudo entrar: puede que la sala ya no exista en el servidor.", 500);
      if ([405, 501].includes(error.status))
        throw new ApiError("El servidor todavía no permite unirse a partidas.", error.status);
      throw error;
    }
    if (!seat.playerId || !colors.includes(seat.playerColor))
      throw new Error("El servidor no ha asignado un color a este jugador.");
    return seat;
  }

  saved(sessionId) {
    return readSession(sessionId);
  }

  // Drops a session the server no longer has (sessions live in its memory,
  // so a restart loses them) and takes it out of the address bar.
  forget(sessionId) {
    forgetSession(sessionId);
    const url = new URL(location.href);
    url.searchParams.delete("session");
    url.hash = "";
    window.history.replaceState({}, "", url);
  }

  persist(session) {
    saveSession(session);
    const url = new URL(location.href);
    url.searchParams.set("session", session.sessionId);
    url.hash = "";
    window.history.replaceState({}, "", url);
  }

  // The link only names the session: whoever opens it gets the free seat.
  // `origin` is where other machines reach this server (never loopback).
  invitation(sessionId, origin = location.origin) {
    const url = new URL("/", origin);
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
