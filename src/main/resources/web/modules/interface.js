import { pieceImage, uiIcon } from "./assets.js";
import { gameModes, modeDefinition } from "./modes.js";

const byId = (id) => document.getElementById(id);
const statusLabels = {
  ACTIVE: "En juego",
  WHITE_WIN: "Ganan las blancas",
  BLACK_WIN: "Ganan las negras",
  DRAW: "Tablas",
};

export class GameLauncher {
  constructor(preferences, onModeChange) {
    this.preferences = preferences;
    this.onModeChange = onModeChange;
    this.mode = "PVP";
    const select = byId("mode");
    for (const [id, mode] of Object.entries(gameModes)) {
      const option = document.createElement("option");
      option.value = id;
      option.textContent = mode.name;
      select.append(option);
    }
  }

  select(mode) {
    this.mode = mode;
    this.render();
    this.onModeChange(mode);
  }

  render() {
    const focusedMode = document.activeElement?.dataset.mode;
    const fragment = document.createDocumentFragment();
    for (const [id, mode] of Object.entries(gameModes)) {
      const button = document.createElement("button");
      button.type = "button";
      button.className = "mode-card";
      button.dataset.mode = id;
      button.setAttribute("aria-pressed", String(id === this.mode));
      button.append(uiIcon(`mode-${id.toLowerCase()}`, "mode-art"));
      const text = document.createElement("span");
      const title = document.createElement("strong");
      title.textContent = mode.name;
      const subtitle = document.createElement("small");
      subtitle.textContent = mode.detail;
      text.append(title, subtitle);
      button.append(text);
      if (!mode.available) {
        button.title = mode.reason;
        const unavailable = document.createElement("span");
        unavailable.className = "mode-unavailable";
        unavailable.textContent = "Próximamente";
        text.append(unavailable);
      }
      button.addEventListener("click", () => this.select(id));
      fragment.append(button);
    }
    byId("mode-cards").replaceChildren(fragment);
    if (focusedMode)
      byId("mode-cards")
        .querySelector(`[data-mode="${focusedMode}"]`)
        ?.focus({ preventScroll: true });
    const selected = modeDefinition(this.mode);
    byId("launcher-badge").textContent = selected.badge;
    byId("launcher-detail").textContent = selected.detail;
    byId("launcher-unavailable").hidden = selected.available;
    byId("launcher-unavailable").textContent = selected.reason ?? "";
    byId("new-game").textContent = selected.action;
    byId("new-game").disabled = !selected.available;
  }
}

export function renderAppearance(preferences) {
  byId("theme-preview").replaceChildren(
    pieceImage("KING", "WHITE", preferences.theme),
    pieceImage("KNIGHT", "WHITE", preferences.theme),
    pieceImage("KNIGHT", "BLACK", preferences.theme),
    pieceImage("KING", "BLACK", preferences.theme),
  );
  byId("sound-toggle").setAttribute("aria-pressed", String(preferences.muted));
  byId("sound-toggle").setAttribute(
    "aria-label",
    preferences.muted ? "Activar sonido" : "Silenciar sonido",
  );
  byId("sound-toggle").replaceChildren(uiIcon("sound"));
  byId("sound-toggle").classList.toggle("is-muted", preferences.muted);
}

