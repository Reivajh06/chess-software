import { ApiError, HttpStatus } from "./api.js";
import { pieceImage } from "./assets.js";
import { fillGlyphs, glyph } from "./glyphs.js";
import { ReplayPlayer } from "./replay.js";

const byId = (id) => document.getElementById(id);

const modeNames = {
	PVP: "Contra una persona",
	PRACTICE: "Práctica",
	PVCPU: "Contra la CPU",
	PVAI: "Contra una IA",
	AIVAI: "IA contra IA",
};

const filters = [
	{ id: "ALL", label: "Todas", icon: null, modes: null },
	{ id: "PVP", label: "PvP", icon: "pvp", modes: ["PVP"] },
	{ id: "PRACTICE", label: "Práctica", icon: "practice", modes: ["PRACTICE"] },
	{ id: "MACHINE", label: "CPU e IA", icon: "pvcpu", modes: ["PVCPU", "PVAI", "AIVAI"] },
];

const results = {
	WIN: { label: "Victoria", tone: "win", glyph: "trophy" },
	LOSS: { label: "Derrota", tone: "loss", glyph: "cross" },
	DRAW: { label: "Tablas", tone: "draw", glyph: "equal" },
	WHITE_WIN: { label: "Ganan blancas", tone: "neutral", glyph: "trophy" },
	BLACK_WIN: { label: "Ganan negras", tone: "neutral", glyph: "trophy" },
	ACTIVE: { label: "Sin terminar", tone: "neutral", glyph: "hourglass" },
};

function element(tag, className, text) {
	const node = document.createElement(tag);
	if(className) node.className = className;
	if(text !== undefined) node.textContent = text;
	return node;
}

function modeIcon(name) {
	const image = element("img", "mode-icon-image");
	image.src = `./assets/ui/mode-${name}.png`;
	image.alt = "";
	return image;
}

function playerColor(game, username) {
	if(["PRACTICE", "AIVAI"].includes(game.gamemode)) return null;

	const move = game.moves.find(move => move.player === username);

	if(move) return move.ply % 2 === 1 ? "WHITE" : "BLACK";
	if(game.moves.length > 0) {
		return game.moves[0].ply % 2 === 1 ? "BLACK" : "WHITE";
	}

	return null;
}

function resultOf(game, color) {
	if(game.status === "DRAW") return results.DRAW;
	if(game.status === "ACTIVE" || !color) return results[game.status] ?? results.ACTIVE;

	return game.status === `${color}_WIN` ? results.WIN : results.LOSS;
}

function rivalOf(game, username) {
	if(game.gamemode === "PRACTICE") return "Tú contra ti";

	const rival = game.creator === username ? game.rival : game.creator;

	return rival ?? "Sin rival";
}

function sides(game, username, color) {
	if(game.gamemode === "PRACTICE") return { white: username, black: username };

	const rival = rivalOf(game, username);

	const white = game.moves.find(move => move.ply % 2 === 1)?.player;
	const black = game.moves.find(move => move.ply % 2 === 0)?.player;

	if(color === "BLACK") return { white: white ?? rival, black: username };
	if(color === "WHITE") return { white: username, black: black ?? rival };

	return { white: white ?? game.creator, black: black ?? rival };
}

function sameDay(first, second) {
	return first.toDateString() === second.toDateString();
}

function formatDate(value) {
	const date = new Date(value);

	if(Number.isNaN(date.getTime())) return "—";

	const today = new Date();
	const yesterday = new Date();
	yesterday.setDate(today.getDate() - 1);

	const time = date.toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit" });

	if(sameDay(date, today)) return `Hoy, ${time}`;
	if(sameDay(date, yesterday)) return `Ayer, ${time}`;

	return `${date.toLocaleDateString("es-ES", { day: "numeric", month: "short", year: "numeric" })}, ${time}`;
}

function playingSince(games) {
	if(games.length === 0) return "Aún no has jugado ninguna partida.";

	const date = new Date(games.at(-1).createdAt);

	return `Jugando desde ${date.toLocaleDateString("es-ES", { month: "long", year: "numeric" })}`;
}

function movesText(count) {
	return count === 1 ? "1 jugada" : `${count} jugadas`;
}

export class ProfileScreen {

