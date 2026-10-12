import { labels } from "./state.js";
import { pieceImage, setBoardTiles } from "./assets.js";
export { pieceImage } from "./assets.js";

export class BoardView {
  constructor(element, store, preferences, select, move) {
    this.element = element;
    this.store = store;
    this.preferences = preferences;
    this.select = select;
    this.move = move;
    this.drag = null;
    this.touchGesture = null;
    this.suppressClickUntil = 0;
    element.addEventListener("click", (event) => {
      if (performance.now() < this.suppressClickUntil) return;
      const target = event.target.closest("[data-square]");
      if (target) this.select(target.dataset.square);
    });
    element.addEventListener("dragstart", (event) => {
      const target = event.target.closest("[data-square]");
      const item = store.pieces?.get(target?.dataset.square);
      if (!store.canSelectPiece(item)) {
        event.preventDefault();
        return;
      }
      this.drag = { from: target.dataset.square, ready: null };
      this.drag.ready = this.select(target.dataset.square);
      event.dataTransfer.setData("text/plain", target.dataset.square);
      event.dataTransfer.effectAllowed = "move";
    });
    element.addEventListener("dragover", (event) => {
      if (this.drag) {
        event.preventDefault();
        event.dataTransfer.dropEffect = "move";
      }
    });
    element.addEventListener("drop", async (event) => {
      event.preventDefault();
      const target = event.target.closest("[data-square]");
      const drag = this.drag;
      this.drag = null;
      if (!target || !drag) return;
      await drag.ready;
      if (store.selected === drag.from && store.targets.has(target.dataset.square))
        this.move(drag.from, target.dataset.square);
    });
    element.addEventListener("dragend", () => {
      this.drag = null;
      this.render();
    });
    element.addEventListener("pointerdown", (event) => {
      if (event.pointerType !== "touch") return;
      const tile = event.target.closest("[data-square]")?.dataset.square;
      if (!store.canSelectPiece(store.pieces?.get(tile))) return;
      this.touchGesture = { from: tile, x: event.clientX, y: event.clientY, moved: false };
    });
    element.addEventListener("pointermove", (event) => {
      if (!this.touchGesture) return;
      const distance = Math.hypot(
        event.clientX - this.touchGesture.x,
        event.clientY - this.touchGesture.y,
      );
      if (distance > 8) {
        this.touchGesture.moved = true;
        element.setPointerCapture(event.pointerId);
      }
      if (this.touchGesture.moved && event.cancelable) event.preventDefault();
    });
    element.addEventListener("pointerup", async (event) => {
      const gesture = this.touchGesture;
      this.touchGesture = null;
      if (!gesture?.moved) return;
      this.suppressClickUntil = performance.now() + 400;
      const destination = document
        .elementFromPoint(event.clientX, event.clientY)
        ?.closest("[data-square]")?.dataset.square;
      if (!destination || destination === gesture.from) return;
      await this.select(gesture.from);
      if (store.selected === gesture.from && store.targets.has(destination))
        this.move(gesture.from, destination);
    });
    element.addEventListener("pointercancel", () => {
      this.touchGesture = null;
    });
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
    if (this.drag) return;
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
      last;
    const previousPosition = animateMove
      ? this.element.querySelector(`[data-square="${last.from}"] .piece`)?.getBoundingClientRect()
      : null;
    this.element.dataset.theme = this.preferences.theme;
    setBoardTiles(this.element, this.preferences.theme);
    this.element.setAttribute("aria-label", `Tablero, ${black ? "negras" : "blancas"} abajo`);
    const fragment = document.createDocumentFragment();
    [...ranks].forEach((rank, row) =>
      [...files].forEach((file, column) => {
        const tile = file + rank;
        const item = pieces.get(tile);
        const button = document.createElement("button");
        button.type = "button";
        button.dataset.square = tile;
        button.className = "square";
        if (("abcdefgh".indexOf(file) + Number(rank)) % 2 === 1) button.classList.add("dark");

        if (last?.from === tile || last?.to === tile) button.classList.add("last-move");
        if (this.store.targets.has(tile))
          button.classList.add("legal", ...(item ? ["capture"] : []));
        if (
          this.store.reviewPly === null &&
          item?.type === "KING" &&
          item.color === this.store.state?.checkedColor
        )
          button.classList.add("in-check");
        button.setAttribute(
          "aria-label",
          `${tile}${item ? `, ${labels[item.type]} ${item.color === "WHITE" ? "blanco" : "negro"}` : ""}`,
        );
        if (item) {
          const image = pieceImage(item.type, item.color, this.preferences.theme);
          image.classList.add("piece");
          image.dataset.pieceType = item.type;
          image.draggable = this.store.canSelectPiece(item);
          button.append(image);
        }
        if (!column) {
          const coordinate = document.createElement("span");
          coordinate.className = "coordinate rank";
          coordinate.textContent = rank;
          button.append(coordinate);
        }
        if (row === 7) {
          const coordinate = document.createElement("span");
          coordinate.className = "coordinate file";
          coordinate.textContent = file;
          button.append(coordinate);
        }
        fragment.append(button);
      }),
    );
    this.element.replaceChildren(fragment);
    if (previousPosition) this.animatePiece(previousPosition, last);
    this.lastSessionId = this.store.session?.sessionId;
    this.lastRenderedPly = ply;
    this.lastReviewPly = this.store.reviewPly;
    this.lastOrientation = this.store.orientation;
    if (focused)
      this.element.querySelector(`[data-square="${focused}"]`)?.focus({ preventScroll: true });
  }
  async animatePiece(origin, move) {
    if (matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const destination = this.element.querySelector(`[data-square="${move.to}"] .piece`);
    if (!destination) return;
    const position = destination.getBoundingClientRect();
    const offsetX = origin.x - position.x;
    const offsetY = origin.y - position.y;
    const jumping = destination.dataset.pieceType === "KNIGHT";
    const frames = [{ transform: `translate(${offsetX}px, ${offsetY}px)` }];
    if (jumping) {
      frames.push({
        offset: 0.5,
        transform: `translate(${offsetX / 2}px, ${offsetY / 2 - position.height * 0.45}px) scale(1.08)`,
      });
    }
    frames.push({ transform: "translate(0, 0)" });
    const tile = destination.parentElement;
    tile.style.zIndex = "4";
    const animation = destination.animate(frames, {
      duration: jumping ? 320 : 220,
      easing: "ease-in-out",
    });
    try {
      await animation.finished;
    } catch {
    } finally {
      tile.style.removeProperty("z-index");
    }
  }
}
