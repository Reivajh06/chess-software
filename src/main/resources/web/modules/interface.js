import { gamePiece, pieceImage, variantTag, uiIcon } from "./assets.js";
import { shownColor } from "./colors.js";
import { gameModes, menuOrder, modeDefinition } from "./modes.js";
import { labels, pieceValues } from "./state.js";
import { themes } from "./themes.js";
import { loadAccount } from "./account.js";
import { accountIdentity, pluginIdentity, toneFor } from "./identity.js";
import { displayName, subName } from "./thinking.js";

const byId = (id) => document.getElementById(id);
const statusLabels = {
	ACTIVE: "En juego",
	WHITE_WIN: "Ganan las blancas",
	BLACK_WIN: "Ganan las negras",
	DRAW: "Tablas",
};
const statusLabel = (status) => {
	const winner = { WHITE_WIN: "WHITE", BLACK_WIN: "BLACK" }[status];
	return winner ? statusLabels[`${shownColor(winner)}_WIN`] : statusLabels[status];
};

export class TitleScreen {
	constructor({ onStart, onJoin, onHover, onError }) {
		this.onStart = onStart;
		this.onError = onError;
		this.onHover = onHover;
		this.onJoin = onJoin;
		this.busy = false;

		byId("title-menu").addEventListener("keydown", (event) => this.navigate(event));
		byId("join-back").addEventListener("click", () => this.showMenu());
		byId("join-form").addEventListener("submit", (event) => {
			event.preventDefault();
			if(!this.busy) this.onJoin(byId("join-id").value);
		});
	}

	entries() {
		const entries = [];
		for(const id of menuOrder) {
			const mode = gameModes[id];
			entries.push({
				id,
				label: mode.name,
				icon: `mode-${id.toLowerCase()}`,
				detail: mode.available ? mode.detail : "Próximamente",
				disabled: !mode.available,
				title: mode.reason,
				hint: mode.hint,
				action: () => this.onStart(id),
			});
		}
		entries.push({
			id: "join",
			label: "Tengo una invitación",
			hint: "¿Tienes un enlace? Pégalo. Prometo no abrir un asistente.",
			action: () => this.showJoin(),
		});
		return entries;
	}

	render() {
		const fragment = document.createDocumentFragment();
		for(const entry of this.entries()) {
			const button = document.createElement("button");
			button.type = "button";
			button.className = "title-option";
			button.dataset.entry = entry.id;
			button.disabled = this.busy || !!entry.disabled;
			const loading = this.busy && entry.id === this.pending;
			button.classList.toggle("is-loading", loading);
			if(entry.title) button.title = entry.title;
			if(entry.icon) button.append(uiIcon(entry.icon, "title-option-art"));
			const text = document.createElement("span");
			const label = document.createElement("strong");
			label.textContent = entry.label;
			text.append(label);
			if(entry.detail || loading) {
				const detail = document.createElement("small");
				detail.textContent = loading ? "Preparando el tablero" : entry.detail;
				text.append(detail);
			}
			button.append(text);
			button.addEventListener("click", entry.action);
			if(entry.hint) {
				const hover = () => this.onHover?.(entry.id, entry.hint);
				button.addEventListener("pointerenter", hover);
				button.addEventListener("focus", hover);
			}
			fragment.append(button);
		}
		byId("title-menu").replaceChildren(fragment);
	}

	focus() {
		const target = byId("join-form").hidden
			? byId("title-menu").querySelector(".title-option:not(:disabled)")
			: byId("join-id");
		target?.focus({ preventScroll: true });
	}

	navigate(event) {
		const step = { ArrowDown: 1, ArrowUp: -1 }[event.key];
		if(!step) return;
		event.preventDefault();
		const options = [...byId("title-menu").querySelectorAll(".title-option:not(:disabled)")];
		const index = options.indexOf(document.activeElement);
		options[(index + step + options.length) % options.length]?.focus();
	}

	showMenu() {
		byId("join-form").hidden = true;
		byId("title-menu").hidden = false;
		this.setError();
		this.focus();
	}

	showJoin(value = "") {
		byId("title-menu").hidden = true;
		byId("join-form").hidden = false;
		byId("join-id").value = value;
		this.setError();
		this.focus();
	}

	setBusy(busy, pending = null) {
		this.busy = busy;
		this.pending = busy ? pending : null;
		byId("join-submit").disabled = busy;
		byId("join-submit").textContent = busy ? "Conectando…" : "Unirse";
		this.render();
	}

