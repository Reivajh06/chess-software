import { labels } from "./state.js";
import { gamePiece, setBoardTiles, variantTag } from "./assets.js";
import { shownColor } from "./colors.js";
export { pieceImage } from "./assets.js";

export class BoardView {
	constructor(element, store, preferences, select) {
		this.element = element;
		this.store = store;
		this.preferences = preferences;
		this.select = select;
		element.addEventListener("click", (event) => {
			const target = event.target.closest("[data-square]");
			if(target) this.select(target.dataset.square);
		});
		element.addEventListener("dragstart", (event) => event.preventDefault());
		element.addEventListener("keydown", (event) => {
			const buttons = [...element.querySelectorAll(".square")];
			const index = buttons.indexOf(document.activeElement);
			const offsets = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -8, ArrowDown: 8 };
			if(index >= 0 && event.key in offsets) {
				event.preventDefault();
				buttons[Math.max(0, Math.min(63, index + offsets[event.key]))].focus();
			}
		});
	}
	render() {
		this.captureDelay = 0;
		if(this.store.reviewPly !== this.lastReviewPly || this.store.session?.sessionId !== this.lastSessionId ||
				this.preferences.theme !== this.lastTheme) {
			this.finishAnimation?.();
		}
		this.lastTheme = this.preferences.theme;
		const focused = document.activeElement?.dataset.square;
		const pieces = this.store.pieces ?? new Map();
		const black = this.store.orientation === "BLACK";
		const files = black ? "hgfedcba" : "abcdefgh";
		const ranks = black ? "12345678" : "87654321";
		const ply = this.store.reviewPly ?? this.store.state?.moves.length;
		const last = this.store.state?.moves[(ply ?? 0) - 1];
		const animateMove =
			this.lastSessionId === this.store.session?.sessionId &&
			this.lastOrientation === this.store.orientation &&
			this.lastReviewPly === null &&
			this.store.reviewPly === null &&
			ply === this.lastRenderedPly + 1 &&
			!matchMedia("(prefers-reduced-motion: reduce)").matches &&
			last;
		const previousPosition = animateMove
			? this.element.querySelector(`[data-square="${last.from}"] .piece`)?.getBoundingClientRect()
			: null;
		const captured = animateMove && last.capture
			? [...this.element.querySelectorAll(".square .piece")].find(image =>
				image.dataset.color !== last.color &&
				(image.parentElement.dataset.square === last.to || !pieces.has(image.parentElement.dataset.square)))
			: null;
		const capture = captured ? {
			tile: captured.parentElement.dataset.square,
			image: captured.cloneNode(true),
		} : null;
		this.element.dataset.theme = this.preferences.theme;
		setBoardTiles(this.element, this.preferences.theme);
		const bottom = shownColor(black ? "BLACK" : "WHITE");
		this.element.setAttribute("aria-label", `Tablero, ${bottom === "BLACK" ? "negras" : "blancas"} abajo`);
		const targets = this.store.targets;
		const interactive = this.store.interactive;
		const movableColor = this.store.movableColor;
		const layout = `${files}${ranks}`;
		if(this.layout !== layout) {
			this.finishAnimation?.();
			this.layout = layout;
			const fragment = document.createDocumentFragment();
			[...ranks].forEach((rank, row) =>
				[...files].forEach((file, column) => {
					const button = document.createElement("button");
					button.type = "button";
					button.dataset.square = file + rank;
					button.className = "square";
					if(("abcdefgh".indexOf(file) + Number(rank)) % 2 === 1) button.classList.add("dark");
					if(!column) button.append(Object.assign(document.createElement("span"), { className: "coordinate rank", textContent: rank }));
					if(row === 7) button.append(Object.assign(document.createElement("span"), { className: "coordinate file", textContent: file }));
					fragment.append(button);
				}),
			);
			this.element.replaceChildren(fragment);
		}
		for(const button of this.element.children) {
			const tile = button.dataset.square;
			const item = pieces.get(tile);
			button.classList.toggle("last-move", last?.from === tile || last?.to === tile);
			button.classList.toggle("selected", this.store.selected === tile);
			button.classList.toggle("movable", !!item && interactive && (!movableColor || item.color === movableColor));
			button.classList.toggle("legal", targets.has(tile));
			button.classList.toggle("capture", targets.has(tile) && !!item);
			button.classList.toggle("in-check",
				this.store.reviewPly === null &&
				item?.type === "KING" &&
				item.color === this.store.state?.checkedColor);
			const state = this.store.selected === tile
				? ", seleccionada"
				: targets.has(tile) ? (item ? ", captura posible" : ", destino legal") : "";
			const label = `${tile}${item ? `, ${labels[item.type]} ${shownColor(item.color) === "WHITE" ? "blanc" : "negr"}${["QUEEN", "ROOK"].includes(item.type) ? "a" : "o"}` : ""}${state}`;
			if(button.getAttribute("aria-label") !== label) button.setAttribute("aria-label", label);
			const current = button.querySelector(".piece");
			const key = item ? `${item.type}:${item.color}:${this.preferences.theme}${variantTag(item.type, item.color)}` : null;
			if((current?.dataset.key ?? null) === key) continue;
			current?.remove();
			if(item) {
				const image = gamePiece(item.type, item.color, this.preferences.theme);
				image.classList.add("piece");
				image.dataset.pieceType = item.type;
				image.dataset.key = key;
				image.draggable = false;
				button.prepend(image);
			}
		}
		if(previousPosition) {
			this.finishAnimation?.();
			this.animatePiece(previousPosition, last, capture);
		}
		this.lastSessionId = this.store.session?.sessionId;
		this.lastRenderedPly = ply;
		this.lastReviewPly = this.store.reviewPly;
		this.lastOrientation = this.store.orientation;
		if(focused)
			this.element.querySelector(`[data-square="${focused}"]`)?.focus({ preventScroll: true });
	}
	settle() {
		return this.motion ?? Promise.resolve();
	}
	async spin() {
		this.render();
		if(matchMedia("(prefers-reduced-motion: reduce)").matches) return;
		const timing = { duration: 600, easing: "ease-in-out" };
		this.element.classList.add("spinning");
		const animations = [
			this.element.animate([
				{ transform: "rotate(180deg)" },
				{ transform: "rotate(0deg)" },
			], timing),
			...[...this.element.querySelectorAll(".piece")].map((piece) => piece.animate([
				{ transform: "rotate(-180deg)" },
				{ transform: "rotate(0deg)" },
			], timing)),
		];
		await Promise.allSettled(animations.map((animation) => animation.finished));
		this.element.classList.remove("spinning");
	}
	async animatePiece(origin, move, capture = null) {
		if(matchMedia("(prefers-reduced-motion: reduce)").matches) return;
		const destination = this.element.querySelector(`[data-square="${move.to}"] .piece`);
		if(!destination) return;
		const position = destination.getBoundingClientRect();
		const offsetX = origin.x - position.x;
		const offsetY = origin.y - position.y;
		const jumping = destination.dataset.pieceType === "KNIGHT";
		const distance = Math.hypot(offsetX, offsetY) / position.width;
		const travelDuration = jumping ? 400 : Math.min(460, 280 + distance * 26);
		const collision = move.capture && capture;
		const duration = collision ? travelDuration + 220 : travelDuration;
		const contactTime = travelDuration * 0.82;
		const contactOffset = contactTime / duration;
		const captureTile = collision
			? this.element.querySelector(`[data-square="${capture.tile}"]`) : null;
		const victimPosition = captureTile?.getBoundingClientRect() ?? position;
		const victimX = victimPosition.x + victimPosition.width / 2 - (position.x + position.width / 2);
		const victimY = victimPosition.y + victimPosition.height / 2 - (position.y + position.height / 2);
		const approachLength = Math.hypot(victimX - offsetX, victimY - offsetY) || 1;
		const directionX = (victimX - offsetX) / approachLength;
		const directionY = (victimY - offsetY) / approachLength;
		const contactX = victimX - directionX * position.width * 0.3;
		const contactY = victimY - directionY * position.height * 0.3;
		const fallDirection = Math.abs(directionX) > 0.1 ? Math.sign(directionX) : Math.sign(directionY) || 1;
		const frames = [{
			transform: `translate(${offsetX}px, ${offsetY}px)`,
			easing: collision ? "cubic-bezier(.5,0,.8,.6)" : "cubic-bezier(.22,.8,.3,1)",
		}];
		if(jumping) {
			frames.push({
				offset: collision ? contactOffset * 0.5 : 0.5,
				transform: `translate(${(offsetX + (collision ? contactX : 0)) / 2}px, ${(offsetY + (collision ? contactY : 0)) / 2 - position.height * 0.45}px)`,
			});
		}
		if(collision) {
			frames.push({ offset: contactOffset, transform: `translate(${contactX}px, ${contactY}px)` });
			frames.push({
				offset: contactOffset + 0.08,
				transform: `translate(${contactX - directionX * 5}px, ${contactY - directionY * 5}px) rotate(${-fallDirection * 2}deg)`,
			});
			frames.push({ offset: 0.92, transform: "translate(0, 0) rotate(0deg)" });
			this.captureDelay = contactTime / 1000;
		} else {
		}
		frames.push({ transform: "translate(0, 0)" });
		const tile = destination.parentElement;
		tile.style.zIndex = "4";
		destination.classList.add("piece-in-motion");
		const animations = [destination.animate(frames, { duration, easing: "linear" })];
		let ghost = null;
		if(collision) {
			ghost = capture.image;
			ghost.classList.remove("piece");
			ghost.classList.add("capture-ghost");
			captureTile.append(ghost);
			const fallX = fallDirection * position.width * 0.22;
			const fallY = directionY * position.height * 0.14 + position.height * 0.08;
			animations.push(ghost.animate([
				{ opacity: 1, transform: "translate(0, 0) rotate(0deg)", easing: "ease-in" },
				{ offset: 0.18, opacity: 1, transform: `translate(${fallX * 0.12}px, ${fallY * 0.12}px) rotate(${fallDirection * 15}deg)`, easing: "ease-in" },
				{ offset: 0.6, opacity: 1, transform: `translate(${fallX * 0.85}px, ${fallY}px) rotate(${fallDirection * 96}deg)`, easing: "ease-out" },
				{ offset: 0.78, opacity: 1, transform: `translate(${fallX}px, ${fallY}px) rotate(${fallDirection * 88}deg)` },
				{ offset: 0.88, opacity: 1, transform: `translate(${fallX}px, ${fallY}px) rotate(${fallDirection * 90}deg)`, easing: "steps(3)" },
				{ opacity: 0, transform: `translate(${fallX}px, ${fallY}px) rotate(${fallDirection * 90}deg)` },
			], { duration: 480, delay: contactTime, fill: "both", easing: "linear" }));
		}
		const cleanup = () => {
			animations.forEach(animation => animation.cancel());
			ghost?.remove();
			destination.classList.remove("piece-in-motion");
			tile.style.removeProperty("z-index");
			if(this.finishAnimation === cleanup) this.finishAnimation = null;
		};
		this.finishAnimation = cleanup;
		const motion = Promise.allSettled(animations.map(animation => animation.finished));
		this.motion = motion;
		try {
			await motion;
		} finally {
			cleanup();
			if(this.motion === motion) this.motion = null;
		}
	}

	async colorWave(apply, onRow) {
		const squares = [...this.element.querySelectorAll(".square")];
		const before = new Map(squares.map((square) => [square, square.querySelector(".piece")?.cloneNode(true)]));
		apply();
		this.render();
		if(matchMedia("(prefers-reduced-motion: reduce)").matches) return;
		const rowDelay = 110;
		const fold = 130;
		const animations = [];
		squares.forEach((square, index) => {
			const delay = Math.floor(index / 8) * rowDelay;
			const ghost = before.get(square);
			const piece = square.querySelector(".piece");
			if(ghost) {
				ghost.classList.remove("piece");
				ghost.classList.add("capture-ghost");
				square.append(ghost);
				animations.push(ghost.animate(
					[{ transform: "scaleX(1)" }, { transform: "scaleX(0)" }],
					{ duration: fold, delay, easing: "steps(3)", fill: "forwards" },
				));
			}
			if(piece) {
				animations.push(piece.animate(
					[{ transform: "scaleX(0)" }, { transform: "scaleX(0)", offset: 0.5 }, { transform: "scaleX(1)" }],
					{ duration: fold * 2, delay, easing: "steps(6)", fill: "backwards" },
				));
			}
			animations.push(square.animate(
				[{ filter: "brightness(1)" }, { filter: "brightness(1.7)" }, { filter: "brightness(1)" }],
				{ duration: fold * 2, delay, easing: "steps(3)" },
			));
		});
		for(let row = 0; row < 8; row++) setTimeout(() => onRow?.(row), row * rowDelay);
		await Promise.allSettled(animations.map((animation) => animation.finished));
		for(const ghost of before.values()) ghost?.remove();
	}

	async topple(tile) {
		const square = this.element.querySelector(`[data-square="${tile}"]`);
		const piece = square?.querySelector(".piece");
		if(!piece) return;
		const ghost = piece.cloneNode(true);
		ghost.classList.remove("piece");
		ghost.classList.add("capture-ghost");
		piece.style.visibility = "hidden";
		square.append(ghost);
		const size = square.offsetWidth;
		const fallX = size * 0.22;
		const fallY = size * 0.08;
		await ghost.animate([
			{ opacity: 1, transform: "translate(0, 0) rotate(0deg)", easing: "ease-in" },
			{ offset: 0.18, opacity: 1, transform: `translate(${fallX * 0.12}px, ${fallY * 0.12}px) rotate(15deg)`, easing: "ease-in" },
			{ offset: 0.6, opacity: 1, transform: `translate(${fallX * 0.85}px, ${fallY}px) rotate(96deg)`, easing: "ease-out" },
			{ offset: 0.78, opacity: 1, transform: `translate(${fallX}px, ${fallY}px) rotate(88deg)` },
			{ offset: 0.88, opacity: 1, transform: `translate(${fallX}px, ${fallY}px) rotate(90deg)`, easing: "steps(3)" },
			{ opacity: 0, transform: `translate(${fallX}px, ${fallY}px) rotate(90deg)` },
		], { duration: 650, fill: "both" }).finished.catch(() => {});
		ghost.remove();
		piece.style.removeProperty("visibility");
	}

	async dropIn(tile) {
		const piece = this.element.querySelector(`[data-square="${tile}"] .piece`);
		if(!piece) return;
		await piece.animate([
			{ transform: "translateY(-120%) scale(0.6)", opacity: 0 },
			{ offset: 0.6, transform: "translateY(6%) scale(1.08, 0.9)", opacity: 1 },
			{ transform: "translateY(0) scale(1)", opacity: 1 },
		], { duration: 460, easing: "steps(6)" }).finished.catch(() => {});
	}
}
