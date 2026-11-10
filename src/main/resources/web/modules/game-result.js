import { byId, dialog, element } from "./ui.js";
import { shownColor } from "./colors.js";
import { gamePiece, logoUrl } from "./assets.js";
import { materialBalance } from "./interface.js";

export function createResults({ store, alfi, preferences, audio, version, mascotReact }) {
	let announcedResult = null;
	function renderResultStats(winner) {
		const moves = store.state?.moves ?? [];
		const captures = (color) => moves.filter((move) => move.capture && move.color === color).length;
		const { lead } = materialBalance(store.state?.pieces);
		byId("stat-moves").textContent = String(Math.ceil(moves.length / 2));
		byId("stat-captures").textContent = `${captures("WHITE")} · ${captures("BLACK")}`;
		byId("stat-material").textContent = !lead ? "=" : `${lead > 0 ? "W" : "B"} +${Math.abs(lead)}`;
		byId("result-rematch").textContent = store.session?.gamemode === "PVP" ? "New room" : "Rematch";
		byId("result-rematch").hidden = !!store.session?.watching; // a watcher does not start games
		const confetti = document.querySelector(".confetti");
		confetti.replaceChildren(
			...(winner ? Array.from({ length: 18 }, (_, index) => {
				const bit = element("i");
				bit.style.left = `${(index * 37) % 100}%`;
				bit.style.animationDelay = `${(index % 6) * 0.12}s`;
				bit.style.background = ["var(--accent)", "#edf4ff", "#e05a5a", "#6fc4ff"][index % 4];
				return bit;
			}) : []),
		);
	}

	async function showResult(status) {
		const key = `${store.session.sessionId}:${status}`;

		if(announcedResult === key) return;
		announcedResult = key;

		const finale = alfi.active && store.session?.cpu && store.session.alfi === "revealed";
		if(finale) {
			const generation = version();
			const winnerColor = { WHITE_WIN: "WHITE", BLACK_WIN: "BLACK" }[status];
			const outcome = !winnerColor ? "draw" : winnerColor === store.session.playerColor ? "win" : "loss";
			await alfi.ending(outcome, () => generation === version());
			if(generation !== version()) return;
		}

		const winner = status === "WHITE_WIN"
				? "WHITE"
				: status === "BLACK_WIN"
						? "BLACK"
						: null;

		const title = !winner
				? "Draw"
				: store.practice
						? "Game over"
						: store.session.playerColor
								? winner === store.session.playerColor
										? "Victory"
										: "Defeat"
								: "Game over";

		byId("result-title").textContent = finale
				? (!winner ? "Draw with Alfi" : title === "Defeat" ? "Alfi wins" : "You beat Alfi")
				: title;

		byId("result-detail").textContent = finale
				? (!winner ? "Nobody wins. Alfi says he did." : title === "Defeat" ? "Maybe next time. Maybe." : "Even his cheating couldn't save him.")
				: winner
						? `${shownColor(winner) === "WHITE" ? "White" : "Black"} wins`
						: "Draw";

		renderResultStats(winner);

		byId("result-card").className =
				`result-card ${shownColor(winner) === "BLACK" ? "black" : winner ? "white" : "draw"}`;

		const resultImage = winner
				? gamePiece("KING", winner, preferences.theme)
				: document.createElement("img");

		if(!winner) {
			resultImage.src = logoUrl;
			resultImage.alt = "";
		}

		byId("result-image").replaceChildren(resultImage);

		audio.play(
				!winner
						? "draw"
						: title === "Defeat"
								? "defeat"
								: "victory",
				0.45
		);

		if(!finale) mascotReact(!winner ? "draw" : title === "Defeat" ? "defeat" : "victory", 3, 0);

		if(!byId("result-dialog").open) {
			dialog("result-dialog");
		}
	}
	return showResult;
}
