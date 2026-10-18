export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.status = status;
  }
}
export class ChessApi {
  constructor() {
    this.playerId = null;
    this.controllers = new Set();
  }
  cancel() {
    for (const controller of this.controllers) controller.abort();
    this.controllers.clear();
  }
  async request(path, body, { allowEmpty = false } = {}) {
    const controller = new AbortController();
    this.controllers.add(controller);
    const timer = setTimeout(() => controller.abort("timeout"), 10000);
    try {
      const headers = { Accept: "application/json" };
      if (body !== undefined) headers["Content-Type"] = "application/json";
      const response = await fetch(`/api${path}`, {
        method: body === undefined ? "GET" : "POST",
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
        if ([404, 405, 501].includes(response.status))
          throw new ApiError(
            response.status === 404 && path !== "/sessions"
              ? "Work in progress · Sesión o función no disponible (404)."
              : "Work in progress",
            response.status,
          );
        throw new ApiError(
          payload?.message || payload?.error || `Error del servidor (${response.status}).`,
          response.status,
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
  create(mode, difficulty) {
    return this.request("/sessions", { gamemode: mode, difficulty });
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
    return this.request(
      `/sessions/${encodeURIComponent(id)}/legal-moves`,
      { from, ...(this.playerId ? { playerId: this.playerId } : {}) },
    );
  }
  async move(id, from, to, moveId) {
    if (!Number.isSafeInteger(moveId) || moveId < 0)
      throw new TypeError("La jugada necesita un moveId válido del servidor.");
    const response = await this.request(`/sessions/${encodeURIComponent(id)}/moves`, {
      from,
      moveId,
      to,
      ...(this.playerId ? { playerId: this.playerId } : {}),
    }, { allowEmpty: true });
    const state = response?.state ?? response?.gameState ??
      response?.session?.gameState ?? response;
    if (state && (state.fen != null || state.board != null ||
        state.pieces != null || state.gameStateFen != null)) return response;
    return this.state(id);
  }
}