	setError(message = "") {
		byId("title-error").textContent = message;
		byId("title-error").hidden = !message;
		if(message) this.onError?.(message);
	}
}

const modeIcons = {
	PVP: "mode-pvp",
	PVAI: "mode-pvai",
	AIVAI: "mode-aivai",
	PVCPU: "mode-pvcpu",
	PRACTICE: "mode-practice",
	LOCAL: "mode-pvp",
	SPECTATE: "mode-aivai",
};

const rendered = new Map();
function changed(id, key) {
	if(rendered.get(id) === key) return false;
	rendered.set(id, key);
	return true;
}
function setText(id, text) {
	const element = byId(id);
	if(element.textContent !== text) element.textContent = text;
}

export function populateThemeGrid(container, onPick) {
	for(const [id, theme] of Object.entries(themes)) {
		const button = document.createElement("button");
		button.type = "button";
		button.className = "choice theme-choice";
		button.dataset.value = id;
		button.setAttribute("role", "radio");
		button.style.setProperty("--swatch-light", theme.colors.light);
		button.style.setProperty("--swatch-dark", theme.colors.dark);
		button.style.setProperty("--swatch-accent", theme.colors.accent);
		const swatch = document.createElement("span");
		swatch.className = "theme-swatch";
		swatch.append(pieceImage("KING", "WHITE", id));
		const name = document.createElement("span");
		name.className = "choice-name";
		name.textContent = theme.name;
		button.append(swatch, name);
		button.addEventListener("click", () => onPick(id));
		container.append(button);
	}
}

export function markChoice(container, value) {
	for(const button of container.children) {
		const selected = button.dataset.value === value;
		button.setAttribute("aria-checked", String(selected));
		button.tabIndex = selected ? 0 : -1;
	}
}

export function renderAppearance(preferences) {
	const sound = byId("sound-toggle");
	sound.setAttribute("aria-pressed", String(preferences.muted));
	sound.setAttribute("aria-label", preferences.muted ? "Activar sonido" : "Silenciar sonido");
	sound.classList.toggle("is-muted", preferences.muted);
	const music = byId("music-toggle");
	music.setAttribute("aria-pressed", String(preferences.music));
	music.setAttribute("aria-label", preferences.music ? "Desactivar música" : "Activar música");
	music.classList.toggle("is-muted", !preferences.music || preferences.muted);
}

