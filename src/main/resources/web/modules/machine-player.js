import { wait } from "./ui.js";
import { setColorSwap } from "./colors.js";
import { legalOptions } from "./cpu.js";
import { normalizeLegalMoves } from "./selection.js";
import { cheatChances, cleanPlies, flipLead, materialLead, teleport } from "./cheats.js";
import { profileLine } from "./alfi-profile.js";
import { machineName } from "./thinking.js";

export function createMachinePlayer({ store, api, board, alfi, audio, sessions, gameMascot, thinkingLog, version, renderGame, applyGameState, submitMove, sendMove, schedulePoll }) {
	let cpuTimer = null;
	let cpuFailures = 0;
	let alfiWaitTimer = null;
	function scheduleAlfiWait(nudges = 0) {
		clearTimeout(alfiWaitTimer);
		const session = store.session;
		if(!session?.cpu || session.alfi !== "revealed" || nudges >= 3) return;
		const ply = store.state?.moves.length;
		alfiWaitTimer = setTimeout(() => {
			const stillWaiting = store.state?.moves.length === ply && store.state?.status === "ACTIVE" &&
				store.state.turn === session.playerColor && !store.cpuBusy;
			if(!stillWaiting || document.body.dataset.screen !== "game") return;
			if(alfi.active && !alfi.seat.timer) alfi.seat.talk("bossWaiting", 1);
			scheduleAlfiWait(nudges + 1);
		}, nudges ? 30000 : 22000);
	}

	function machineToMove() {
		const session = store.session;
		const turn = store.state?.turn;
		if(session?.cpu?.color === turn) return session.cpu;
		return session?.machines?.find((machine) => machine.color === turn) ?? null;
	}

	function scheduleCpu() {
		clearTimeout(cpuTimer);
		const session = store.session;
		if(!(session?.cpu || session?.machines) || store.cpuBusy || store.state?.status !== "ACTIVE") return;
		if(store.state.waitingForOpponent) return;
		if(session.alfi === "hidden") {
			revealAlfi();
			return;
		}
		const machine = machineToMove();
		if(!machine) return;
		const generation = version();
		const backoff = cpuFailures ? Math.min(15000, 1000 * 2 ** cpuFailures) : 0;
		cpuTimer = setTimeout(() => playCpu(generation, machine), 700 + Math.random() * 600 + backoff);
	}


	function wantsCheat(kind) {
		const session = store.session;
		return alfi.active && session?.alfi === "revealed" && store.state?.status === "ACTIVE" &&
			store.state.moves.length >= cleanPlies && Math.random() < cheatChances[kind];
	}

	async function playCpu(generation, machine) {
		if(generation !== version() || store.cpuBusy || store.state?.status !== "ACTIVE" ||
			machineToMove() !== machine) return;
		store.cpuBusy = true;
		try {
			const rival = machine === store.session.cpu;
			if(rival && await alfiFlip()) return;
			const boss = store.session.alfiBoss;
			const double = rival && boss && wantsCheat("double");
			if(generation !== version()) return;
			if(!boss) thinkingLog.start(machineName(store.session), machine.color);
			// Double move: the first engine move keeps the turn on Alfi (PVALFI only), so the second one is an
			// ordinary request-move that hands the turn to the player.
			const move = await playEngineMove(generation, machine, { keepTurn: double });
			// Against a person the raw thinking stays hidden: it would reveal the machine's plans.
			if(generation === version() && !boss) {
				thinkingLog.end(move, machineDetails, { thinking: !store.session.cpu, pluginName: machinePlugin });
				// Between machines the page learns who plays each colour when the server names the plugin.
				if(machinePlugin) store.session.machineNames = { ...store.session.machineNames, [machine.color]: machinePlugin };
			}
			// Only if the server really kept the turn: never ask for an engine move on the player's turn.
			if(move && double && generation === version() && store.state?.status === "ACTIVE" &&
				store.state.turn === machine.color) {
				alfi.cheat("double");
				await wait(700);
				if(generation === version()) await playEngineMove(generation, machine);
			}
		} finally {
			if(generation === version()) {
				store.cpuBusy = false;
				renderGame();
				scheduleCpu();
				schedulePoll(0);
			}
		}
	}

	async function playEngineMove(generation, machine, request = {}) {
		if(generation !== version() || store.state?.status !== "ACTIVE" ||
			store.state.turn !== machine.color) return null;
		const move = await submitMachineMove(request);
		if(generation !== version()) return null;
		cpuFailures = move ? 0 : cpuFailures + 1;
		if(move || !store.session.alfiBoss) return move;
		// A failed reply may still have applied a move. Reconcile before choosing a
		// fallback, and only use fresh legal moves after repeated engine failures.
		try {
			const state = await api.state(store.session.sessionId);
			if(generation !== version()) return null;
			applyGameState(state);
		} catch { return null; }
		if(cpuFailures < silentMachineFailures || store.state?.status !== "ACTIVE" ||
			store.state.turn !== machine.color) return null;
		const options = await legalOptions(api, store.session.sessionId, store.state.pieces, machine.color);
		if(generation !== version() || store.state.turn !== machine.color) return null;
		const fallback = options[Math.floor(Math.random() * options.length)];
		if(fallback && await submitMove(fallback)) {
			cpuFailures = 0;
			return fallback;
		}
		return null;
	}

	async function alfiFlip() {
		const session = store.session;
		if(session.flipped || !wantsCheat("flip") || materialLead(store.state.pieces, session.playerColor) < flipLead) return false;
		// The server swaps who plays each colour; the page only follows once it has. A refusal means no flip.
		const swapped = await api.alfiInvertSeats(session.sessionId).then(() => true, () => false);
		if(!swapped) return false;
		alfi.cheat("flip");
		session.flipped = true;
		[session.playerColor, session.cpu.color] = [session.cpu.color, session.playerColor];
		sessions.persist(session);
		alfi.side = session.cpu.color;
		await wait(600);
		await board.spin();
		await board.colorWave(() => alfi.rebrand(true), () => audio.play("scramble"));
		renderGame();
		return true;
	}

	async function playerMove(command) {
		if(store.cpuBusy) return;
		if(!alfi.active || !store.session?.cpu) return submitMove(command);
		const generation = version();
		store.cpuBusy = true;
		try {
			const dodged = await alfiDodge(command);
			if(generation !== version()) return;
			// A dodge changes the board: it may give check or block the path, so the player's move is checked
			// again. If it is no longer possible it is dropped and the player simply chooses again.
			const move = dodged ? await stillPossible(command) : command;
			if(generation !== version()) return;
			if(!move) {
				store.selection.reset();
				return;
			}
			await submitMove(move);
			if(generation === version()) await alfiRewind(move);
		} finally {
			if(generation === version()) {
				store.cpuBusy = false;
				renderGame();
				scheduleCpu();
				schedulePoll(0);
			}
		}
	}

	// Dodge: the threatened piece makes one of its own legal, non-capturing moves during the player's turn.
	async function alfiDodge(command) {
		const { pieces } = store.state;
		const target = pieces.get(command.to);
		const attacker = pieces.get(command.from);
		// A pawn or king capture may stop being legal once the square is empty, so those are never dodged.
		if(!target || target.color !== store.session.cpu.color || target.type === "KING" ||
			["PAWN", "KING"].includes(attacker?.type) || !wantsCheat("dodge")) return false;
		const escapes = await dodgeEscapes(command.to, target);
		if(!escapes.length) return false;
		const { to, moveId } = escapes[Math.floor(Math.random() * escapes.length)];
		alfi.cheat("dodge");
		await wait(250);
		// A refused cheat stays silent: the player's own move simply goes ahead.
		const dodged = await submitMove({ from: command.to, to, moveId, cheat: true }, { quietFailure: () => true });
		await wait(350);
		return dodged;
	}

	// The player's move after a dodge, with its current moveId, or null when it is no longer legal.
	async function stillPossible(command) {
		try {
			const options = normalizeLegalMoves(await api.legal(store.session.sessionId, command.from)).get(command.to);
			return options?.length ? { ...command, moveId: options[0].moveId } : null;
		} catch {
			return null;
		}
	}

	async function dodgeEscapes(from, piece) {
		let destinations;
		try {
			destinations = normalizeLegalMoves(await api.legal(store.session.sessionId, from, { cheat: true }));
		} catch {
			return [];
		}
		const promotes = (tile) => piece.type === "PAWN" && /[18]$/.test(tile);
		return [...destinations]
			.filter(([tile]) => !store.state.pieces.has(tile) && !promotes(tile))
			.map(([to, [option]]) => ({ to, moveId: option.moveId }));
	}

	async function alfiRewind(command) {
		const moved = store.state?.pieces.get(command.to);
		const last = store.state?.moves.at(-1);
		const castled = moved?.type === "KING" && Math.abs(command.from.charCodeAt(0) - command.to.charCodeAt(0)) > 1;
		if(!moved || last?.capture || command.promotion || castled) return;
		if(store.state.checkedColor !== store.session.cpu.color || !wantsCheat("rewind")) return;
		alfi.cheat("rewind");
		await wait(500);
		await submitMove(teleport(command.to, command.from));
	}

	function alfiEncounters(add = 0) {
		try {
			const count = Number(localStorage.getItem("archchess.alfi.encounters")) || 0;
			if(add) localStorage.setItem("archchess.alfi.encounters", String(count + add));
			return count + add;
		} catch {
			return 0;
		}
	}

	async function revealAlfi() {
		const generation = version();
		const session = store.session;
		alfi.side = session.cpu.color;
		store.cpuBusy = true;
		session.alfi = "revealing";
		gameMascot.silence();
		renderGame();

		try {
			await revealScenes(generation, session);
		} catch (error) {
			// A broken animation must never leave the board locked: skip to the end of the reveal and play on.
			console.error(error);
			if(generation === version()) {
				alfi.abort();
				session.alfi = "revealed";
				sessions.persist(session);
				alfi.apply(true);
			}
		}
		if(generation !== version()) return;
		store.cpuBusy = false;
		renderGame();
		scheduleCpu();
		schedulePoll(0);
		scheduleAlfiWait();
		const comment = profileLine(session.profile);
		if(comment) setTimeout(() => alfi.active && alfi.seat.talk(comment.kind, 2, {}, comment.vars), 5000);
	}

	function revealScenes(generation, session) {
		return alfi.reveal({
			// PVALFI starts on Black's turn: Alfi's first move is an ordinary engine move before he shows up.
			cheat: () => submitMachineMove(),
			alive: () => generation === version(),
			encounters: alfiEncounters(),
			onRevealed: () => {
				session.alfi = "revealed";
				sessions.persist(session);
				alfiEncounters(1);
			},
			// The inversion is only visual: every piece stays where it is and swaps colour on screen, while the
			// seats and the position on the server stay exactly the same.
			swapColors: async (onRow) => {
				await board.colorWave(() => setColorSwap(true), onRow);
				renderGame();
			},
			crownKing: async () => {
				const tile = [...store.state.pieces].find(([, item]) => item.type === "KING" && item.color === session.cpu.color)?.[0];
				if(!tile) return;
				audio.play("capture");
				await board.topple(tile);
				alfi.crown();
				board.render();
				audio.play("promotion");
				await board.dropIn(tile);
				renderGame();
			},
		});
	}


	let machineDetails = {};
	let machinePlugin = null;
	// A machine turn that fails (a plugin that chose a blocked piece, say) is retried by itself: the player only
	// hears about it when it keeps failing.
	const silentMachineFailures = 4;

	async function submitMachineMove(request = {}) {
		const generation = version();
		let played = null;
		machineDetails = {};
		machinePlugin = null;
		await sendMove(async () => {
			const { move, details, plugin, state } = await api.machineMove(store.session.sessionId, request);
			if(generation !== version()) return state;
			played = move;
			machineDetails = store.session.alfiBoss ? {} : details;
			machinePlugin = store.session.alfiBoss ? null : plugin;
			return state;
		}, { quietFailure: () => cpuFailures < silentMachineFailures });
		return played;
	}


	function reset() {
		clearTimeout(cpuTimer);
		clearTimeout(alfiWaitTimer);
		cpuFailures = 0;
		alfi.abort();
	}
	return { scheduleCpu, scheduleAlfiWait, playerMove, reset };
}
