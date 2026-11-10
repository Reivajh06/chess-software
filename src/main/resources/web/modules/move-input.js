import { byId, dialog, element } from "./ui.js";
import { gamePiece } from "./assets.js";
import { labels } from "./state.js";

export function createMoveInput({ store, api, board, audio, preferences, version, playerMove, clearMoveWarning, setNotice }) {
	function selectSquare(tile) {
		const movableColor = store.movableColor;
		const command = store.selection.click(tile, {
			pieces: store.pieces,
			interactive: store.interactive &&
					(!movableColor || store.pieces.get(tile)?.color === movableColor),
		});

		if(command.type === "ignore") {
			return;
		}

		clearMoveWarning();

		switch(command.type) {
			case "load":
				audio.play("select");
				board.render();
				return loadLegalMoves(command);
			case "deselect":
				board.render();
				return;
			case "promote":
				showPromotionPicker(command.to);
				return;
			case "choose":
				offerMoveChoice(command);
				return;
			case "move":
				return playerMove(command);
		}
	}

	async function loadLegalMoves({ from, token }) {
		const generation = version();

		try {
			const response = await api.legal(store.session.sessionId, from);

			if(version() === generation && store.selection.loaded(token, response)) {
				board.render();
			}
		} catch (error) {
			if(version() !== generation || error.name === "AbortError") {
				return;
			}

			if(store.selection.failed(token)) {
				setNotice(error.message);
				board.render();
			}
		}
	}

	const promotionPieces = ["QUEEN", "KNIGHT", "ROOK", "BISHOP"];

	function showPromotionPicker(to) {
		const color = store.pieces.get(store.selection.from)?.color ?? "WHITE";
		const square = board.element.querySelector(`[data-square="${to}"]`);
		const size = square.offsetWidth;
		const downwards = square.offsetTop < board.element.offsetHeight / 2;
		const picker = byId("promotion-picker");
		picker.style.left = `${square.offsetLeft}px`;
		picker.style.width = `${size}px`;
		picker.style.top = downwards ? `${square.offsetTop}px` : "auto";
		picker.style.bottom = downwards ? "auto" : `${board.element.offsetHeight - square.offsetTop - size}px`;
		picker.classList.toggle("upwards", !downwards);

		const buttons = promotionPieces.map((type) => {
			const button = element("button", "promotion-choice");
			button.style.height = `${size}px`;
			button.setAttribute("aria-label", `Promote to ${labels[type]}`);
			button.append(gamePiece(type, color, preferences.theme));
			button.addEventListener("click", () => {
				const command = store.selection.promote(type);
				byId("promotion-layer").hidden = true;

				if(command) {
					playerMove(command);
				} else {
					closePromotionPicker();
				}
			});
			return button;
		});
		const cancel = element("button", "promotion-cancel", "×");
		cancel.setAttribute("aria-label", "Cancel promotion");
		cancel.addEventListener("click", closePromotionPicker);
		picker.replaceChildren(...(downwards ? [...buttons, cancel] : [cancel, ...buttons.reverse()]));
		byId("promotion-layer").hidden = false;
		audio.play("select");
		buttons[0].focus({ preventScroll: true });
	}

	function closePromotionPicker() {
		if(byId("promotion-layer").hidden) return;
		byId("promotion-layer").hidden = true;
		store.selection.reset();
		board.render();
	}

	byId("promotion-layer").addEventListener("click", (event) => {
		if(event.target === event.currentTarget) closePromotionPicker();
	});
	byId("promotion-layer").addEventListener("keydown", (event) => {
		if(event.key === "Escape") closePromotionPicker();
	});

	function offerMoveChoice({ options }) {
		const fragment = document.createDocumentFragment();

		for(const option of options) {
			const button = element("button");
			const name = element("span");

			button.type = "button";
			button.className = "choice";
			name.className = "choice-name";
			name.textContent = option.label || `Move ${option.moveId + 1}`;
			button.append(name);

			button.addEventListener("click", () => {
				const command = store.selection.choose(option.moveId);
				dialog("move-choice-dialog", false);

				if(command) {
					playerMove(command);
				}
			});

			fragment.append(button);
		}

		byId("move-choice-options").replaceChildren(fragment);
		dialog("move-choice-dialog");
	}


	byId("move-choice-dialog").addEventListener("close", () => {
		if(store.selection.cancelChoice()) board.render();
	});
	return { selectSquare, closePromotionPicker };
}
