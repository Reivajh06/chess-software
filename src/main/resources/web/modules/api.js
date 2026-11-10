export const HttpStatus = Object.freeze({
	BAD_REQUEST: 400,
	UNAUTHORIZED: 401,
	FORBIDDEN: 403,
	NOT_FOUND: 404,
	CONFLICT: 409,
	SERVICE_UNAVAILABLE: 503,
});

const defaultMessages = {
	[HttpStatus.BAD_REQUEST]: "The server rejected an invalid request.",
	[HttpStatus.FORBIDDEN]: "You can't do that in this game.",
	[HttpStatus.NOT_FOUND]: "The game does not exist on the server.",
	[HttpStatus.CONFLICT]: "That action isn't possible in the game's current state.",
	[HttpStatus.SERVICE_UNAVAILABLE]: "The server is currently unavailable.",
};

// The server names its machine modes PVM (player vs machine) and MVM (machine vs machine). The page keeps its own
// PVCPU / AIVAI ids, so gamemodes are translated here, at the boundary, and nowhere else.
const toServerMode = { PVCPU: "PVM", PVAI: "PVM", AIVAI: "MVM" };
const fromServerMode = { PVM: "PVCPU", MVM: "AIVAI" };

function localModes(value) {
	if(Array.isArray(value)) return value.map(localModes);
	if(!value || typeof value !== "object") return value;
	const result = {};
	for(const [key, item] of Object.entries(value))
		result[key] = key === "gamemode" && typeof item === "string" ? fromServerMode[item] ?? item : localModes(item);
	return result;
}

