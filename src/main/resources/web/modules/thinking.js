import { pluginLabel } from "./modes.js";
import { pluginIdentity, toneFor } from "./identity.js";

const maxEntries = 8;
const sides = { WHITE: "Blancas", BLACK: "Negras" };

export const elapsedLabel = (ms) => `${(Math.max(0, ms) / 1000).toFixed(1)} s`;

export function describeMove(move) {
	if(!move) return "sin respuesta";
	const promotion = move.promotion ? ` = ${String(move.promotion)[0]}` : "";
	return `${move.from} → ${move.to}${promotion}`;
}

// "Ollama (qwen3:14b)" -> "Ollama" and "qwen3:14b"; "stockfish.jar" -> "stockfish".
export const baseName = (name) => pluginLabel(String(name ?? "")).replace(/\s*\([^)]*\)\s*$/, "").trim();
export const modelName = (name) => /\(([^)]*)\)\s*$/.exec(String(name ?? ""))?.[1]?.trim() ?? "";

// The big name is the model when there is one: "Ollama (qwen3:14b)" is shown as "qwen3:14b" with "Ollama" under it,
// because the player may pick two different models of the same plugin. An engine has no model: "Stockfish".
export const displayName = (name) => modelName(name) || baseName(name);
export const subName = (name) => (modelName(name) ? baseName(name) : "");

export function initials(name) {
	const letters = displayName(name).replace(/[^\p{L}\p{N}]/gu, "");
	return letters.slice(0, 2).toUpperCase() || "··";
}

// The colour of a plugin in this game (see identity.js): random, and different from the other participants.
// The model counts: two models of the same plugin are two participants.
export const pluginTone = (name) => toneFor(pluginIdentity(name));

// Against a machine the plugin is known from the start; between machines the page only learns it
// when the server names the plugin that played (see ChessApi.machineMove).
export function machineName(session) {
	return session?.cpu && session.pluginNames?.length ? session.pluginNames[0] : null;
}

// What plugins tell about their calculation (see ChessApi.machineMove). They choose the keys; the common ones
// get a Spanish label and a unit, any other key is shown with its own name.
const detailLabels = {
	depth: "Profundidad",
	nodes: "Nodos",
	nps: "Nodos/s",
	time: "Tiempo",
	reason: "Motivo",
	thinking: "Pensamiento",
};
const longDetails = new Set(["thinking"]);

export function detailLabel(key) {
	return detailLabels[key] ?? key.charAt(0).toUpperCase() + key.slice(1).replace(/[_-]+/g, " ");
}

export function formatDetail(key, value) {
	if((key === "nodes" || key === "nps") && /^\d+$/.test(value)) return Number(value).toLocaleString("es-ES");
	if(key === "time" && /^\d+$/.test(value)) return `${value} ms`;
	return value;
}

/**
 * The rows to show for a move, in order: short facts first, long texts (the model's thinking) last.
 * @param options.thinking false hides the long texts: in a game against a person the player could read the plans.
 */
export function detailRows(details, { thinking = true } = {}) {
	const rows = Object.entries(details ?? {})
		.map(([key, value]) => ({ key, label: detailLabel(key), value: formatDetail(key, value), long: longDetails.has(key) }))
		.filter((row) => thinking || !row.long);
	return [...rows.filter((row) => !row.long), ...rows.filter((row) => row.long)];
}

function element(tag, className, text) {
	const node = document.createElement(tag);
	if(className) node.className = className;
	if(text !== undefined) node.textContent = text;
	return node;
}

function renderDetails(rows) {
	const box = element("div", "thinking-details");
	for(const row of rows) {
		if(row.long) {
			const fold = element("details", "detail-long");
			fold.append(element("summary", "", row.label), element("p", "", row.value));
			box.append(fold);
		} else {
			const line = element("p", "detail-row");
			line.append(element("span", "", row.label), element("strong", "", row.value));
			box.append(line);
		}
	}
	return box;
}

// Append-only log of the machine turns of a game: one slab per turn, coloured after the plugin that plays it.
// Only the running clock is rewritten while a machine thinks, so the list does not flicker.
// A turn that fails is removed: the page retries by itself and the player only needs the moves that happened.
export class ThinkingLog {
	/** @param list the slabs; @param live a marker that shows while a machine is thinking. */
	constructor(list, live) {
		this.list = list;
		this.live = live;
		this.active = null;
		this.timer = null;
	}

	reset() {
		this.#stop();
		this.active = null;
		this.list.replaceChildren();
		this.#showLive(false);
	}

	/** @param name the plugin's name, or null when the page does not know it yet. */
	start(name, color) {
		this.#stop();
		const item = element("li", "thinking-entry is-thinking");
		item.dataset.side = color ?? "";
		const chip = element("span", "slab-chip");
		const title = element("strong");
		const model = element("small");
		const side = element("span", "slab-side");
		side.dataset.side = color ?? "";
		side.title = sides[color] ?? "";
		const time = element("time", "", elapsedLabel(0));
		const head = element("div", "slab-head");
		const names = element("span", "slab-name");
		names.append(title, model);
		head.append(chip, names, side, time);
		const text = element("span", "thinking-text", "pensando");
		item.append(head, text);

		const parts = { item, chip, title, model, time, text, startedAt: performance.now(), name: null };
		this.#identify(parts, name);
		this.list.append(item);
		while(this.list.children.length > maxEntries) this.list.firstElementChild.remove();
		this.list.scrollTop = this.list.scrollHeight;

		this.active = parts;
		this.#showLive(true);
		this.timer = setInterval(() => {
			time.textContent = elapsedLabel(performance.now() - parts.startedAt);
		}, 200);
	}

	/**
	 * @param move the move the machine played ({ from, to, promotion }) or null when it failed.
	 * @param details what the plugin reported about its calculation; with none, only the move is shown.
	 * @param options.thinking whether the long texts of the details (the model's thinking) may be shown.
	 * @param options.pluginName the plugin that played, when the server says so.
	 */
	end(move, details = {}, options = {}) {
		const active = this.active;
		if(!active) return;
		this.#stop();
		this.active = null;
		this.#showLive(false);
		if(!move) {
			active.item.remove();
			return;
		}
		if(options.pluginName) this.#identify(active, options.pluginName);
		active.time.textContent = elapsedLabel(performance.now() - active.startedAt);
		active.text.textContent = describeMove(move);
		active.item.classList.remove("is-thinking");
		active.item.classList.add("is-done");
		const rows = detailRows(details, options);
		if(rows.length) active.item.append(renderDetails(rows));
	}

	#identify(parts, name) {
		parts.name = name ?? null;
		parts.chip.textContent = name ? initials(name) : "··";
		parts.title.textContent = name ? displayName(name) : "Máquina";
		parts.model.textContent = name ? subName(name) : "";
		parts.item.style.setProperty("--tone", pluginTone(name));
		parts.item.title = name ?? "";
	}

	#showLive(visible) {
		if(this.live) this.live.hidden = !visible;
	}

	#stop() {
		clearInterval(this.timer);
		this.timer = null;
	}
}
