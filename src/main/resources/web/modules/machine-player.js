import { wait } from "./ui.js";
import { setColorSwap } from "./colors.js";
import { legalOptions, outOfTurnPawnPush } from "./cpu.js";
import { cheatChances, cleanPlies, flipLead, freeNeighbours, materialLead, teleport } from "./cheats.js";
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
			if(alfi.active && !alfi.seat.timer) {
				alfi.followGame(store.state, true);
				alfi.seat.talk("bossWaiting", 1);
			}
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
			const double = rival && wantsCheat("double");
			if(generation !== version()) return;
			const boss = store.session.alfiBoss;
			if(!boss) thinkingLog.start(machineName(store.session), machine.color);
			const move = await playEngineMove(generation, machine);
			// Against a person the raw thinking stays hidden: it would reveal the machine's plans.
			if(generation === version() && !boss) {
				thinkingLog.end(move, machineDetails, { thinking: !store.session.cpu, pluginName: machinePlugin });
				// Between machines the page learns who plays each colour when the server names the plugin.
				if(machinePlugin) store.session.machineNames = { ...store.session.machineNames, [machine.color]: machinePlugin };
			}
			// request-move always uses the authoritative side to move. A second engine
			// move needs backend turn control (contract §7); never ask on the human's turn.
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

	async function playEngineMove(generation, machine) {
		if(generation !== version() || store.state?.status !== "ACTIVE" ||
			store.state.turn !== machine.color) return null;
		const move = await submitMachineMove();
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
			await alfiDodge(command);
			if(generation !== version()) return;
			await submitMove(command);
			if(generation === version()) await alfiRewind(command);
		} finally {
			if(generation === version()) {
				store.cpuBusy = false;
				renderGame();
				scheduleCpu();
				schedulePoll(0);
			}
		}
	}

	async function alfiDodge(command) {
		const target = store.state.pieces.get(command.to);
		if(!target || target.color !== store.session.cpu.color || target.type === "KING" || !wantsCheat("dodge")) return;
		const free = freeNeighbours(store.state.pieces, command.to).filter((tile) => tile !== command.from);
		if(!free.length) return;
		alfi.cheat("dodge");
		await wait(250);
		await submitMove(teleport(command.to, free[Math.floor(Math.random() * free.length)]));
		await wait(350);
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

		await alfi.reveal({
			cheat: async () => {
				const move = outOfTurnPawnPush(store.state.pieces, session.cpu.color);
				if(move) await submitMove(move);
			},
			alive: () => generation === version(),
			encounters: alfiEncounters(),
			onRevealed: () => {
				session.alfi = "revealed";
				sessions.persist(session);
				alfiEncounters(1);
			},
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
		if(generation !== version()) return;
		store.cpuBusy = false;
		renderGame();
		scheduleCpu();
		schedulePoll(0);
		scheduleAlfiWait();
		const comment = profileLine(session.profile);
		if(comment) setTimeout(() => alfi.active && alfi.seat.talk(comment.kind, 2, {}, comment.vars), 5000);
	}


	let machineDetails = {};
	let machinePlugin = null;
	// A machine turn that fails (a plugin that chose a blocked piece, say) is retried by itself: the player only
	// hears about it when it keeps failing.
	const silentMachineFailures = 4;

	async function submitMachineMove() {
		const generation = version();
		let played = null;
		machineDetails = {};
		machinePlugin = null;
		await sendMove(async () => {
			const { move, details, plugin, state } = await api.machineMove(store.session.sessionId);
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