export function renderMatch(store, preferences) {
	const session = store.session;
	const state = store.state;
	const watching = !!session?.watching; // somebody else's game, followed from an invitation to watch
	const spectator = session?.gamemode === "AIVAI" || watching;
	const mode = modeDefinition(session?.gamemode);
	const shared = store.practice || store.local;
	const color = shared ? store.orientation : (session?.playerColor ?? "WHITE");
	const rival = color === "WHITE" ? "BLACK" : "WHITE";
	const colorName = (value) => (shownColor(value) === "WHITE" ? "Blancas" : "Negras");
	const finished = ["WHITE_WIN", "BLACK_WIN", "DRAW"].includes(state?.status);
	const check = state?.status === "ACTIVE" && !!state.checkedColor;
	const currentStatus = !session
		? "Sin partida"
		: state?.waitingForOpponent
			? "Esperando oponente"
			: check
				? "¡Jaque!"
				: (statusLabel(state?.status) ?? "Conectando");
	const reviewing = store.reviewPly !== null;

	byId("review-banner").hidden = !reviewing;
	setText("review-label", !reviewing
		? ""
		: store.reviewPly === 0
			? "Posición inicial"
			: `Jugada ${store.reviewPly} de ${state?.moves.length ?? 0}`);
	setText("move-count", String(state?.moves.length ?? 0));
	byId("session-details").hidden = !session;
	setText("session-id", session?.sessionId ?? "");
	byId("invite").hidden = !session || session.gamemode !== "PVP";
	byId("watch-invite").hidden = !session || store.practice || store.local;
	setText("arena-mode", session ? mode.badge : "—");
	setText("arena-name", session ? mode.name : "Sin partida");
	setText("arena-status", currentStatus);
	byId("arena-status").dataset.alert = String(check);
	const icon = `./assets/ui/${modeIcons[session?.gamemode] ?? "mode-practice"}.png`;
	if(byId("arena-icon").getAttribute("src") !== icon) byId("arena-icon").setAttribute("src", icon);

	if(changed("avatars", `${color}:${preferences.theme}${variantTag("KING", rival)}`)) {
		byId("player-avatar").replaceChildren(gamePiece("KING", color, preferences.theme));
		byId("opponent-avatar").replaceChildren(gamePiece("KING", rival, preferences.theme));
	}
	// Who is who. A plugin shows its model first ("qwen3:14b" over "Ollama"); between machines the page learns the
	// plugin of each colour when the server names it (session.machineNames).
	const machineNames = session?.machineNames ?? {};
	const versusMachine = !!session?.cpu; // PvCPU and PvAI: one machine, named from the start
	const machine = session?.pluginNames?.[0];
	const title = (name, fallback) => (name ? displayName(name) : fallback);
	const detail = (name, side) => [name ? subName(name) : "", colorName(side)].filter(Boolean).join(" · ");

	setText("player-name", watching
		? colorName(color)
		: spectator
			? title(machineNames[color], state?.whiteModel ?? "Modelo blanco")
			: shared
				? colorName(color)
				: "Tú");
	setText("opponent-name", watching
		? colorName(rival)
		: spectator
			? title(machineNames[rival], state?.blackModel ?? "Modelo negro")
			: shared
				? colorName(rival)
				: session?.gamemode === "PVAI"
					? title(machine, "IA local")
					: versusMachine
						? (session.alfi === "revealed" ? "Alfi" : title(machine, "Stockfish"))
						: "Oponente");
	setText("player-detail", !session
		? ""
		: store.practice
			? "Práctica libre"
			: store.local
				? "En este equipo"
				: spectator && !watching
					? detail(machineNames[color], color)
					: colorName(color));
	setText("opponent-detail", !session
		? ""
		: state?.waitingForOpponent
			? "Esperando invitación"
			: store.practice
				? "Práctica libre"
				: store.local
					? "En este equipo"
					: spectator && !watching
						? detail(machineNames[rival], rival)
						: versusMachine
							? detail(machine, rival)
							: colorName(rival));

	// Identity colours (identity.js): plugins and guests draw a random one for each game, an account keeps its own.
	const account = loadAccount();
	const humanIdentity = account ? accountIdentity(account.username) : "guest";
	const identityOf = (side) => {
		if(watching) return `watching:${side}`;
		if(spectator) return pluginIdentity(machineNames[side]) ?? `machine:${side}`;
		if(versusMachine) return side === color ? humanIdentity : (pluginIdentity(machine) ?? "machine");
		if(shared) return side === color ? humanIdentity : "guest:2";
		return side === color ? humanIdentity : "rival";
	};
	document.querySelector('.player[data-side="player"]').style.setProperty("--tone", toneFor(identityOf(color)));
	document.querySelector('.player[data-side="opponent"]').style.setProperty("--tone", toneFor(identityOf(rival)));

	const active = !!session && state?.status === "ACTIVE" && !reviewing;
	byId("player-turn").hidden = !active || state.turn !== color;
	byId("opponent-turn").hidden = !active || state.turn !== rival;
	document.querySelector('.player[data-side="player"]').classList.toggle("to-move", !byId("player-turn").hidden);
	document.querySelector('.player[data-side="opponent"]').classList.toggle("to-move", !byId("opponent-turn").hidden);
	renderTurn(state, session, finished, reviewing, check, preferences);

	byId("spectator-banner").hidden = !spectator;
	// The machines' turn log: only in games against machines, and not with Alfi, who has his own stage.
	byId("thinking-tab").hidden = !((session?.cpu || session?.machines) && !session?.alfi);
	setText("thinking-status", state?.thinkingColor
		? `Pensando: ${state.thinkingColor === "WHITE" ? "blancas" : "negras"}`
		: watching ? "Viendo la partida" : "Partida entre modelos");
	renderCaptured("player-captured", state?.eatenPieces?.[rival], preferences);
	renderCaptured("opponent-captured", state?.eatenPieces?.[color], preferences);
	renderMaterial(store.pieces, color);
	renderLastMove(state?.moves[(store.reviewPly ?? state?.moves.length ?? 0) - 1], preferences);
	renderClock("player-clock", state?.clocks?.[color], state?.turn === color);
	renderClock("opponent-clock", state?.clocks?.[rival], state?.turn === rival);
	const moves = state?.moves.length ?? 0;
	const ply = store.reviewPly ?? moves;
	byId("history-start").disabled = !moves || ply === 0;
	byId("history-prev").disabled = !moves || ply === 0;
	byId("history-next").disabled = !reviewing;
	byId("history-live").disabled = !reviewing;
}

