import { saveSession, readSession, forgetSession } from "./preferences.js";
import { ApiError, HttpStatus } from "./api.js";
import { modeDefinition } from "./modes.js";

const colors = ["WHITE", "BLACK"];
const rival = (color) => (color === "WHITE" ? "BLACK" : "WHITE");

// La respuesta de creación marca siempre waitingForOpponent; con una máquina sentada nadie más tiene que
// unirse, así que el tablero no se bloquea.
const seated = (state) => (state ? { ...state, waitingForOpponent: false } : undefined);

export class SessionAccess {
  constructor(api) {
    this.api = api;
  }

  async open(sessionId, mode = "PVP", cpuDifficulty = "EASY") {
    const difficulty = mode === "PRACTICE" ? "PRACTICE" : mode === "PVCPU" ? cpuDifficulty : "NOT_CPU";
    const local = !sessionId && mode === "LOCAL";
    const practice = !sessionId && mode === "PRACTICE";
    const created = sessionId ? null :
      await this.api.create(modeDefinition(mode).server ?? mode, difficulty);
    const id = sessionId ?? created.sessionId ?? created.id;
    if (!id)
      throw new Error("El servidor no ha proporcionado el ID de sesión.");

    let gamemode = local ? mode : (created?.gamemode ?? mode);
    let seat;
    let cpu = null;
    let machines = null;
    if (!sessionId && (gamemode === "PVCPU" || gamemode === "PVAI")) {
      // El servidor sienta a la máquina al crear la partida, en el color contrario al del creador, y
      // calcula sus jugadas. En la dificultad máxima espera Alfi.
      const creator = this.creatorSeat(created);
      seat = { ...creator, state: seated(created.state ?? created.gameState) };
      cpu = { color: rival(creator.playerColor) };
    } else if (!sessionId && gamemode === "AIVAI") {
      // Los dos asientos son máquinas: este equipo solo mira y pide cada jugada cuando le toca a una.
      machines = colors.map((color) => ({ color }));
      seat = { playerColor: null, state: seated(created.state ?? created.gameState) };
    } else if (local || practice) {
      // Los dos colores se juegan en este equipo: ocupamos también el segundo asiento.
      this.creatorSeat(created);
      const second = await this.join(id);
      seat = {
        playerColor: null,
        state: second.state ?? second.gameState ?? await this.api.state(id),
      };
    } else if (sessionId) {
      seat = await this.join(id);
      gamemode = seat.gamemode ?? gamemode;
    } else {
      seat = this.creatorSeat(created);
    }

    const session = {
      sessionId: id,
      playerColor: seat.playerColor,
      gamemode,
      difficulty: seat.difficulty ?? created?.difficulty ?? difficulty,
      ...(cpu ? { cpu, alfi: gamemode === "PVCPU" && difficulty === "HARD" ? "hidden" : null } : {}),
      ...(machines ? { machines } : {}),
    };

    const state = seat.state ?? seat.gameState ?? created?.state ?? created?.gameState ??
      await this.api.state(id);
    return { session, state };
  }

  creatorSeat(created) {
    if (colors.includes(created.playerColor))
      return { playerColor: created.playerColor };
    throw new Error("El servidor no ha asignado un color al creador de la sala.");
  }

  async join(sessionId) {
    let seat;
    try {
      seat = await this.api.join(sessionId);
    } catch (error) {
      if (error.status === HttpStatus.CONFLICT)
        throw new ApiError("La sala ya está completa.", error.status, error.code);
      if (error.status === HttpStatus.NOT_FOUND)
        throw new ApiError("La sala no existe.", error.status, error.code);
      throw error;
    }
    if (!colors.includes(seat.playerColor))
      throw new Error("El servidor no ha asignado un color a este jugador.");
    return seat;
  }

  saved(sessionId) {
    return readSession(sessionId);
  }

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
