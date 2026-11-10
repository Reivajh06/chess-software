import { modeLabel, modeIcon as iconName } from "./modes.js";
import { byId, element } from "./ui.js";
import { ApiError, HttpStatus } from "./api.js";
import { pieceImage } from "./assets.js";
import { fillGlyphs, glyph } from "./glyphs.js";
import { ReplayPlayer } from "./replay.js";
import { Mascot } from "./mascot.js";
import { labels } from "./state.js";
import { profileInsights, streak } from "./profile-insights.js";

const toneLabels = { win: "Wins", loss: "Losses", draw: "Draws" };
const toneGlyphs = { win: "trophy", loss: "cross", draw: "equal" };
const toneLetters = { win: "W", loss: "L", draw: "D" };
const toneSingle = { win: "Win", loss: "Loss", draw: "Draw" };
const pieceName = (piece) => (labels[piece] ?? piece).toLowerCase();
const percent = (part, whole) => (whole ? Math.round((part / whole) * 100) : 0);
const alfiCycle = 6000;
const alfiResume = 4000;


const filters = [
	{ id: "ALL", label: "All", icon: null, modes: null },
	{ id: "PVP", label: "PvP", icon: "pvp", modes: ["PVP"] },
	{ id: "PRACTICE", label: "Practice", icon: "practice", modes: ["PRACTICE"] },
	{ id: "MACHINE", label: "Machines", icon: "pvcpu", modes: ["PVCPU", "PVAI", "AIVAI"] },
];

const results = {
	WIN: { label: "Victory", tone: "win", glyph: "trophy" },
	LOSS: { label: "Defeat", tone: "loss", glyph: "cross" },
	DRAW: { label: "Draw", tone: "draw", glyph: "equal" },
	WHITE_WIN: { label: "White wins", tone: "neutral", glyph: "trophy" },
	BLACK_WIN: { label: "Black wins", tone: "neutral", glyph: "trophy" },
	ACTIVE: { label: "Unfinished", tone: "neutral", glyph: "hourglass" },
};


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
	if(game.gamemode === "PRACTICE") return "You vs yourself";

	const rival = game.creator === username ? game.rival : game.creator;

	return rival ?? "No opponent";
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

	const time = date.toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" });

	if(sameDay(date, today)) return `Today, ${time}`;
	if(sameDay(date, yesterday)) return `Yesterday, ${time}`;

	return `${date.toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" })}, ${time}`;
}

function playingSince(games) {
	if(games.length === 0) return "You haven't played any games yet.";

	const date = new Date(games.at(-1).createdAt);

	return `Playing since ${date.toLocaleDateString("en-GB", { month: "long", year: "numeric" })}`;
}

function movesText(count) {
	return count === 1 ? "1 move" : `${count} moves`;
}

export class ProfileScreen {

	constructor(api, preferences, { onDeleteAccount, onSignIn, audio }) {
		this.api = api;
		this.preferences = preferences;
		this.replay = new ReplayPlayer(preferences, audio);
		this.account = null;
		this.games = [];
		this.filter = filters[0];
		this.request = 0;
		this.insights = null;
		this.comments = new WeakMap();
		this.alfi = new Mascot(byId("profile-mascot"), audio, preferences,
			{ idle: false, pokes: { poke: "profilePoke", annoyed: "profilePoke" } });
		this.idleTopic = 0;

		byId("profile-delete").addEventListener("click", onDeleteAccount);
		byId("profile-signin").addEventListener("click", onSignIn);
		byId("profile-retry").addEventListener("click", () => this.load());

		const screen = byId("profile-screen");
		fillGlyphs(screen);
		this.renderFilters();
		this.watchComments(screen);
		setInterval(() => this.idleComment(), alfiCycle);
	}

	// Alfi reacts to whatever the pointer (or keyboard focus) lands on, then goes back to cycling.
	watchComments(screen) {
		const react = (event) => {
			const target = event.target.closest?.("[data-comment]");
			const comment = target && this.comments.get(target);
			if(!comment || comment === this.lastComment) return;
			this.lastComment = comment;
			this.resumeAt = Date.now() + alfiResume;
			this.alfi.talk(comment.kind, 1, {}, comment.vars);
		};
		screen.addEventListener("pointerover", react);
		screen.addEventListener("focusin", react);
		screen.addEventListener("pointerleave", () => (this.lastComment = null));
	}

	comment(node, kind, vars = {}) {
		node.dataset.comment = "";
		this.comments.set(node, { kind, vars });
		return node;
	}

	idleComment() {
		if(document.body.dataset.screen !== "profile" || !this.insights || Date.now() < (this.resumeAt ?? 0)) return;
		const topics = this.idleTopics();
		if(!topics.length) return;
		const [kind, vars] = topics[this.idleTopic++ % topics.length];
		this.alfi.talk(kind, 0, {}, vars);
	}

