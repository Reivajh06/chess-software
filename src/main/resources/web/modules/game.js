import { byId, dialog, tabGroup, wait } from "./ui.js";
import { HttpStatus } from "./api.js";
import { GameStore, normalizeState, stateSignature } from "./state.js";
import { BoardView } from "./board.js";
import { HistoryView } from "./history.js";
import { SessionPolling, abandonedSession } from "./session.js";
import { MoveQueue } from "./move-queue.js";
import { Commentator } from "./commentary.js";
import { rivalKind } from "./alfi-reactions.js";
import { ThinkingLog } from "./thinking.js";
import { loadAccount } from "./account.js";
import { accountIdentity, accountTone, newGame, reserveTone } from "./identity.js";
import { renderMatch, renderAppearance } from "./interface.js";
import { createMachinePlayer } from "./machine-player.js";
import { createMoveInput } from "./move-input.js";
import { createResults } from "./game-result.js";

export function createGame({ api, preferences, audio, sessions, alfi, gameMascot, showScreen }) {
	const store = new GameStore();
	const moveQueue = new MoveQueue();
	const board = new BoardView(byId("board"), store, preferences, (tile) => input.selectSquare(tile));
	const historyView = new HistoryView(byId("move-history"), store, preferences, reviewPosition);
	const thinkingLog = new ThinkingLog(byId("thinking-log"), byId("thinking-live"));
	const commentator = new Commentator();
	let sessionVersion = 0;
	const version = () => sessionVersion;
	let waitingTimer = null;
	const activeMascot = () => alfi.active ? alfi.seat : gameMascot;
	const schedulePoll = (delay = 1000) => liveSession.schedule(delay);
	const machines = createMachinePlayer({ store, api, board, alfi, audio, sessions, gameMascot,
		thinkingLog, version, renderGame, applyGameState, submitMove, sendMove, schedulePoll });
	const { scheduleCpu, scheduleAlfiWait, playerMove } = machines;
	const input = createMoveInput({ store, api, board, audio, preferences, version, playerMove, clearMoveWarning, setNotice });
	const { closePromotionPicker } = input;
	const showResult = createResults({ store, alfi, preferences, audio, version, mascotReact });
	const sideKey = "archchess.side-open";
	let savedSide = "moves";
	try { savedSide = localStorage.getItem(sideKey) === "thinking" ? "thinking" : "moves"; } catch {}
	const openSideSection = tabGroup({ moves: byId("moves-tab"), thinking: byId("thinking-tab") }, (name) => {
		byId("side-panel").dataset.open = name;
		try { localStorage.setItem(sideKey, name); } catch {}
	}, savedSide);
	function mascotReact(kind, priority = 1, delay = 650, options = {}, vars = {}) {
		setTimeout(() => {
			if(document.body.dataset.screen !== "game" || store.session?.alfi === "revealing") return;
			activeMascot().talk(kind, priority, options, vars);
		}, delay);
	}

	function scheduleWaitingChat() {
		clearTimeout(waitingTimer);
		if(!store.state?.waitingForOpponent) return;
		waitingTimer = setTimeout(() => {
			if(document.body.dataset.screen === "game" && store.state?.waitingForOpponent) {
				gameMascot.talk("waiting");
			}
			scheduleWaitingChat();
		}, 11000);
	}

	const liveSession = new SessionPolling(api, {
		sessionId: () => store.session?.sessionId,

		canPoll: () =>
				!!store.session &&
				store.state?.status === "ACTIVE" &&
				!store.selection.busy && !store.cpuBusy,

		onState: (response) => {
			setConnection("Connected", "online");
			applyGameState(response);
		},

		onError: (error) => {
			if(error.status === HttpStatus.NOT_FOUND) {
				liveSession.stop();
				setConnection("No game");
				setNotice("The game no longer exists on the server. Return to the menu and start a new one.");
				return;
			}

			setConnection("Offline");
			setNotice(error.message, true);
		},
	});

	function setNotice(message = "", retry = false) {
		byId("notice").textContent = message;
		byId("notice").hidden = !message;
		byId("retry").hidden = !retry;
	}

	function clearMoveWarning() {
		byId("move-warning").hidden = true;
		byId("move-warning-text").textContent = "";
	}

	function setConnection(text, state = "") {
		byId("connection").textContent = text;
		byId("connection").dataset.state = state;
	}

	function renderGame() {
		board.render();
		historyView.render();
		renderMatch(store, preferences);
		if(store.session?.alfiBoss && byId("side-panel").dataset.open !== "moves") openSideSection("moves");
		renderAppearance(preferences);
	}

	function applyGameState(payload) {
		const before = store.state;
		const next = normalizeState(payload);
		const signature = stateSignature(next);

		if(before && signature === store.lastStateSignature) {
			return false;
		}

		const moved = before && next.moves.length !== before.moves.length;
		const state = store.applyNormalized(next);

		if(moved && !store.selection.busy) {
			store.selection.reset();
			closePromotionPicker();
		}
		store.lastStateSignature = signature;

		clearMoveWarning();
		dialog("move-choice-dialog", false);
		renderGame();

		if(before?.waitingForOpponent && !state.waitingForOpponent) {
			audio.play("confirm");
			mascotReact("opponentJoined", 2, 0);
		}
		scheduleWaitingChat();

		if(before && state.moves.length > before.moves.length) {
			const move = state.moves.at(-1);
			const contactDelay = move?.capture ? board.captureDelay : 0;

			const promotion =
					move?.type === "PAWN" && /[18]$/.test(move.to ?? "") || !!move?.promotion;

			const cue = move?.capture ? "capture" : promotion ? "promotion" : move?.type === "KING" ? "king_move" : "move";
			audio.play(cue, contactDelay);

			if(promotion && move?.capture) {
				audio.play("promotion", contactDelay + 0.18);
			}

			if(state.status === "ACTIVE" && state.checkedColor) {
				audio.play("check", contactDelay + (promotion ? 0.45 : 0.22));
			}

			if(state.checkedColor) {
				shakeBoard(contactDelay);
			}

			const reaction = commentator.react({
				move,
				before,
				after: state,
				userColors: store.practice || store.local
						? ["WHITE", "BLACK"]
						: store.session?.playerColor ? [store.session.playerColor] : [],
			});

			if(reaction && alfi.active && store.session?.cpu) {
				const kind = rivalKind(reaction.kind, { move, after: state, cpuColor: store.session.cpu.color });
				if(kind) mascotReact(kind, reaction.priority, 650, {}, reaction.vars);
			} else if(reaction) {
				mascotReact(reaction.kind, reaction.priority, 650, { stare: reaction.stare }, reaction.vars);
			}
		}

		alfi.followGame(state);
		if(["WHITE_WIN", "BLACK_WIN", "DRAW"].includes(state.status)) {
			showResult(state.status);
		} else if(store.reviewPly === null) {
			setNotice(state.waitingForOpponent ? "Waiting for opponent" : state.checkedColor ? "Check" : "");
		}

		faceSideToMove();
		scheduleCpu();
		scheduleAlfiWait();
		return true;
	}

	async function faceSideToMove() {
		if(!store.local || store.turning) return;

		const turn = store.state?.turn;
		if(store.localSide === null) {
			store.localSide = turn ?? "WHITE";
			renderGame();
			return;
		}
		if(store.state?.status !== "ACTIVE" || turn === store.localSide) return;

		const generation = sessionVersion;
		store.turning = true;
		board.render();

		await board.settle();
		await wait(250);
		if(generation !== sessionVersion) return;

		store.localSide = store.state.turn;
		store.reviewPly = null;
		renderMatch(store, preferences);
		historyView.render();
		await board.spin();
		if(generation !== sessionVersion) return;

		store.turning = false;
		renderGame();
		faceSideToMove();
	}

	function shakeBoard(delay = 0) {
		const frame = document.querySelector(".board-frame");
		setTimeout(() => {
			frame.classList.remove("shake");
			void frame.offsetWidth;
			frame.classList.add("shake");
		}, delay * 1000 + 120);
	}

	function reviewPosition(ply) {
		if(store.selection.busy) {
			return;
		}

		if(ply !== null && !store.snapshots.has(ply)) {
			setNotice("The server did not provide this past position.");
			return;
		}
		store.reviewPly = ply;
		setNotice();

		store.selection.reset();
		clearMoveWarning();
		renderGame();
	}

	function submitMove({ from, to, moveId, promotion = null }) {
		return sendMove(() => api.move(store.session.sessionId, from, to, moveId, promotion));
	}

	function sendMove(request, options) {
		const generation = sessionVersion;
		return moveQueue.run(() => generation === sessionVersion ? performMove(request, options) : false);
	}

	async function performMove(request, { quietFailure = () => false } = {}) {
		clearMoveWarning();
		liveSession.stop();
		api.cancel();
		board.render();

		const generation = sessionVersion;
		let stateApplied = false;

		try {
			const response = await request();

			if(sessionVersion === generation) {
				stateApplied = applyGameState(response);
				return true;
			}
		} catch (error) {
			if(
					sessionVersion === generation &&
					error.name !== "AbortError" &&
					!quietFailure()
			) {
				setNotice(store.session?.alfiBoss ? "Alfi couldn't move. He'll try again." : error.message, true);
			}
		} finally {
			if(sessionVersion === generation) {
				store.selection.reset();

				if(!stateApplied) {
					board.render();
				}

				schedulePoll(0);
			}
		}
		return false;
	}

	function resetSession(next = null) {
		const dropped = store.session;
		sessionVersion++;
		thinkingLog.reset();
		// New game, new colours for plugins and guests; a registered player keeps the colour of the account.
		newGame();
		const signedIn = loadAccount();
		if(signedIn) reserveTone(accountIdentity(signedIn.username), accountTone(signedIn.username));
		commentator.reset();

		api.cancel();
		const abandoned = abandonedSession(dropped, next);
		if(abandoned) api.abandon(abandoned);
		if(dropped && dropped.sessionId !== next) sessions.forget(dropped.sessionId);
		liveSession.stop();
		clearMoveWarning();
		clearTimeout(waitingTimer);

		machines.reset();

		store.reset();
		store.lastStateSignature = null;

		byId("promotion-layer").hidden = true;

		for(const id of ["move-choice-dialog", "result-dialog", "invite-dialog"]) dialog(id, false);
	}

	async function activateSession(session, state) {
		if(session.alfi === "hidden" && normalizeState(state).moves.length) session.alfi = "revealed";
		store.session = session;
		if(session.cpu) alfi.side = session.cpu.color;
		store.localSide = null;

		applyGameState(state);
		sessions.persist(session);
		showScreen("game");
		if(alfi.active && session.alfi === "revealed") {
			alfi.seat.talk("bossReturn", 1);
		} else {
			activeMascot().talk(
					store.state?.waitingForOpponent ? "waiting" : store.state?.moves.length ? "idle" : "start", 1
			);
		}

		setConnection("Connected", "online");
		schedulePoll();
		scheduleCpu();
		scheduleAlfiWait();
	}

	byId("flip").addEventListener("click", () => {
		store.flipped = !store.flipped;
		renderGame();
	});

	const historyActions = {
		"return-live": () => null,
		"history-live": () => null,
		"history-start": () => 0,
		"history-prev": () => Math.max(0, (store.reviewPly ?? store.state.moves.length) - 1),
		"history-next": () => {
			const next = (store.reviewPly ?? store.state.moves.length) + 1;
			return next >= store.state.moves.length ? null : next;
		},
	};
	for(const [id, target] of Object.entries(historyActions)) {
		byId(id).addEventListener("click", () => reviewPosition(target()));
	}

	document.addEventListener("keydown", (event) => {
		if(
				document.body.dataset.screen !== "game" ||
				event.altKey || event.ctrlKey || event.metaKey ||
				document.querySelector("dialog[open]") ||
				event.target.closest?.("input, select, textarea, .board")
		) {
			return;
		}

		const button = byId({
			ArrowLeft: "history-prev",
			ArrowRight: "history-next",
			Home: "history-start",
			End: "history-live",
		}[event.key]);

		if(button && !button.disabled) {
			event.preventDefault();
			button.click();
		}
	});

	byId("retry").addEventListener("click", () => {
		liveSession.failures = 0;

		if(store.session) {
			liveSession.refresh();
		} else {
			showScreen("title");
		}
	});
	return { store, render: renderGame, activate: activateSession, reset: resetSession, version,
		stop: () => liveSession.stop(), activeMascot };
}
