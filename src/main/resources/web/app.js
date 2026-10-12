import { ChessApi } from "./modules/api.js";
import { GameStore, normalizeState, stateSignature } from "./modules/state.js";
import { BoardView } from "./modules/board.js";
import { pieceImage, logoUrl } from "./modules/assets.js";
import { HistoryView } from "./modules/history.js";
import { loadPreferences, savePreferences, parseInvitation, invitationPlayer } from "./modules/preferences.js";
import { SessionAccess, SessionPolling } from "./modules/session.js";
import { GameAudio } from "./modules/audio.js";
import { populateSoundSets } from "./modules/soundsets.js";
import { applyTheme, populateThemes } from "./modules/themes.js";
import { modeDefinition } from "./modules/modes.js";
import { GameLauncher, renderMatch, renderAppearance } from "./modules/interface.js";

const byId = (id) => document.getElementById(id);
const preferences = loadPreferences();
const store = new GameStore();
const api = new ChessApi();
const audio = new GameAudio(preferences);
const board = new BoardView(byId("board"), store, preferences, selectSquare, requestMove);
const historyView = new HistoryView(byId("move-history"), store, preferences, reviewPosition);
let sessionVersion = 0;
let selectionVersion = 0;
let pendingMove = false;
let selectionLoading = false;
let warningTimer = null;
let sessionBusy = false;
let joinMode = false;
let pendingPromotion = null;
let pendingMoveChoice = null;
const announcedResults = new Set();
const sessions = new SessionAccess(api);
const launcher = new GameLauncher(preferences, (mode) => {
  byId("mode").value = mode;
  syncSetup();
});
populateThemes(byId("theme-select"));
populateSoundSets(byId("soundset-select"));
applyTheme(preferences.theme);
const liveSession = new SessionPolling(api, {
  sessionId: () => store.session?.sessionId,
  canPoll: () => !!store.session && store.state?.status === "ACTIVE" && !pendingMove,
  onState: (response) => {
    setConnection("Conectado", "online");
    applyGameState(response);
  },
  onError: (error) => {
    if ([404, 405, 501].includes(error.status)) {
      liveSession.stop();
      setConnection("Sesión creada");
      setNotice("La sesión se ha creado. El servidor todavía no permite consultar su estado.");
      return;
    }
    setConnection("Sin conexión");
    setNotice(errorMessage(error), true);
  },
});
function setNotice(message = "", retry = false) {
  byId("notice").textContent = message;
  byId("notice").hidden = !message;
  byId("retry").hidden = !retry;
}

function clearMoveWarning() {
  clearTimeout(warningTimer);
  warningTimer = null;
  byId("move-warning").hidden = true;
  byId("move-warning-text").textContent = "";
}

function showMoveWarning(message) {
  clearTimeout(warningTimer);
  byId("move-warning-text").textContent = message;
  byId("move-warning").hidden = false;
  warningTimer = setTimeout(clearMoveWarning, 2800);
}

function setConnection(text, state = "") {
  byId("connection").textContent = text;
  byId("connection").dataset.state = state;
}

function setSetupError(message = "") {
  byId("setup-error").textContent = message;
  byId("setup-error").hidden = !message;
}

function errorMessage(error) {
  return error.status === 404 && store.session
    ? "La sesión no existe o el servidor todavía no admite esta función."
    : error.message === "Work in progress"
      ? "El servidor todavía no permite crear partidas."
      : error.message;
}

function renderGame() {
  board.render();
  historyView.render();
  renderMatch(store, preferences);
  renderAppearance(preferences);
  launcher.render();
}

function applyGameState(payload) {
  const before = store.state;
  // Work with the actual Java GameState regardless of its API envelope.
  const next = normalizeState(payload);
  const signature = stateSignature(next);
  if (before && signature === store.lastStateSignature) return false;
  const state = store.applyNormalized(next);
  store.lastStateSignature = signature;
  clearMoveWarning();
  if (pendingPromotion) {
    pendingPromotion = null;
    byId("promotion-dialog").close();
  }
  if (pendingMoveChoice) {
    pendingMoveChoice = null;
    byId("move-choice-dialog").close();
  }
  selectionVersion++;
  renderGame();
  if (before && state.moves.length > before.moves.length) {
    const move = state.moves.at(-1);
    const contactDelay = move?.capture ? board.captureDelay : 0;
    audio.play(move?.capture ? "capture" : move?.type === "KING" ? "king_move" : "move", contactDelay);
    if (state.status === "ACTIVE" && state.checkedColor) audio.play("check", contactDelay + 0.35);
  }
  if (["WHITE_WIN", "BLACK_WIN", "DRAW"].includes(state.status)) showResult(state.status);
  else if (store.reviewPly === null)
    setNotice(state.waitingForOpponent ? "Esperando oponente" : state.checkedColor ? "Jaque" : "");
  return true;
}