export function renderMatch(store, preferences) {
  const session = store.session;
  const state = store.state;
  const spectator = session?.gamemode === "AIVAI";
  const mode = modeDefinition(session?.gamemode);
  const color = store.practice ? store.orientation : (session?.playerColor ?? "WHITE");
  const rival = color === "WHITE" ? "BLACK" : "WHITE";
  const currentStatus = !session
    ? "Sin partida"
    : state?.waitingForOpponent
      ? "Esperando oponente"
      : (statusLabels[state?.status] ?? "Conectando");

  for (const [id, view] of [
    ["history-panel", "history"],
    ["chat-panel", "chat"],
    ["info-panel", "info"],
    ["analysis-panel", "analysis"],
  ]) {
    byId(id).hidden =
      preferences.view !== view &&
      !(preferences.view === "both" && ["history", "chat"].includes(view));
  }
  for (const tab of document.querySelectorAll("[data-view]")) {
    tab.setAttribute("aria-pressed", String(tab.dataset.view === preferences.view));
  }
  byId("panel-view").value = preferences.view;
  byId("review-banner").hidden = store.reviewPly === null;
  byId("move-count").textContent = state?.moves.length ?? 0;
  byId("session-details").hidden = !session;
  byId("session-id").textContent = session?.sessionId ?? "";
  byId("session-mode").textContent = session ? mode.name : "";
  byId("invite").hidden = !session || session.gamemode !== "PVP";
  byId("arena-mode").textContent = session ? mode.badge : "Tablero";
  byId("arena-status").textContent =
    store.reviewPly === null ? currentStatus : `Posición ${store.reviewPly}`;
  byId("player-avatar").replaceChildren(pieceImage("KING", color, preferences.theme));
  byId("opponent-avatar").replaceChildren(pieceImage("KING", rival, preferences.theme));
  byId("player-name").textContent = spectator
    ? (state?.whiteModel ?? "Modelo blanco")
    : store.practice
      ? "Práctica"
      : "Tú";
  byId("opponent-name").textContent = spectator
    ? (state?.blackModel ?? "Modelo negro")
    : store.practice
      ? rival === "BLACK"
        ? "Negras"
        : "Blancas"
      : session?.gamemode === "PVAI"
        ? "IA local"
        : "Oponente";
  byId("player-detail").textContent = !session
    ? "Elige una modalidad"
    : store.practice
      ? "Controlas ambos colores"
      : spectator
        ? "Espectador · Blancas"
        : color === "WHITE"
          ? "Blancas"
          : "Negras";
  byId("opponent-detail").textContent = !session
    ? "Sin partida"
    : state?.waitingForOpponent
      ? "Esperando invitación"
      : spectator
        ? "Espectador · Negras"
        : store.practice
          ? ""
          : rival === "WHITE"
            ? "Blancas"
            : "Negras";
  byId("player-turn").hidden =
    store.practice || !session || state?.status !== "ACTIVE" || state.turn !== color;
  byId("opponent-turn").hidden =
    store.practice || !session || state?.status !== "ACTIVE" || state.turn !== rival;
  byId("info-mode").textContent = session ? mode.name : "Sin partida";
  byId("info-status").textContent = currentStatus;
  byId("info-time").textContent = state?.timeControl ?? "Sin reloj";
  byId("info-players").textContent = store.practice
    ? "Control de ambos colores"
    : spectator
      ? "Dos modelos de IA"
      : session
        ? "Blancas / Negras"
        : "Sin partida";
  byId("spectator-banner").hidden = !spectator;
  byId("thinking-status").textContent = state?.thinkingColor
    ? `Pensando: ${state.thinkingColor === "WHITE" ? "blancas" : "negras"}`
    : "Partida entre modelos";
  renderCaptured("player-captured", state?.eatenPieces?.[rival], preferences);
  renderCaptured("opponent-captured", state?.eatenPieces?.[color], preferences);
  renderClock("player-clock", state?.clocks?.[color], state?.turn === color);
  renderClock("opponent-clock", state?.clocks?.[rival], state?.turn === rival);
  for (const id of ["history-start", "history-prev", "history-next", "history-live", "review-open"])
    byId(id).disabled = !state?.moves.length;
}

function renderCaptured(id, pieces, preferences) {
  const element = byId(id);
  element.hidden = !Array.isArray(pieces) || !pieces.length;
  element.replaceChildren(
    ...(Array.isArray(pieces) ? pieces : []).map((piece) =>
      pieceImage(piece.type, piece.color, preferences.theme),
    ),
  );
}

function renderClock(id, seconds, active) {
  const element = byId(id);
  element.hidden = !Number.isFinite(seconds);
  if (element.hidden) return;
  const remaining = Math.max(0, Math.floor(seconds));
  element.textContent = `${Math.floor(remaining / 60)}:${String(remaining % 60).padStart(2, "0")}`;
  element.classList.toggle("active", active);
}
