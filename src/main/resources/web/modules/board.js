import { labels } from "./state.js";
import { pieceImage, setBoardTiles } from "./assets.js";
export { pieceImage } from "./assets.js";

export class BoardView {
  constructor(element, store, preferences, select) {
    this.element = element;
    this.store = store;
    this.preferences = preferences;
    this.select = select;
    element.addEventListener("click", (event) => {
      const target = event.target.closest("[data-square]");
      if (target) this.select(target.dataset.square);
    });
    element.addEventListener("dragstart", (event) => event.preventDefault());
    element.addEventListener("keydown", (event) => {
      const buttons = [...element.querySelectorAll(".square")];
      const index = buttons.indexOf(document.activeElement);
      const offsets = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -8, ArrowDown: 8 };
      if (index >= 0 && event.key in offsets) {
        event.preventDefault();
        buttons[Math.max(0, Math.min(63, index + offsets[event.key]))].focus();
      }
    });
  }
  render() {
    this.captureDelay = 0;
    if (this.store.reviewPly !== this.lastReviewPly || this.store.session?.sessionId !== this.lastSessionId ||
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
    this.element.setAttribute("aria-label", `Tablero, ${black ? "negras" : "blancas"} abajo`);
    const targets = this.store.targets;
    const interactive = this.store.interactive;
    const layout = `${files}${ranks}`;
    if (this.layout !== layout) {
      this.finishAnimation?.();
      this.layout = layout;
      const fragment = document.createDocumentFragment();
      [...ranks].forEach((rank, row) =>
        [...files].forEach((file, column) => {
          const button = document.createElement("button");
          button.type = "button";
          button.dataset.square = file + rank;
          button.className = "square";
          if (("abcdefgh".indexOf(file) + Number(rank)) % 2 === 1) button.classList.add("dark");
          if (!column) button.append(Object.assign(document.createElement("span"), { className: "coordinate rank", textContent: rank }));
          if (row === 7) button.append(Object.assign(document.createElement("span"), { className: "coordinate file", textContent: file }));
          fragment.append(button);
        }),
      );
      this.element.replaceChildren(fragment);
    }
    for (const button of this.element.children) {
      const tile = button.dataset.square;
      const item = pieces.get(tile);
      button.classList.toggle("last-move", last?.from === tile || last?.to === tile);
      button.classList.toggle("selected", this.store.selected === tile);
      button.classList.toggle("movable", !!item && interactive);
      button.classList.toggle("legal", targets.has(tile));
      button.classList.toggle("capture", targets.has(tile) && !!item);
      button.classList.toggle("in-check",
        this.store.reviewPly === null &&
        item?.type === "KING" &&
        item.color === this.store.state?.checkedColor);
      const state = this.store.selected === tile
        ? ", seleccionada"
        : targets.has(tile) ? (item ? ", captura posible" : ", destino legal") : "";
      const label = `${tile}${item ? `, ${labels[item.type]} ${item.color === "WHITE" ? "blanc" : "negr"}${["QUEEN", "ROOK"].includes(item.type) ? "a" : "o"}` : ""}${state}`;
      if (button.getAttribute("aria-label") !== label) button.setAttribute("aria-label", label);
      const current = button.querySelector(".piece");
      const key = item ? `${item.type}:${item.color}:${this.preferences.theme}` : null;
      if ((current?.dataset.key ?? null) === key) continue;
      current?.remove();
      if (item) {
        const image = pieceImage(item.type, item.color, this.preferences.theme);
        image.classList.add("piece");
        image.dataset.pieceType = item.type;
        image.dataset.key = key;
        image.draggable = false;
        button.prepend(image);
      }
    }
    if (previousPosition) {
      this.finishAnimation?.();
      this.animatePiece(previousPosition, last, capture);
    }
    this.lastSessionId = this.store.session?.sessionId;
    this.lastRenderedPly = ply;
    this.lastReviewPly = this.store.reviewPly;
    this.lastOrientation = this.store.orientation;
    if (focused)
      this.element.querySelector(`[data-square="${focused}"]`)?.focus({ preventScroll: true });
  }
  async animatePiece(origin, move, capture = null) {
    if (matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const destination = this.element.querySelector(`[data-square="${move.to}"] .piece`);
    if (!destination) return;
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
    if (jumping) {
      frames.push({
        offset: collision ? contactOffset * 0.5 : 0.5,
        transform: `translate(${(offsetX + (collision ? contactX : 0)) / 2}px, ${(offsetY + (collision ? contactY : 0)) / 2 - position.height * 0.45}px)`,
      });
    }
    if (collision) {
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
    if (collision) {
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
      if (this.finishAnimation === cleanup) this.finishAnimation = null;
    };
    this.finishAnimation = cleanup;
    try {
      await Promise.allSettled(animations.map(animation => animation.finished));
    } finally {
      cleanup();
    }
  }
}