function showResult(status) {
  const key = `${store.session.sessionId}:${status}`;
  if (announcedResults.has(key)) return;
  announcedResults.add(key);
  const winner = status === "WHITE_WIN" ? "WHITE" : status === "BLACK_WIN" ? "BLACK" : null;
  const title = !winner
    ? "Empate"
    : store.practice
      ? "Partida terminada"
      : store.session.playerColor
      ? winner === store.session.playerColor
        ? "Victoria"
        : "Derrota"
      : "Partida terminada";
  byId("result-title").textContent = title;
  byId("result-detail").textContent = winner
    ? `Ganan las ${winner === "WHITE" ? "blancas" : "negras"}`
    : "Tablas";
  byId("result-card").className =
    `result-card ${winner === "BLACK" ? "black" : winner ? "white" : "draw"}`;
  const resultImage = winner
    ? pieceImage("KING", winner, preferences.theme)
    : document.createElement("img");
  if (!winner) {
    resultImage.src = logoUrl;
    resultImage.alt = "";
  }
  byId("result-image").replaceChildren(resultImage);
  audio.play(!winner ? "draw" : title === "Derrota" ? "defeat" : "victory", 0.45);
  if (!byId("result-dialog").open) byId("result-dialog").showModal();
}

function reviewPosition(ply) {
  if (pendingMove || pendingPromotion) return;
  if (ply === null) {
    store.reviewPly = null;
    setNotice("");
  } else if (store.snapshots.has(ply)) {
    store.reviewPly = ply;
    setNotice("");
  } else {
    setNotice("El servidor no ha proporcionado esta posición histórica.");
    return;
  }
  selectionVersion++;
  selectionLoading = false;
  store.selected = null;
  store.clearLegalMoves();
  clearMoveWarning();
  renderGame();
}

async function selectSquare(tile) {
  if (!store.interactive || pendingMove || pendingPromotion || pendingMoveChoice) return;
  if (store.selected && store.targets.has(tile) && !selectionLoading) {
    clearMoveWarning();
    await requestMove(store.selected, tile);
    return;
  }

  const item = store.pieces.get(tile);
  if (store.selected && !store.canSelectPiece(item)) {
    showMoveWarning(selectionLoading
      ? "Consultando los movimientos legales de la pieza…"
      : `La casilla ${tile.toUpperCase()} no está en los movimientos legales de esta pieza.`);
    return;
  }
  if (!store.canSelectPiece(item)) return;

  const version = ++selectionVersion;
  const generation = sessionVersion;
  // Clicking the already selected piece deselects it; clicking a different
  // friendly piece selects that one instead of attempting an illegal move.
  const deselect = store.selected === tile;
  store.clearLegalMoves();
  store.selected = deselect ? null : tile;
  selectionLoading = !deselect;
  clearMoveWarning();
  board.render();
  if (deselect) return;
  audio.play("select");

  try {
    const response = await api.legal(store.session.sessionId, tile);
    if (sessionVersion !== generation || selectionVersion !== version || store.selected !== tile)
      return;
    store.setLegalMoves(response);
    if (!store.targets.size)
      showMoveWarning("Esta pieza no tiene movimientos legales en la posición actual.");
    board.render();
  } catch (error) {
    if (sessionVersion !== generation || selectionVersion !== version || error.name === "AbortError")
      return;
    store.selected = null;
    setNotice(errorMessage(error));
    board.render();
  } finally {
    if (sessionVersion === generation && selectionVersion === version)
      selectionLoading = false;
  }
}

function offerMoveChoice(from, to, options) {
  pendingMoveChoice = { from, to };
  const fragment = document.createDocumentFragment();
  for (const option of options) {
    const button = document.createElement("button");
    button.type = "button";
    // The optional server label distinguishes two rules leading to the same tile.
    button.textContent = option.label || `Movimiento ${option.moveId + 1}`;
    button.addEventListener("click", () => {
      pendingMoveChoice = null;
      byId("move-choice-dialog").close();
      requestMove(from, to, option.moveId);
    });
    fragment.append(button);
  }
  byId("move-choice-options").replaceChildren(fragment);
  byId("move-choice-dialog").showModal();
}

