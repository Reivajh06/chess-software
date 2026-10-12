import { pieceImage, uiIcon } from "./assets.js";
import { labels } from "./state.js";

function textSpan(className, text) {
  const span = document.createElement("span");
  span.className = className;
  span.textContent = text;
  return span;
}

function pieceDescription(type, color) {
  const feminine = ["QUEEN", "ROOK"].includes(type);
  const side = color === "WHITE" ? "blanc" : "negr";
  return `${labels[type] ?? "pieza"} ${side}${feminine ? "a" : "o"}`;
}

export class HistoryView {
  constructor(element, store, preferences, review) {
    this.element = element;
    this.store = store;
    this.preferences = preferences;
    this.review = review;
  }
  render() {
    const moves = this.store.state?.moves ?? [];
    const focusedPly = this.element.contains(document.activeElement)
      ? document.activeElement.dataset.ply : null;
    const previousScroll = this.element.scrollTop;
    const following = this.element.scrollHeight - previousScroll - this.element.clientHeight < 24;
    const fragment = document.createDocumentFragment();
    if (!moves.length) {
      const empty = document.createElement("div");
      empty.className = "empty";
      empty.setAttribute("role", "listitem");
      const title = document.createElement("strong");
      title.textContent = "Tu próxima jugada empieza aquí";
      const detail = document.createElement("p");
      detail.textContent = "Los movimientos de tu partida aparecerán aquí después de cada jugada.";
      empty.append(uiIcon("duel", "empty-art"), title, detail);
      fragment.append(empty);
    }
    for (const move of moves) {
      const row = document.createElement("div");
      row.className = "move-row";
      row.setAttribute("role", "listitem");
      const number = textSpan("move-number", String(move.ply));
      number.setAttribute("aria-hidden", "true");
      row.append(number);
      const button = document.createElement("button");
      button.type = "button";
      button.className = "move-button";
      button.dataset.ply = move.ply;
      const selected =
        this.store.reviewPly === move.ply ||
        (this.store.reviewPly === null && move.ply === moves.length);
      button.classList.toggle("active", selected);
      button.setAttribute("aria-pressed", String(selected));

      const identity = document.createElement("span");
      identity.className = "move-identity";
      const side = textSpan("move-side", move.color === "WHITE" ? "Blancas" : "Negras");
      side.dataset.color = move.color;
      const name = labels[move.type] ?? "pieza";
      identity.append(pieceImage(move.type, move.color, this.preferences.theme),
        textSpan("move-piece-name", name[0].toUpperCase() + name.slice(1)), side);
      button.append(identity);

      const route = document.createElement("span");
      route.className = "move-route";
      for (const [label, square] of [["De", move.from], ["A", move.to]]) {
        if (label === "A") {
          const arrow = textSpan("move-arrow", "→");
          arrow.setAttribute("aria-hidden", "true");
          route.append(arrow);
        }
        const endpoint = document.createElement("span");
        endpoint.className = "move-endpoint";
        endpoint.append(textSpan("move-square-label", label), textSpan("move-square", square ?? "—"));
        route.append(endpoint);
      }
      button.append(route);

      const description = pieceDescription(move.type, move.color);
      let accessibleLabel = `Jugada ${move.ply}: ${description}, de ${move.from ?? "casilla desconocida"} a ${move.to ?? "casilla desconocida"}.`;
      if (move.capture) {
        const capture = document.createElement("span");
        capture.className = "move-capture";
        const captured = move.capturedPiece;
        const capturedDescription = captured ? pieceDescription(captured.type, captured.color) : "pieza";
        capture.append(textSpan("move-capture-label", "Captura"));
        const capturedIdentity = document.createElement("span");
        capturedIdentity.className = "move-capture-piece";
        if (captured) capturedIdentity.append(pieceImage(captured.type, captured.color, this.preferences.theme));
        capturedIdentity.append(textSpan("move-captured-name", capturedDescription));
        capture.append(capturedIdentity);
        button.append(capture);
        accessibleLabel += ` Captura ${capturedDescription}.`;
      }
      button.setAttribute("aria-label", accessibleLabel);
      button.title = this.store.snapshots.has(move.ply)
        ? `Ver posición después de la jugada ${move.ply}`
        : "Posición no disponible";
      button.addEventListener("click", () => this.review(move.ply));
      row.append(button);
      fragment.append(row);
    }
    this.element.replaceChildren(fragment);
    this.element.scrollTop =
      this.store.reviewPly === null && following ? this.element.scrollHeight : previousScroll;
    if (focusedPly)
      this.element.querySelector(`[data-ply="${focusedPly}"]`)?.focus({ preventScroll: true });
  }
}
