export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.status = status;
  }
}
export class ChessApi {
  constructor() {
    this.token = null;
    this.controllers = new Set();
  }
  cancel() {
    for (const controller of this.controllers) controller.abort();
    this.controllers.clear();
  }
  async request(path, body) {
    const controller = new AbortController();
    this.controllers.add(controller);
    const timer = setTimeout(() => controller.abort("timeout"), 10000);
    try {
      const headers = { Accept: "application/json" };
      if (this.token) headers["X-Player-Token"] = this.token;
      if (body !== undefined) headers["Content-Type"] = "application/json";
      const response = await fetch(`/api${path}`, {
        method: body === undefined ? "GET" : "POST",
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal,
      });
      const json = response.headers.get("content-type")?.includes("application/json");
      const payload = json ? await response.json().catch(() => null) : null;
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
      if (!payload || typeof payload !== "object")
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
  state(id) {
    return this.request(`/sessions/${encodeURIComponent(id)}`);
  }
  legal(id, from) {
    return this.request(
      `/sessions/${encodeURIComponent(id)}/legal-moves?from=${encodeURIComponent(from)}`,
    );
  }
  move(id, from, to, promotion) {
    return this.request(`/sessions/${encodeURIComponent(id)}/moves`, {
      from,
      to,
      ...(promotion ? { promotion } : {}),
    });
  }
}