	constructor(api, preferences, { onDeleteAccount, onSignIn }) {
		this.api = api;
		this.preferences = preferences;
		this.replay = new ReplayPlayer(preferences);
		this.account = null;
		this.games = [];
		this.filter = filters[0];
		this.request = 0;

		byId("profile-delete").addEventListener("click", onDeleteAccount);
		byId("profile-signin").addEventListener("click", onSignIn);
		byId("profile-retry").addEventListener("click", () => this.load());

		fillGlyphs(byId("profile-screen"));
		this.renderFilters();
	}

	open(account) {
		this.account = account;
		this.games = [];
		this.filter = filters[0];
		this.renderStats();
		this.renderFilters();
		this.renderList();
		byId("profile-mark").replaceChildren(pieceImage("KING", "WHITE", this.preferences.theme));
		byId("profile-name").textContent = account.username;
		this.load();
	}

	async load() {
		const request = ++this.request;
		this.setState("loading");

		try {
			const history = await this.api.history();
			if(request !== this.request) return;
			this.showGames(history.sessions);
		} catch (error) {
			if(request !== this.request || error.name === "AbortError") return;
			this.showError(error);
		}
	}

	showGames(sessions) {
		const username = this.account.username;

		this.games = sessions.map(game => {
			const color = playerColor(game, username);
			return {
				...game,
				...sides(game, username, color),
				color,
				result: resultOf(game, color),
				rival: rivalOf(game, username),
				modeName: modeNames[game.gamemode] ?? game.gamemode,
			};
		});

		this.setState(this.games.length > 0 ? "ready" : "empty");
		this.renderStats();
		this.renderList();
	}

	showError(error) {
		if(error instanceof ApiError && error.status === HttpStatus.FORBIDDEN) return this.setState("expired");

		byId("profile-error-text").textContent = "No se ha podido cargar tu historial. Inténtalo de nuevo.";
		this.setState("error");
	}

	setState(state) {
		byId("profile-screen").dataset.state = state;
	}

	renderStats() {
		const count = (result) => this.games.filter(game => game.result === result).length;

		byId("profile-since").textContent = playingSince(this.games);
		byId("stat-games").textContent = this.games.length;
		byId("stat-wins").textContent = count(results.WIN);
		byId("stat-losses").textContent = count(results.LOSS);
		byId("stat-draws").textContent = count(results.DRAW);
	}

	renderFilters() {
		byId("profile-filters").replaceChildren(...filters.map(filter => this.filterButton(filter)));
	}

	filterButton(filter) {
		const button = element("button", "profile-filter");
		button.type = "button";
		button.dataset.mode = filter.id;
		button.setAttribute("aria-pressed", String(filter === this.filter));
		if(filter.icon) button.append(modeIcon(filter.icon));
		button.append(filter.label);
		button.addEventListener("click", () => this.selectFilter(filter));
		return button;
	}

	selectFilter(filter) {
		this.filter = filter;

		for(const button of byId("profile-filters").children) {
			button.setAttribute("aria-pressed", String(button.dataset.mode === filter.id));
		}

		this.renderList();
	}

	visibleGames() {
		if(this.filter.modes === null) return this.games;

		return this.games.filter(game => this.filter.modes.includes(game.gamemode));
	}

	renderList() {
		const games = this.visibleGames();

		byId("profile-games-empty").hidden = games.length > 0 || this.games.length === 0;
		byId("profile-games").replaceChildren(...games.map(game => this.gameItem(game)));
	}

	gameItem(game) {
		const item = element("li");
		const button = element("button", "profile-game");
		button.type = "button";
		button.setAttribute("aria-label", `Ver repetición: ${game.modeName} contra ${game.rival}, ${game.result.label}`);

		const details = element("span", "profile-game-details");
		details.append(
			element("strong", "profile-game-rival", game.rival),
			element("span", "profile-game-meta", `${game.modeName} · ${formatDate(game.createdAt)} · ${movesText(game.moves.length)}`),
		);

		const side = element("span", "profile-game-side");
		if(game.color) side.append(pieceImage("KING", game.color, this.preferences.theme));
		side.title = game.color === "WHITE" ? "Jugaste con blancas" : game.color === "BLACK" ? "Jugaste con negras" : "";

		const result = element("span", `result-badge tone-${game.result.tone}`);
		result.append(glyph(game.result.glyph), game.result.label);

		const play = element("span", "profile-game-play");
		play.append(glyph("play"), element("span", "", "Repetir"));

		button.append(modeIcon(game.gamemode.toLowerCase()), details, side, result, play);
		button.addEventListener("click", () => this.replay.open(game));
		item.append(button);
		return item;
	}
}