function renderTurn(state, session, finished, reviewing, check, preferences) {
	const card = document.querySelector(".turn-card");
	const turn = state?.turn ?? "WHITE";
	const winner = state?.status === "WHITE_WIN" ? "WHITE" : state?.status === "BLACK_WIN" ? "BLACK" : null;
	const label = !session
		? "—"
		: finished
			? statusLabel(state.status)
			: reviewing
				? "Revisando"
				: state?.waitingForOpponent
					? "Esperando rival"
					: shownColor(turn) === "WHITE" ? "Blancas" : "Negras";
	setText("turn-eyebrow", finished ? "Resultado" : check && !reviewing ? "¡Jaque! Mueven" : "Mueven");
	setText("turn-name", label);
	card.dataset.turn = !session || finished || reviewing ? "" : shownColor(turn);
	card.dataset.check = String(check && !reviewing);
	const avatarColor = finished ? winner : session && !reviewing ? turn : null;
	if(changed("turn-avatar", `${avatarColor}:${preferences.theme}${variantTag("KING", avatarColor)}`)) {
		byId("turn-avatar").replaceChildren(
			...(avatarColor ? [gamePiece("KING", avatarColor, preferences.theme)] : []),
		);
	}
}

function renderLastMove(move, preferences) {
	byId("last-move-card").hidden = !move;
	if(!move) return;
	const name = labels[move.type] ?? "pieza";
	setText("last-move-route", `${move.from ?? "?"} → ${move.to ?? "?"}`);
	const captured = move.capturedPiece;
	setText("last-move-detail", move.capture
		? `${name[0].toUpperCase()}${name.slice(1)} captura ${captured ? labels[captured.type] : "pieza"}`
		: `${name[0].toUpperCase()}${name.slice(1)} ${shownColor(move.color) === "WHITE" ? "blanco" : "negro"}`);
	byId("last-move-card").classList.toggle("is-capture", !!move.capture);
	if(changed("last-move-piece", `${move.type}:${move.color}:${preferences.theme}${variantTag(move.type, move.color)}`)) {
		byId("last-move-piece").replaceChildren(gamePiece(move.type, move.color, preferences.theme));
	}
}

export function materialBalance(pieces) {
	const totals = { WHITE: 0, BLACK: 0 };
	for(const item of pieces?.values() ?? []) totals[item.color] += pieceValues[item.type] ?? 0;
	return { totals, lead: totals.WHITE - totals.BLACK };
}

function renderMaterial(pieces, color) {
	const { totals, lead } = materialBalance(pieces);
	const shownLead = shownColor("WHITE") === "WHITE" ? lead : -lead;
	const share = 50 + Math.max(-10, Math.min(10, shownLead)) * 4.5;
	byId("material-white").style.width = `${share}%`;
	setText("score-lead", !shownLead ? "Igualado" : `${shownLead > 0 ? "Blancas" : "Negras"} +${Math.abs(shownLead)}`);
	const rival = color === "WHITE" ? "BLACK" : "WHITE";
	for(const [id, side, other] of [["player-advantage", color, rival], ["opponent-advantage", rival, color]]) {
		const diff = totals[side] - totals[other];
		byId(id).hidden = diff <= 0;
		setText(id, diff > 0 ? `+${diff}` : "");
	}
}

function renderCaptured(id, pieces, preferences) {
	const list = Array.isArray(pieces) ? pieces : [];
	if(!changed(id, `${preferences.theme}${variantTag("", "")}:${list.map((piece) => piece.color + piece.type).join(",")}`)) return;
	const order = ["QUEEN", "ROOK", "BISHOP", "KNIGHT", "PAWN", "KING"];
	byId(id).replaceChildren(
		...[...list]
			.sort((a, b) => order.indexOf(a.type) - order.indexOf(b.type))
			.map((piece) => gamePiece(piece.type, piece.color, preferences.theme)),
	);
}

function renderClock(id, seconds, active) {
	const element = byId(id);
	element.hidden = !Number.isFinite(seconds);
	if(element.hidden) return;
	const remaining = Math.max(0, Math.floor(seconds));
	setText(id, `${Math.floor(remaining / 60)}:${String(remaining % 60).padStart(2, "0")}`);
	element.classList.toggle("active", active);
}
