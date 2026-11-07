import { pieceImage } from "./assets.js";
import { BoardView } from "./board.js";
import { glyph } from "./glyphs.js";
import { initialFen, labels, parseFen } from "./state.js";

const byId = (id) => document.getElementById(id);

const speeds = [0.5, 1, 2, 4];
const pauseBetweenMoves = 900;

function describeMove(move) {
	const promotion = move.promotion ? ` y corona a ${labels[move.promotion]}` : "";
	const action = move.capture ? "captura en" : "→";

	return `${move.player}: ${labels[move.piece]} ${move.from} ${action} ${move.to}${promotion}`;
}

function moveNumber(ply) {
	return ply % 2 === 1 ? `${Math.ceil(ply / 2)}.` : `${ply / 2}…`;
}

export class ReplayPlayer {

	constructor(preferences) {
		this.preferences = preferences;
		this.game = null;
		this.positions = [];
		this.moves = [];
		this.ply = 0;
		this.speed = 1;
		this.playing = false;
		this.frame = null;
		this.lastTime = null;
		this.remaining = pauseBetweenMoves;
		this.moving = false;
		this.position = 0;
		this.opened = 0;
		this.store = {
			session: null,
			state: { moves: [], checkedColor: null },
			pieces: new Map(),
			reviewPly: null,
			orientation: "WHITE",
			targets: new Set(),
			selected: null,
			interactive: false,
			movableColor: null,
		};
		this.board = new BoardView(byId("replay-board"), this.store, preferences, () => {});

		byId("replay-play").addEventListener("click", () => this.toggle());
		byId("replay-first").addEventListener("click", () => this.jump(0));
		byId("replay-prev").addEventListener("click", () => this.jump(this.ply - 1));
		byId("replay-next").addEventListener("click", () => this.jump(this.ply + 1));
		byId("replay-last").addEventListener("click", () => this.jump(this.moves.length));
		byId("replay-progress").addEventListener("input", (event) => this.jump(Number(event.target.value)));
		byId("replay-dialog").addEventListener("keydown", (event) => this.onKey(event));
		byId("replay-dialog").addEventListener("close", () => this.close());

		this.renderSpeeds();
	}

	open(game) {
		this.close();
		this.game = game;
		this.positions = [parseFen(initialFen), ...game.moves.map(move => parseFen(move.fen))];
		this.moves = game.moves.map((move, index) => ({
			...move,
			color: this.positions[index].get(move.from)?.color ?? (move.ply % 2 === 1 ? "WHITE" : "BLACK"),
			capture: this.positions[index + 1].size < this.positions[index].size,
		}));
		this.store.session = { sessionId: `${game.sessionId}:${++this.opened}` };
		this.store.orientation = game.color === "BLACK" ? "BLACK" : "WHITE";

		this.renderHeading();
		this.renderPlayers();
		this.renderMoveList();

		byId("replay-progress").max = this.moves.length;
		byId("replay-dialog").showModal();
		this.show(0);

		if(this.moves.length > 0) this.play();
	}

	renderHeading() {
		const game = this.game;

		byId("replay-mode-icon").src = `./assets/ui/mode-${game.gamemode.toLowerCase()}.png`;
		byId("replay-mode").textContent = game.modeName;
		byId("replay-title").textContent = `${game.white} contra ${game.black}`;

		const result = byId("replay-result");
		result.className = `result-badge tone-${game.result.tone}`;
		result.replaceChildren(glyph(game.result.glyph), game.result.label);
	}

	renderPlayers() {
		const whiteAtBottom = this.store.orientation === "WHITE";
		const bottom = whiteAtBottom ? "WHITE" : "BLACK";
		const top = whiteAtBottom ? "BLACK" : "WHITE";

		byId("replay-bottom-name").textContent = bottom === "WHITE" ? this.game.white : this.game.black;
		byId("replay-top-name").textContent = top === "WHITE" ? this.game.white : this.game.black;
		byId("replay-bottom-piece").replaceChildren(pieceImage("KING", bottom, this.preferences.theme));
		byId("replay-top-piece").replaceChildren(pieceImage("KING", top, this.preferences.theme));
	}

	renderMoveList() {
		byId("replay-moves").replaceChildren(...this.moves.map(move => {
			const item = document.createElement("li");
			const button = document.createElement("button");
			button.type = "button";
			button.dataset.ply = move.ply;
			button.setAttribute("aria-label", `${moveNumber(move.ply)} ${describeMove(move)}`);
			button.append(
				Object.assign(document.createElement("span"), { className: "replay-moves-number", textContent: moveNumber(move.ply) }),
				pieceImage(move.piece, move.color, this.preferences.theme),
				`${move.from} → ${move.to}${move.promotion ? ` = ${labels[move.promotion]}` : ""}`,
			);
			button.addEventListener("click", () => this.jump(move.ply));
			item.append(button);
			return item;
		}));
	}

