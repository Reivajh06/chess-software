import { square } from "./squares.js";

export const Phase = Object.freeze({
	IDLE: "idle",
	LOADING: "loading",
	SELECTED: "selected",
	CHOOSING: "choosing",
	MOVING: "moving",
});

const IGNORE = Object.freeze({ type: "ignore" });

export function boardInteractive(session, state) {
	if(!session || !state) return false;
	if(state.status !== "ACTIVE" || state.waitingForOpponent) return false;
	return session.gamemode !== "AIVAI";
}

export function normalizeLegalMoves(payload) {
	if(!payload || !Array.isArray(payload.moves)) {
		throw new Error("El servidor no ha proporcionado movimientos legales válidos.");
	}

	const byDestination = new Map();
	const seenIds = new Set();

	for(const rule of payload.moves) {
		if(
				!rule ||
				!Number.isSafeInteger(rule.moveId) ||
				rule.moveId < 0 ||
				!Array.isArray(rule.tiles) ||
				seenIds.has(rule.moveId)
		) {
			throw new Error("El servidor ha enviado un identificador de movimiento no válido.");
		}

		seenIds.add(rule.moveId);

		if(rule.label !== undefined && typeof rule.label !== "string") {
			throw new Error("El servidor ha enviado una descripción de movimiento no válida.");
		}

		for(const rawTile of rule.tiles) {
			const tile = square(rawTile);

			if(!tile) {
				throw new Error("El servidor ha enviado una casilla legal no válida.");
			}

			if(!byDestination.has(tile)) {
				byDestination.set(tile, []);
			}

			const options = byDestination.get(tile);

			if(!options.some((option) => option.moveId === rule.moveId)) {
				options.push({ moveId: rule.moveId, label: rule.label ?? null });
			}
		}
	}

	return byDestination;
}

function isPromotion(piece, to) {
	return piece?.type === "PAWN" && (to[1] === "1" || to[1] === "8");
}

export class Selection {
	constructor() {
		this.token = 0;
		this.reset();
	}

	reset() {
		this.token++;
		this.phase = Phase.IDLE;
		this.from = null;
		this.to = null;
		this.options = new Map();
	}

	get selected() {
		return this.phase === Phase.IDLE ? null : this.from;
	}

	get targets() {
		return this.phase === Phase.SELECTED || this.phase === Phase.CHOOSING
				? new Set(this.options.keys())
				: new Set();
	}

	get busy() {
		return this.phase === Phase.MOVING;
	}

	click(tile, { pieces, interactive }) {
		if(this.phase === Phase.MOVING || this.phase === Phase.CHOOSING) return IGNORE;

		if(this.phase === Phase.SELECTED && this.options.has(tile)) {
			return this.#target(tile, pieces.get(this.from));
		}

		if(this.phase !== Phase.IDLE && tile === this.from) {
			this.reset();
			return { type: "deselect" };
		}

		const piece = pieces.get(tile);

		if(piece && interactive) {
			this.reset();
			this.phase = Phase.LOADING;
			this.from = tile;
			return { type: "load", from: tile, token: this.token };
		}

		if(this.phase === Phase.IDLE) return IGNORE;

		this.reset();
		return { type: "deselect" };
	}

	loaded(token, payload) {
		if(this.phase !== Phase.LOADING || token !== this.token) return false;
		this.options = normalizeLegalMoves(payload);
		this.phase = Phase.SELECTED;
		return true;
	}

	failed(token) {
		if(this.phase !== Phase.LOADING || token !== this.token) return false;
		this.reset();
		return true;
	}

	choose(moveId) {
		if(this.phase !== Phase.CHOOSING) return null;
		const options = this.options.get(this.to) ?? [];
		if(!options.some((option) => option.moveId === moveId)) return null;
		return this.#move(this.to, moveId);
	}

	promote(newType) {
		if(this.phase !== Phase.CHOOSING) return null;
		const [option] = this.options.get(this.to) ?? [];
		if(!option) return null;
		return { ...this.#move(this.to, option.moveId), promotion: newType };
	}

	cancelChoice() {
		if(this.phase !== Phase.CHOOSING) return false;
		this.phase = Phase.SELECTED;
		this.to = null;
		return true;
	}

	#target(to, piece) {
		if(isPromotion(piece, to)) {
			this.phase = Phase.CHOOSING;
			this.to = to;
			return { type: "promote", from: this.from, to };
		}

		const options = this.options.get(to);

		if(options.length > 1) {
			this.phase = Phase.CHOOSING;
			this.to = to;
			return { type: "choose", from: this.from, to, options };
		}

		return this.#move(to, options[0].moveId);
	}

	#move(to, moveId) {
		this.phase = Phase.MOVING;
		this.to = to;
		return { type: "move", from: this.from, to, moveId };
	}
}