export class ApiError extends Error {
	constructor(message, status = 0, code = null) {
		super(message);
		this.status = status;
		this.code = code;
	}
}
export class ChessApi {
	constructor() {
		this.controllers = new Set();
	}
	cancel() {
		for(const controller of this.controllers) controller.abort();
		this.controllers.clear();
	}
	async request(path, body, { allowEmpty = false, method = body === undefined ? "GET" : "POST", timeout = 10000 } = {}) {
		const controller = new AbortController();
		this.controllers.add(controller);
		const timer = setTimeout(() => controller.abort("timeout"), timeout);
		try {
			const headers = { Accept: "application/json" };
			if(body !== undefined) headers["Content-Type"] = "application/json";
			const response = await fetch(`/api${path}`, {
				method,
				headers,
				body: body === undefined ? undefined : JSON.stringify(body),
				signal: controller.signal,
				cache: "no-store",
			});
			const bodyText = await response.text();
			let payload = null;
			if(bodyText.trim()) {
				try { payload = JSON.parse(bodyText); } catch {}
			}
			if(!response.ok) {
				throw new ApiError(
					payload?.message || defaultMessages[response.status] || `Server error (${response.status}).`,
					response.status,
					typeof payload?.error === "string" ? payload.error : null,
				);
			}
			if(payload === null && allowEmpty && !bodyText.trim()) return null;
			if(!payload || typeof payload !== "object" || Array.isArray(payload))
				throw new ApiError("Invalid server response.");
			return localModes(payload);
		} catch (error) {
			if(error instanceof ApiError) throw error;
			if(controller.signal.aborted && controller.signal.reason !== "timeout")
				throw new DOMException("Cancelado", "AbortError");
			throw new ApiError("Cannot connect to the server.");
		} finally {
			clearTimeout(timer);
			this.controllers.delete(controller);
		}
	}
	create(mode, difficulty, pluginNames) {
		return this.request("/sessions", { gamemode: toServerMode[mode] ?? mode, difficulty, pluginNames });
	}
	async history() {
		const { sessions } = await this.request("/history");
		return { sessions: Array.isArray(sessions) ? sessions : [] };
	}
	async plugins() {
		const { plugins } = await this.request("/plugins");
		if(!Array.isArray(plugins)) throw new ApiError("Invalid server response.");
		return plugins;
	}
	createAccount(username, password) {
		return this.request("/accounts", { username, password }, { allowEmpty: true });
	}
	login(username, password) {
		return this.request("/login", { username, password }, { allowEmpty: true });
	}
	logout() {
		return this.request("/logout", {}, { allowEmpty: true });
	}
	deleteAccount(username, password) {
		return this.request("/accounts/delete", { username, password }, { allowEmpty: true, method: "DELETE" });
	}
	join(id) {
		return this.request(`/sessions/${encodeURIComponent(id)}/join`, {});
	}
	serverInfo() {
		return this.request("/server");
	}
	state(id) {
		return this.request(`/sessions/${encodeURIComponent(id)}`);
	}
	// The player leaves the game for good: the server removes the session (and what it keeps for it) once no
	// player is left. Best effort and never cancelled by api.cancel(), which runs when the next game starts.
	abandon(id) {
		return fetch(`/api/sessions/${encodeURIComponent(id)}`, { method: "DELETE", keepalive: true, cache: "no-store" })
			.then(() => undefined, () => undefined);
	}
	legal(id, from) {
		return this.request(`/sessions/${encodeURIComponent(id)}/legal-moves`, { from });
	}
	promote(id, tile, newType) {
		return this.request(`/sessions/${encodeURIComponent(id)}/promote`, {
			tile,
			newType,
		}, { allowEmpty: true });
	}
	async move(id, from, to, moveId, promotion = null) {
		if(!Number.isSafeInteger(moveId) || moveId < 0)
			throw new TypeError("The move requires a valid moveId from the server.");
		const response = await this.request(`/sessions/${encodeURIComponent(id)}/moves`, {
			from,
			moveId,
			to,
		}, { allowEmpty: true });
		if(promotion) {
			await this.promote(id, to, promotion);
			return this.state(id);
		}
		const state = response?.state ?? response?.gameState ??
			response?.session?.gameState ?? response;
		if(state && (state.fen != null || state.board != null ||
				state.pieces != null || state.gameStateFen != null)) return response;
		return this.state(id);
	}
	// The server asks the plugin for its move, applies it (promotion included) and answers with the move it
	// played. It can take a while for AI plugins, hence the long timeout.
	async machineMove(id) {
		const response = await this.request(`/sessions/${encodeURIComponent(id)}/request-move`, undefined, { timeout: 120000 });
		const move = playedMove(response);
		const details = normalizeDetails(response.details);
		// The plugin that played, when the server says so: as a field of the response or inside the details.
		const named = [response.plugin, response.pluginName, details.plugin].find((name) => typeof name === "string" && name.trim());
		delete details.plugin;
		return { move, details, plugin: named?.trim() ?? null, state: await this.state(id) };
	}
}

// What a plugin reports about how it chose its move: a map of text values (depth, reason...). Whatever the
// plugin sends is shown as it comes; the response may also wrap the map once more ({ details: { details: {...} } }).
export function normalizeDetails(raw) {
	const source = raw && typeof raw.details === "object" && raw.details !== null && !Array.isArray(raw.details)
		? raw.details
		: raw;
	if(!source || typeof source !== "object" || Array.isArray(source)) return {};
	const details = {};
	for(const [key, value] of Object.entries(source)) {
		if(typeof value === "string" && value.trim()) details[key] = value.trim();
		else if(typeof value === "number" && Number.isFinite(value)) details[key] = String(value);
	}
	return details;
}

function playedMove(response) {
	const from = typeof response.from === "string" ? response.from.trim().toLowerCase() : null;
	const to = typeof response.to === "string" ? response.to.trim().toLowerCase() : null;
	if(!/^[a-h][1-8]$/.test(from ?? "") || !/^[a-h][1-8]$/.test(to ?? ""))
		throw new ApiError("The engine returned an invalid move.");
	const promotion = response.promotion ?? response.promote_to ?? response.promoteTo ?? null;
	return { from, to, promotion: promotion ? String(promotion).toUpperCase() : null };
}
