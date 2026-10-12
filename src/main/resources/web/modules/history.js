import { pieceImage, uiIcon } from "./assets.js";
export class HistoryView {
  constructor(element, store, preferences, review) {
    this.element = element;
    this.store = store;
    this.preferences = preferences;
    this.review = review;
  }
  render() {
    const moves = this.store.state?.moves ?? [];
    const previousScroll = this.element.scrollTop;
    const following = this.element.scrollHeight - previousScroll - this.element.clientHeight < 24;
    const fragment = document.createDocumentFragment();
    if (!moves.length) {
      const empty = document.createElement("div");
      empty.className = "empty";
      const title = document.createElement("strong");
      title.textContent = "Tu próxima jugada empieza aquí";
      const detail = document.createElement("p");
      detail.textContent = "Los movimientos de tu partida aparecerán aquí cuando la API de sesiones esté disponible.";
      empty.append(uiIcon("duel", "empty-art"), title, detail);
      fragment.append(empty);
    }
    for (let index = 0; index < moves.length; index += 2) {
      const row = document.createElement("div");
      row.className = "move-row";
      const number = document.createElement("span");
      number.className = "move-number";
      number.textContent = `${index / 2 + 1}.`;
      row.append(number);
      for (const move of moves.slice(index, index + 2)) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "move-button";
        button.dataset.ply = move.ply;
        const selected =
          this.store.reviewPly === move.ply ||
          (this.store.reviewPly === null && move.ply === moves.length);
        button.classList.toggle("active", selected);
        button.setAttribute("aria-pressed", String(selected));
        button.append(pieceImage(move.type, move.color, this.preferences.theme));
        const text = document.createElement("span");
        text.textContent = move.notation;
        button.append(text);
        button.title = this.store.snapshots.has(move.ply)
          ? `Ver posición ${move.ply}`
          : "Posición no disponible";
        button.addEventListener("click", () => this.review(move.ply));
        row.append(button);
      }
      fragment.append(row);
    }
    this.element.replaceChildren(fragment);
    this.element.scrollTop =
      this.store.reviewPly === null && following ? this.element.scrollHeight : previousScroll;
  }
}
