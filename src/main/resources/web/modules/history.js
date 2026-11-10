import { element } from "./ui.js";
import { gamePiece, uiIcon } from "./assets.js";
import { colorSwapped, shownColor } from "./colors.js";
import { labels } from "./state.js";

const pieceDescription = (type, color) =>
	`${shownColor(color) === "WHITE" ? "white" : "black"} ${labels[type] ?? "piece"}`;

// A board-coloured tile with the square's name: a1 is dark, like the real board.
function squareTile(square) {
	const tile = element("span", "move-square", square ?? "—");
	if(square) tile.classList.add((square.charCodeAt(0) - 97 + Number(square[1])) % 2 ? "light" : "dark");
	return tile;
}

const isCastle = (move) => move.type === "KING" && move.from && move.to &&
	Math.abs(move.from.charCodeAt(0) - move.to.charCodeAt(0)) === 2;

export class HistoryView {
	constructor(element, store, preferences, review) {
		this.element = element;
		this.store = store;
		this.preferences = preferences;
		this.review = review;
		this.signature = null;
	}
	plyButton(move, selected) {
		const theme = this.preferences.theme;
		const button = element("button", "move-button");
		button.dataset.ply = move.ply;
		button.dataset.color = move.color;
		button.classList.toggle("active", selected);
		button.setAttribute("aria-pressed", String(selected));

		const name = labels[move.type] ?? "piece";
		const identity = element("span", "move-identity");
		identity.append(element("span", "move-piece-name", name[0].toUpperCase() + name.slice(1)),
			element("small", "move-side", shownColor(move.color) === "WHITE" ? "White" : "Black"));
		const route = element("span", "move-route");
		route.append(squareTile(move.from), element("span", "move-arrow", "→"), squareTile(move.to));
		button.append(gamePiece(move.type, move.color, theme), identity, route);

		let label = `Move ${move.ply}: ${pieceDescription(move.type, move.color)}, from ${move.from ?? "unknown square"} to ${move.to ?? "unknown square"}.`;
		const tags = element("span", "move-tags");
		if(move.capture) {
			const captured = move.capturedPiece;
			const tag = element("span", "move-tag capture", "takes");
			if(captured) tag.append(gamePiece(captured.type, captured.color, theme));
			tags.append(tag);
			label += ` Captures ${captured ? pieceDescription(captured.type, captured.color) : "a piece"}.`;
		}
		if(isCastle(move)) tags.append(element("span", "move-tag castle", "castles"));
		if(tags.childElementCount) button.append(tags);

		button.setAttribute("aria-label", label);
		button.title = this.store.snapshots.has(move.ply) ? `View position after move ${move.ply}` : "Position unavailable";
		button.addEventListener("click", () => this.review(move.ply));
		return button;
	}
	render() {
		const moves = this.store.state?.moves ?? [];
		const signature = [this.preferences.theme, colorSwapped(), this.store.reviewPly,
			...moves.map(move => [move.ply, move.from, move.to, move.type, move.color, move.capture,
				move.capturedPiece?.type, move.capturedPiece?.color].join(":"))].join("|");
		if(signature === this.signature) return;
		this.signature = signature;

		const focusedPly = this.element.contains(document.activeElement) ? document.activeElement.dataset.ply : null;
		const previousScroll = this.element.scrollTop;
		const following = this.element.scrollHeight - previousScroll - this.element.clientHeight < 24;
		const fragment = document.createDocumentFragment();
		if(!moves.length) {
			const empty = element("div", "empty");
			empty.setAttribute("role", "listitem");
			empty.append(uiIcon("duel", "empty-art"), element("strong", "", "Your next move starts here"),
				element("p", "", "Select a piece to see where it can move."));
			fragment.append(empty);
		}
		// One card per turn: the move number, then White's and Black's plies.
		for(let i = 0; i < moves.length; i += 2) {
			const row = element("div", "move-row");
			row.setAttribute("role", "listitem");
			const number = element("span", "move-number", String(i / 2 + 1));
			number.setAttribute("aria-hidden", "true");
			row.append(number);
			for(const move of moves.slice(i, i + 2)) {
				const selected = this.store.reviewPly === move.ply || (this.store.reviewPly === null && move.ply === moves.length);
				row.append(this.plyButton(move, selected));
			}
			fragment.append(row);
		}
		this.element.replaceChildren(fragment);
		if(this.store.reviewPly === null && following) this.element.scrollTop = this.element.scrollHeight;
		else {
			this.element.scrollTop = previousScroll;
			if(this.store.reviewPly !== null)
				this.element.querySelector(".move-button.active")?.scrollIntoView({ block: "nearest" });
		}
		if(focusedPly) this.element.querySelector(`[data-ply="${focusedPly}"]`)?.focus({ preventScroll: true });
	}
}
