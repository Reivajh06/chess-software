// Mirrors the error statuses declared in ChessAPI; the API answers no other errors.
export const HttpStatus = Object.freeze({
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  SERVICE_UNAVAILABLE: 503,
});

const defaultMessages = {
  [HttpStatus.BAD_REQUEST]: "El servidor ha rechazado una petición no válida.",
  [HttpStatus.FORBIDDEN]: "No puedes hacer eso en esta partida.",
  [HttpStatus.NOT_FOUND]: "La partida no existe en el servidor.",
  [HttpStatus.CONFLICT]: "Esa acción no es posible en el estado actual de la partida.",
  [HttpStatus.SERVICE_UNAVAILABLE]: "El servidor no está disponible ahora mismo.",
};

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
    for (const controller of this.controllers) controller.abort();
    this.controllers.clear();
  }
  async request(path, body, { allowEmpty = false, method = body === undefined ? "GET" : "POST" } = {}) {
    const controller = new AbortController();
    this.controllers.add(controller);
    const timer = setTimeout(() => controller.abort("timeout"), 10000);
    try {
      const headers = { Accept: "application/json" };
      if (body !== undefined) headers["Content-Type"] = "application/json";
      const response = await fetch(`/api${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal,
        cache: "no-store",
      });
      const bodyText = await response.text();
      let payload = null;
      if (bodyText.trim()) {
        try { payload = JSON.parse(bodyText); } catch {}
      }
      if (!response.ok) {
        throw new ApiError(
          payload?.message || defaultMessages[response.status] || `Error del servidor (${response.status}).`,
          response.status,
          typeof payload?.error === "string" ? payload.error : null,
        );
      }
      if (payload === null && allowEmpty && !bodyText.trim()) return null;
      if (!payload || typeof payload !== "object" || Array.isArray(payload))
        throw new ApiError("Respuesta del servidor no válida.");
      return payload;
    } catch (error) {
      if (error instanceof ApiError) throw error;
      if (controller.signal.aborted && controller.signal.reason !== "timeout")
        throw new DOMException("Cancelado", "AbortError");
      throw new ApiError("No se puede conectar con el servidor.");
    } finally {
      clearTimeout(timer);
      this.controllers.delete(controller);
    }
  }
  create(mode, difficulty, pluginNames) {
    return this.request("/sessions", { gamemode: mode, difficulty, pluginNames });
  }
  async plugins() {
    const { plugins } = await this.request("/plugins");
    if (!Array.isArray(plugins)) throw new ApiError("Respuesta del servidor no válida.");
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
    if (!Number.isSafeInteger(moveId) || moveId < 0)
      throw new TypeError("La jugada necesita un moveId válido del servidor.");
    const response = await this.request(`/sessions/${encodeURIComponent(id)}/moves`, {
      from,
      moveId,
      to,
    }, { allowEmpty: true });
    if (promotion) {
      await this.promote(id, to, promotion);
      return this.state(id);
    }
    const state = response?.state ?? response?.gameState ??
      response?.session?.gameState ?? response;
    if (state && (state.fen != null || state.board != null ||
        state.pieces != null || state.gameStateFen != null)) return response;
    return this.state(id);
  }
  // Jugada de una máquina: sin casillas, el servidor sabe por los colores de la sesión quién mueve, calcula
  // un único movimiento, lo aplica (coronación incluida) y devuelve la jugada hecha. Después se lee el estado.
  async machineMove(id) {
    const response = await this.request(`/sessions/${encodeURIComponent(id)}/moves`, {});
    const move = playedMove(response);
    return { move, state: await this.state(id) };
  }
}

// La jugada llega como { from, to, promote_to } (o promotion); las casillas pueden venir
// en mayúsculas y la coronación vacía cuando no la hay.
function playedMove(response) {
  const from = typeof response.from === "string" ? response.from.trim().toLowerCase() : null;
  const to = typeof response.to === "string" ? response.to.trim().toLowerCase() : null;
  if (!/^[a-h][1-8]$/.test(from ?? "") || !/^[a-h][1-8]$/.test(to ?? ""))
    throw new ApiError("El motor ha devuelto una jugada no válida.");
  const promotion = response.promotion ?? response.promote_to ?? response.promoteTo ?? null;
  return { from, to, promotion: promotion ? String(promotion).toUpperCase() : null };
}