	renderSpeeds() {
		byId("replay-speeds").replaceChildren(...speeds.map(speed => {
			const button = document.createElement("button");
			button.type = "button";
			button.textContent = `×${String(speed).replace(".", ",")}`;
			button.dataset.speed = speed;
			button.setAttribute("aria-label", `Velocidad ${String(speed).replace(".", ",")} por uno`);
			button.setAttribute("aria-pressed", String(speed === this.speed));
			button.addEventListener("click", () => this.setSpeed(speed));
			return button;
		}));
	}

	setSpeed(speed) {
		this.updateClock(performance.now());
		this.speed = speed;

		for(const button of byId("replay-speeds").children) {
			button.setAttribute("aria-pressed", String(Number(button.dataset.speed) === speed));
		}

		this.applySpeed();
	}

	applySpeed() {
		for(const animation of this.board.element.getAnimations({ subtree: true })) {
			animation.playbackRate = this.speed;
		}
	}

	onKey(event) {
		const actions = {
			" ": () => this.toggle(),
			ArrowLeft: () => this.jump(this.ply - 1),
			ArrowRight: () => this.jump(this.ply + 1),
			Home: () => this.jump(0),
			End: () => this.jump(this.moves.length),
		};

		if(event.defaultPrevented || !(event.key in actions) || event.target.matches("input")) return;
		if(event.key === " " && event.target.closest("button")) return;

		event.preventDefault();
		actions[event.key]();
	}

	toggle() {
		if(this.playing) return this.pause();

		if(this.ply === this.moves.length && !this.moving) this.jump(0);
		this.play();
	}

	play() {
		if(this.playing || this.moves.length === 0) return;

		for(const animation of this.board.element.getAnimations({ subtree: true })) {
			animation.play();
		}

		this.lastTime = performance.now();
		this.setPlaying(true);
		this.scheduleFrame();
	}

	scheduleFrame() {
		this.frame = requestAnimationFrame(time => this.tick(time));
	}

	updateClock(time) {
		if(this.playing && !this.moving && this.lastTime !== null) {
			this.remaining -= (time - this.lastTime) * this.speed;
		}

		this.lastTime = time;
	}

	tick(time) {
		if(!this.playing) return;

		this.updateClock(time);
		if(!this.moving && this.ply === this.moves.length) return this.pause();
		if(!this.moving && this.remaining <= 0) this.show(this.ply + 1);

		this.scheduleFrame();
	}

	pause() {
		this.updateClock(performance.now());
		cancelAnimationFrame(this.frame);
		this.frame = null;

		for(const animation of this.board.element.getAnimations({ subtree: true })) {
			animation.pause();
		}

		this.setPlaying(false);
	}

	close() {
		this.pause();
		this.position++;
		this.moving = false;
		this.board.finishAnimation?.();
	}

	jump(ply) {
		this.pause();
		this.board.finishAnimation?.();
		this.show(Math.max(0, Math.min(this.moves.length, ply)));
	}

	setPlaying(playing) {
		this.playing = playing;
		const button = byId("replay-play");
		button.replaceChildren(glyph(playing ? "pause" : "play"));
		button.setAttribute("aria-label", playing ? "Pausar" : "Reproducir");
		button.title = playing ? "Pausar" : "Reproducir";
	}

	show(ply) {
		const position = ++this.position;
		this.ply = ply;
		this.store.state = { moves: this.moves.slice(0, ply), checkedColor: null };
		this.store.pieces = this.positions[ply];
		this.board.render();
		this.applySpeed();
		this.renderPosition();
		this.remaining = pauseBetweenMoves;
		this.moving = true;
		this.board.settle().then(() => this.finishMove(position));
	}

	finishMove(position) {
		if(position !== this.position) return;

		this.moving = false;
		this.lastTime = performance.now();
		if(this.playing && this.ply === this.moves.length) this.pause();
	}

	renderPosition() {
		const move = this.moves[this.ply - 1] ?? null;
		const last = this.ply === this.moves.length;

		byId("replay-move").textContent = move
				? `${moveNumber(move.ply)} ${describeMove(move)}`
				: this.moves.length > 0 ? "Posición inicial" : "Esta partida no tiene jugadas.";
		byId("replay-progress").value = this.ply;
		byId("replay-progress").setAttribute("aria-valuetext", `Jugada ${this.ply} de ${this.moves.length}`);
		byId("replay-counter").textContent = `${this.ply} / ${this.moves.length}`;
		byId("replay-first").disabled = this.ply === 0;
		byId("replay-prev").disabled = this.ply === 0;
		byId("replay-next").disabled = last;
		byId("replay-last").disabled = last;
		byId("replay-play").disabled = this.moves.length === 0;

		for(const button of byId("replay-moves").querySelectorAll("button")) {
			const current = Number(button.dataset.ply) === this.ply;
			button.setAttribute("aria-current", String(current));
			if(current) this.scrollMoveIntoView(button);
		}
	}

	scrollMoveIntoView(button) {
		const list = byId("replay-moves");
		const item = button.getBoundingClientRect();
		const viewport = list.getBoundingClientRect();

		if(item.top < viewport.top) list.scrollTop += item.top - viewport.top;
		if(item.bottom > viewport.bottom) list.scrollTop += item.bottom - viewport.bottom;
	}
}