	idleTopics() {
		const insights = this.insights;
		if(insights.games === 0) return [["profileEmpty", {}]];
		const run = streak(insights.form);
		return [
			insights.scored && ["profileWinRate", { rate: insights.winRate.at(-1).rate }],
			insights.favouriteFirstMove && ["profileFirstMove", { move: this.openingText(insights.favouriteFirstMove) }],
			insights.opponent && ["profileOpponent", { name: insights.opponent.name, games: insights.opponent.games, wins: insights.opponent.wins }],
			["profileAverage", { moves: insights.averageMoves }],
			run?.length > 1 && run.tone !== "draw" && [run.tone === "win" ? "profileStreakWin" : "profileStreakLoss", { length: run.length }],
			insights.pieces[0] && ["profilePiece", { piece: pieceName(insights.pieces[0].piece), count: insights.pieces[0].count }],
			insights.modes[0] && ["profileMode", { mode: modeLabel(insights.modes[0].mode), count: insights.modes[0].count }],
		].filter(Boolean);
	}

	openingText({ piece, to }) {
		return piece === "PAWN" ? to : `${pieceName(piece)} ${to}`;
	}

	open(account) {
		this.account = account;
		this.games = [];
		this.insights = null;
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
				modeName: modeLabel(game.gamemode),
			};
		});

		this.setState(this.games.length > 0 ? "ready" : "empty");
		this.renderStats();
		this.renderList();
		this.insights = profileInsights(this.games, username);
		this.renderInsights();
		this.resumeAt = Date.now() + alfiResume;
		this.alfi.talk(this.games.length ? "profileGreet" : "profileEmpty", 1, {}, { name: username, games: this.games.length });
	}

	renderInsights() {
		const { averageMoves, favouriteFirstMove: opening, opponent, results, scored } = this.insights;

		byId("insight-average").textContent = averageMoves;
		this.comment(byId("insight-average").parentElement, "profileAverage", { moves: averageMoves });

		const openingNode = byId("insight-opening");
		openingNode.replaceChildren(...(opening ? [pieceImage(opening.piece, "WHITE", this.preferences.theme), opening.to] : ["—"]));
		byId("insight-opening-detail").textContent = opening ? `played first in ${opening.count} games` : "no moves yet";
		if(opening) this.comment(openingNode.parentElement, "profileFirstMove", { move: this.openingText(opening) });

		byId("insight-opponent").textContent = opponent?.name ?? "—";
		byId("insight-opponent-detail").textContent = opponent ? `${opponent.games} games · you won ${opponent.wins}` : "no rivals yet";
		if(opponent) this.comment(byId("insight-opponent").parentElement, "profileOpponent", { name: opponent.name, games: opponent.games, wins: opponent.wins });

		const tones = Object.keys(toneLabels).filter(tone => results[tone] > 0);
		byId("insight-results").replaceChildren(...tones.map(tone => {
			const segment = element("i", `tone-${tone}`);
			segment.style.flexGrow = results[tone];
			return this.comment(segment, `profileStat${toneLabels[tone]}`, { count: results[tone] });
		}));
		byId("insight-legend").replaceChildren(...Object.keys(toneLabels).map(tone => {
			const entry = element("span", `tone-${tone}`);
			entry.append(glyph(toneGlyphs[tone]), `${toneLabels[tone]} ${results[tone]}`, element("small", "", ` ${percent(results[tone], scored)}%`));
			return entry;
		}));

		byId("insight-form").replaceChildren(...this.insights.form.map(tone => {
			const cell = element("li", `tone-${tone}`, toneLetters[tone]);
			cell.title = toneSingle[tone];
			cell.tabIndex = 0;
			return this.comment(cell, { win: "profileFormWin", loss: "profileFormLoss", draw: "profileFormDraw" }[tone]);
		}));

		this.renderRateChart();
		this.renderBars("insight-modes", this.insights.modes, ({ mode, count, winPercent }) => ({
			label: [modeIcon(iconName(mode).slice(5)), modeLabel(mode)],
			value: count,
			note: winPercent === null ? "not scored" : `${winPercent}% won`,
			comment: ["profileMode", { mode: modeLabel(mode), count }],
		}));
		this.renderBars("insight-pieces", this.insights.pieces, ({ piece, count }) => ({
			label: [pieceImage(piece, "WHITE", this.preferences.theme), labels[piece] ?? piece],
			value: count,
			note: "moves",
			comment: ["profilePiece", { piece: pieceName(piece), count }],
		}));
	}

	renderBars(id, entries, describe) {
		const top = Math.max(1, ...entries.map(entry => describe(entry).value));
		byId(id).replaceChildren(...entries.slice(0, 6).map(entry => {
			const { label, value, note, comment } = describe(entry);
			const row = element("div", "insight-bar");
			const name = element("span", "insight-bar-label");
			name.append(...label);
			const track = element("span", "insight-bar-track");
			const fill = element("i");
			fill.style.width = `${percent(value, top)}%`;
			track.append(fill);
			const amount = element("span", "insight-bar-value", String(value));
			amount.append(element("small", "", note));
			row.append(name, track, amount);
			return this.comment(row, ...comment);
		}));
	}

	// Cumulative win rate per scored game; hovering shows the game and makes Alfi comment on the trend.
	renderRateChart() {
		const points = this.insights.winRate;
		const chart = byId("insight-rate");
		if(points.length < 2) {
			chart.replaceChildren(element("p", "insight-empty", "Play a couple of scored games to draw this line."));
			return;
		}
		const [width, height, left, right, top, bottom] = [560, 190, 40, 14, 10, 28];
		const x = (index) => left + (index / (points.length - 1)) * (width - left - right);
		const y = (rate) => top + (1 - rate / 100) * (height - top - bottom);
		const path = points.map((point, index) => `${index ? "L" : "M"}${x(index).toFixed(1)} ${y(point.rate).toFixed(1)}`).join("");
		const last = points.at(-1);
		chart.innerHTML = `<svg viewBox="0 0 ${width} ${height}" role="img" aria-label="Win rate after each game, now ${last.rate}%">
			${[0, 50, 100].map(rate => `<line class="rate-grid${rate === 50 ? " half" : ""}" x1="${left}" x2="${width - right}" y1="${y(rate)}" y2="${y(rate)}"/><text class="rate-axis" x="${left - 6}" y="${y(rate) + 5}" text-anchor="end">${rate}%</text>`).join("")}
			<text class="rate-axis" x="${left}" y="${height - 4}">game 1</text><text class="rate-axis" x="${width - right}" y="${height - 4}" text-anchor="end">game ${points.length}</text>
			<path class="rate-line" d="${path}"/>
			<circle class="rate-end" cx="${x(points.length - 1)}" cy="${y(last.rate)}" r="5"/>
			<line class="rate-cross" y1="${top}" y2="${height - bottom}" visibility="hidden"/>
			<circle class="rate-dot" r="5" visibility="hidden"/>
			<rect class="rate-hit" x="${left}" y="${top}" width="${width - left - right}" height="${height - top - bottom}"/>
		</svg><div class="rate-tip" hidden></div>`;

		const [cross, dot, hit, tip] = ["rate-cross", "rate-dot", "rate-hit", "rate-tip"].map(name => chart.querySelector(`.${name}`));
		let shown = -1;
		hit.addEventListener("pointermove", (event) => {
			const box = hit.getBoundingClientRect();
			const index = Math.round(((event.clientX - box.left) / box.width) * (points.length - 1));
			if(index === shown || index < 0 || index >= points.length) return;
			shown = index;
			const point = points[index];
			cross.setAttribute("x1", x(index));
			cross.setAttribute("x2", x(index));
			dot.setAttribute("cx", x(index));
			dot.setAttribute("cy", y(point.rate));
			cross.setAttribute("visibility", "visible");
			dot.setAttribute("visibility", "visible");
			tip.hidden = false;
			tip.textContent = `Game ${point.game} · ${toneSingle[point.tone]} vs ${point.rival} · ${point.rate}%`;
			tip.style.left = `${(x(index) / width) * 100}%`;
			const before = points[index - 1]?.rate ?? point.rate;
			if(point.rate !== before) {
				this.resumeAt = Date.now() + alfiResume;
				this.alfi.talk(point.rate > before ? "profileRateUp" : "profileRateDown", 1, {}, { rate: point.rate });
			}
		});
		hit.addEventListener("pointerleave", () => {
			shown = -1;
			tip.hidden = true;
			cross.setAttribute("visibility", "hidden");
			dot.setAttribute("visibility", "hidden");
		});
	}

	showError(error) {
		if(error instanceof ApiError && error.status === HttpStatus.FORBIDDEN) return this.setState("expired");

		byId("profile-error-text").textContent = "Couldn't load your game history. Try again.";
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
		for(const [id, kind] of [["stat-games", "profileStatGames"], ["stat-wins", "profileStatWins"], ["stat-losses", "profileStatLosses"], ["stat-draws", "profileStatDraws"]]) {
			this.comment(byId(id).parentElement, kind, { count: byId(id).textContent });
		}
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
		button.setAttribute("aria-label", `Watch replay: ${game.modeName} vs ${game.rival}, ${game.result.label}`);

		const details = element("span", "profile-game-details");
		details.append(
			element("strong", "profile-game-rival", game.rival),
			element("span", "profile-game-meta", `${game.modeName} · ${formatDate(game.createdAt)} · ${movesText(game.moves.length)}`),
		);

		const side = element("span", "profile-game-side");
		if(game.color) side.append(pieceImage("KING", game.color, this.preferences.theme));
		side.title = game.color === "WHITE" ? "You played as White" : game.color === "BLACK" ? "You played as Black" : "";

		const result = element("span", `result-badge tone-${game.result.tone}`);
		result.append(glyph(game.result.glyph), game.result.label);

		const play = element("span", "profile-game-play");
		play.append(glyph("play"), element("span", "", "Replay"));

		button.append(modeIcon(iconName(game.gamemode).slice(5)), details, side, result, play);
		button.addEventListener("click", () => this.replay.open(game));
		const kind = { win: "profileGameWin", loss: "profileGameLoss", draw: "profileGameDraw" }[game.result.tone] ?? "profileGameOther";
		item.append(this.comment(button, kind, { rival: game.rival }));
		return item;
	}
}