async function requestMove(from, to, chosenMoveId = null) {
  if (!store.interactive || pendingMove || store.selected !== from) return;
  if (!store.targets.has(to)) {
    showMoveWarning(`La casilla ${to.toUpperCase()} no es un movimiento legal.`);
    return;
  }
  // The existing Java updater does not apply promotion. Avoid sending an
  // unsupported field or allowing a visually incorrect promoted position.
  if (store.pieces.get(from)?.type === "PAWN" && ["1", "8"].includes(to[1])) {
    showMoveWarning("La promoción de peones todavía no está implementada en el servidor.");
    return;
  }
  const options = store.moveOptionsAt(to);
  if (!options.length) {
    showMoveWarning("Este movimiento ya no es válido. Vuelve a seleccionar la pieza.");
    return;
  }
  if (chosenMoveId === null && options.length > 1) {
    offerMoveChoice(from, to, options);
    return;
  }
  const moveId = chosenMoveId ?? options[0].moveId;
  if (!options.some((option) => option.moveId === moveId)) return;

  pendingMove = true;
  selectionLoading = false;
  clearMoveWarning();
  liveSession.stop();
  api.cancel();
  const generation = sessionVersion;
  let stateApplied = false;
  try {
    // ChessApi.move() reads current state after PostMovePiece's empty 200.
    // Do not clear the board or invent an optimistic position before it arrives.
    const response = await api.move(store.session.sessionId, from, to, moveId);
    if (sessionVersion === generation) stateApplied = applyGameState(response);
  } catch (error) {
    if (sessionVersion === generation && error.name !== "AbortError")
      setNotice(error.status === 404
        ? "La jugada se envió, pero falta la consulta de estado para actualizar el tablero."
        : errorMessage(error), true);
  } finally {
    if (sessionVersion === generation) {
      pendingMove = false;
      store.selected = null;
      store.clearLegalMoves();
      // State application already rendered; a second render cuts off the animation.
      if (!stateApplied) board.render();
      schedulePoll(0);
    }
  }
}

function schedulePoll(delay = 1000) {
  liveSession.schedule(delay);
}

function refresh() {
  return liveSession.refresh();
}

function resetSession() {
  sessionVersion++;
  selectionVersion++;
  api.cancel();
  liveSession.stop();
  pendingMove = false;
  selectionLoading = false;
  clearMoveWarning();
  pendingPromotion = null;
  pendingMoveChoice = null;
  store.reset();
  store.lastStateSignature = null;
  api.playerId = null;
  for (const id of ["promotion-dialog", "move-choice-dialog", "result-dialog"])
    byId(id).close();
}

function syncSetup() {
  const mode = modeDefinition(byId("mode").value);
  byId("ai-fields").hidden = joinMode || !["PVAI", "AIVAI", "PVCPU"].includes(byId("mode").value);
  byId("mode-field").hidden = joinMode;
  byId("join-field").hidden = !joinMode;
  byId("mode-explanation").hidden = joinMode || mode.available;
  byId("mode-explanation").textContent = mode.reason ?? "";
  byId("setup-submit").disabled = sessionBusy || (!joinMode && !mode.available);
  byId("setup-submit").textContent = sessionBusy
    ? "Conectando…"
    : joinMode
      ? "Unirse"
      : mode.available
        ? mode.action
        : "No disponible";
}

function openSetup(join = false, id = "") {
  joinMode = join;
  byId("setup-title").textContent = join ? "Unirse a una partida" : "Nueva partida";
  byId("join-id").value = id;
  setSetupError();
  syncSetup();
  if (!byId("setup-dialog").open) byId("setup-dialog").showModal();
}

async function activateSession(session, state) {
  delete document.body.dataset.panel;
  store.session = session;
  api.playerId = session.playerId ?? null;
  applyGameState(state);
  sessions.persist(session);
  byId("setup-dialog").close();
  if (matchMedia("(max-width: 750px)").matches) byId("board").scrollIntoView({ block: "center" });
  setConnection("Conectado", "online");
  schedulePoll();
}

async function handleSessionSubmit(event) {
  event.preventDefault();
  if (sessionBusy || (!joinMode && !modeDefinition(byId("mode").value).available)) return;
  const id = joinMode ? parseInvitation(byId("join-id").value) : null;
  if (joinMode && !id) {
    setSetupError("Introduce un ID o enlace válido.");
    return;
  }
  sessionBusy = true;
  setSetupError();
  syncSetup();
  resetSession();
  const generation = sessionVersion;
  renderGame();
  try {
    const invited = invitationPlayer(byId("join-id").value);
    const { session, state } = await sessions.open(id, joinMode ? "PVP" : byId("mode").value,
      invited.playerId, invited.playerColor);
    if (generation === sessionVersion) await activateSession(session, state);
  } catch (error) {
    if (generation === sessionVersion && error.name !== "AbortError") {
      setSetupError(errorMessage(error));
      setNotice(errorMessage(error));
      setConnection("No disponible");
    }
  } finally {
    sessionBusy = false;
    syncSetup();
  }
}

byId("setup-form").addEventListener("submit", handleSessionSubmit);
byId("mode").addEventListener("change", () => launcher.select(byId("mode").value));
byId("new-game").addEventListener("click", () => openSetup());
byId("join-game").addEventListener("click", () => openSetup(true));
byId("result-new").addEventListener("click", () => {
  byId("result-dialog").close();
  openSetup();
});
for (const button of document.querySelectorAll("[data-close]"))
  button.addEventListener("click", () => byId(button.dataset.close).close());
