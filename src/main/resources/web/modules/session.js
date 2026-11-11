import { saveSession, readSession, forgetSession } from "./preferences.js";
import { ApiError, HttpStatus } from "./api.js";
import { modeDefinition } from "./modes.js";
import { strongestEngine } from "./engine-plugins.js";

const colors = ["WHITE", "BLACK"];
const rival = (color) => (color === "WHITE" ? "BLACK" : "WHITE");

const defaultDifficulty = { PVCPU: "EASY", AIVAI: "MAX" };
const pluginDifficulty = (mode, chosen) =>
	mode in defaultDifficulty ? (chosen ?? defaultDifficulty[mode]) : "NOT_PLUGIN";
const seated = (state) => (state ? { ...state, waitingForOpponent: false } : undefined);

/**
 * The session the server must be told about when the page drops {@code current} to go to {@code next} (or to the
 * menu): a game one plays in. A watcher never owned the game, and joining the session one already holds is no exit.
 * @returns the session id, or null when there is nothing to abandon
 */
export function abandonedSession(current, next = null) {
	if(!current?.sessionId || current.watching) return null;
	return current.sessionId === next ? null : current.sessionId;
}

export class SessionAccess {
	constructor(api) {
		this.api = api;
	}

	/**
	 * Follows a game without sitting in it: the state is read like any player does, but the server is never asked
	 * for a seat, so the room keeps its players. The board stays closed and no machine move is requested from here.
	 */
	async watch(sessionId) {
		let state;
		try {
			state = await this.api.state(sessionId);
		} catch (error) {
			if(error.status === HttpStatus.NOT_FOUND)
				throw new ApiError("The game does not exist.", error.status, error.code);
			throw error;
		}
		const session = { sessionId, playerColor: null, gamemode: state.gamemode ?? state.gameState?.gamemode ?? "SPECTATE", difficulty: "NOT_PLUGIN", watching: true };
		return { session, state };
	}

	async open(sessionId, mode = "PVP", chosenDifficulty, pluginNames) {
		if(sessionId && mode === "SPECTATE") return this.watch(sessionId);
		if(mode === "PVAI") mode = "PVCPU"; // Rematches of saved games use the merged mode.
		const alfiBoss = !sessionId && mode === "PVCPU" && chosenDifficulty === "ALFI";
		if(alfiBoss || (!sessionId && mode === "PVCPU" && chosenDifficulty === "MAX")) {
			const engine = strongestEngine(await this.api.plugins());
			if(alfiBoss && !engine) throw new Error("No chess engine is installed.");
			if(engine) pluginNames = [engine];
		}

		// Every mode with a plugin sends a plugin difficulty: the one the player chose, or the mode's default
		// (human vs machine starts easy; machines play at full strength). Other modes send NOT_PLUGIN.
		const difficulty = mode === "PRACTICE" ? "PRACTICE"
			: pluginDifficulty(mode, alfiBoss ? "MAX" : chosenDifficulty);
		const local = !sessionId && mode === "LOCAL";
		const practice = !sessionId && mode === "PRACTICE";
		const created = sessionId ? null :
			await this.api.create(modeDefinition(mode).server ?? mode, difficulty, pluginNames);
		const id = sessionId ?? created.sessionId ?? created.id;
		if(!id)
			throw new Error("The server did not provide the session ID.");

		let gamemode = local ? mode : (created?.gamemode ?? mode);
		let seat;
		let cpu = null;
		let machines = null;
		if(!sessionId && (gamemode === "PVCPU" || gamemode === "PVAI")) {
			const creator = this.creatorSeat(created);
			seat = { ...creator, state: seated(created.state ?? created.gameState) };
			cpu = { color: rival(creator.playerColor) };
		} else if(!sessionId && gamemode === "AIVAI") {
			machines = colors.map((color) => ({ color }));
			seat = { playerColor: null, state: seated(created.state ?? created.gameState) };
		} else if(local || practice) {
			this.creatorSeat(created);
			const second = await this.join(id);
			seat = {
				playerColor: null,
				state: second.state ?? second.gameState ?? await this.api.state(id),
			};
		} else if(sessionId) {
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
			...(cpu ? { cpu, alfiBoss, alfi: alfiBoss ? "hidden" : null } : {}),
			...(machines ? { machines } : {}),
			...(pluginNames ? { pluginNames } : {}),
		};

		const state = seat.state ?? seat.gameState ?? created?.state ?? created?.gameState ??
			await this.api.state(id);
		return { session, state };
	}

	creatorSeat(created) {
		if(colors.includes(created.playerColor))
			return { playerColor: created.playerColor };
		throw new Error("The server did not assign a color to the room's creator.");
	}

	async join(sessionId) {
		let seat;
		try {
			seat = await this.api.join(sessionId);
		} catch (error) {
			if(error.status === HttpStatus.CONFLICT)
				throw new ApiError("The room is full.", error.status, error.code);
			if(error.status === HttpStatus.NOT_FOUND)
				throw new ApiError("The room does not exist.", error.status, error.code);
			throw error;
		}
		if(!colors.includes(seat.playerColor))
			throw new Error("The server did not assign a color to this player.");
		return seat;
	}

	saved(sessionId) {
		const saved = readSession(sessionId);
		// Older seats already saved an explicit reveal phase. Do not infer a boss
		// from difficulty: a normal HARD or MAX game must remain a normal CPU.
		return saved ? { ...saved, alfiBoss: saved.alfiBoss ?? !!saved.alfi } : null;
	}

	forget(sessionId) {
		forgetSession(sessionId);
		const url = new URL(location.href);
		url.searchParams.delete("session");
		url.searchParams.delete("watch");
		url.hash = "";
		window.history.replaceState({}, "", url);
	}

	persist(session) {
		const url = new URL(location.href);
		// A watcher has no seat to remember: reloading the page just watches again.
		if(session.watching) {
			url.searchParams.delete("session");
			url.searchParams.set("watch", session.sessionId);
		} else {
			saveSession(session);
			url.searchParams.delete("watch");
			url.searchParams.set("session", session.sessionId);
		}
		url.hash = "";
		window.history.replaceState({}, "", url);
	}

	/** The link that invites to play ("play": takes the free seat) or to watch ("watch": read-only). */
	invitation(sessionId, origin = location.origin, kind = "play") {
		const url = new URL("/", origin);
		url.searchParams.set(kind === "watch" ? "watch" : "session", sessionId);
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
		if(this.canPoll()) {
			this.timer = setTimeout(() => this.refresh(), delay);
		}
	}

	async refresh() {
		if(!this.canPoll() || this.inFlight) return;
		const generation = this.generation;
		const id = this.sessionId();
		this.inFlight = true;

		try {
			const response = await this.api.state(id);
			if(generation !== this.generation || id !== this.sessionId()) return;
			this.failures = 0;
			this.onState(response);
		} catch (error) {
			if(generation !== this.generation || error.name === "AbortError") return;
			this.failures++;
			this.onError(error);
		} finally {
			if(generation === this.generation) {
				this.inFlight = false;
				this.schedule(Math.min(15000, 1000 * 2 ** this.failures));
			}
		}
	}
}