byId("promotion-dialog").addEventListener("close", () => {
  pendingPromotion = null;
});
byId("move-choice-dialog").addEventListener("close", () => {
  pendingMoveChoice = null;
});
byId("setup-dialog").addEventListener("cancel", (event) => {
  if (sessionBusy) event.preventDefault();
});
byId("flip").addEventListener("click", () => {
  store.flipped = !store.flipped;
  board.render();
});
byId("return-live").addEventListener("click", () => reviewPosition(null));
byId("history-live").addEventListener("click", () => reviewPosition(null));
byId("history-start").addEventListener("click", () => reviewPosition(0));
byId("history-prev").addEventListener("click", () =>
  reviewPosition(Math.max(0, (store.reviewPly ?? store.state.moves.length) - 1)),
);
byId("history-next").addEventListener("click", () => {
  const next = (store.reviewPly ?? store.state.moves.length) + 1;
  reviewPosition(next >= store.state.moves.length ? null : next);
});
byId("retry").addEventListener("click", () => {
  liveSession.failures = 0;
  if (store.session) refresh();
  else openSetup();
});
byId("invite").addEventListener("click", async () => {
  if (!store.session) return;
  const invitation = sessions.invitation(store.session.sessionId);
  try {
    await navigator.clipboard.writeText(invitation);
    setNotice("Invitación copiada.");
  } catch {
    setNotice(`No se pudo copiar. ${invitation}`);
  }
});
for (const [id, key] of [
  ["theme-select", "theme"],
  ["soundset-select", "soundSet"],
  ["panel-view", "view"],
  ["volume", "volume"],
  ["muted", "muted"],
]) {
  const element = byId(id);
  if (key === "muted") element.checked = preferences[key];
  else element.value = preferences[key];
  element.addEventListener("input", () => {
    preferences[key] =
      key === "muted" ? element.checked : key === "volume" ? Number(element.value) : element.value;
    if (key === "theme") applyTheme(preferences.theme);
    if (["soundSet", "volume", "muted"].includes(key)) audio.updatePreferences();
    if (key === "soundSet") audio.play("move");
    savePreferences(preferences);
    renderGame();
  });
}
for (const tab of document.querySelectorAll("[data-view]")) {
  tab.addEventListener("click", () => {
    preferences.view = tab.dataset.view;
    savePreferences(preferences);
    renderGame();
  });
}
byId("sound-toggle").addEventListener("click", () => {
  preferences.muted = !preferences.muted;
  byId("muted").checked = preferences.muted;
  audio.updatePreferences();
  savePreferences(preferences);
  renderAppearance(preferences);
});
byId("settings-open").addEventListener("click", () => {
  byId("settings-dialog").showModal();
  byId("theme-select").focus({ preventScroll: true });
});
for (const button of document.querySelectorAll("[data-panel-open]")) {
  button.addEventListener("click", () => {
    document.body.dataset.panel = button.dataset.panelOpen;
    document.querySelector(`.${button.dataset.panelOpen} .panel-close`).focus();
  });
}
for (const button of document.querySelectorAll(".panel-close"))
  button.addEventListener("click", () => { delete document.body.dataset.panel; });
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") delete document.body.dataset.panel;
});
byId("nav-play").addEventListener("click", () => {
  if (matchMedia("(max-width: 850px)").matches) document.body.dataset.panel = "utilities";
  else byId("mode-cards").querySelector('[aria-pressed="true"]')?.focus();
});
byId("nav-watch").addEventListener("click", () => {
  launcher.select("AIVAI");
  if (matchMedia("(max-width: 850px)").matches) document.body.dataset.panel = "utilities";
});
byId("review-open").addEventListener("click", () => {
  preferences.view = "history";
  renderGame();
  if (matchMedia("(max-width: 850px)").matches) document.body.dataset.panel = "activity";
});

document.addEventListener("pointerdown", () => audio.unlock(), { once: true });
document.addEventListener("keydown", () => audio.unlock(), { once: true });
window.addEventListener("pagehide", () => {
  api.cancel();
  liveSession.stop();
});
renderGame();
const invitation = parseInvitation(new URL(location.href).searchParams.get("session") ?? "");
if (invitation) {
  const saved = sessions.saved(invitation);
  if (saved) {
    resetSession();
    const generation = sessionVersion;
    api.playerId = saved.playerId;
    setConnection("Conectando…");
    api
      .state(invitation)
      .then((response) => {
        if (generation === sessionVersion) activateSession(saved, response);
      })
      .catch((error) => {
        if (generation === sessionVersion && error.name !== "AbortError") {
          openSetup(true, location.href);
          setSetupError(errorMessage(error));
        }
      });
  } else openSetup(true, location.href);
}
