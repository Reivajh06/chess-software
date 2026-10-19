import { ChessApi } from "./modules/api.js";
import { GameStore, labels, normalizeState, stateSignature } from "./modules/state.js";
import { BoardView } from "./modules/board.js";
import { pieceImage, logoUrl } from "./modules/assets.js";
import { HistoryView } from "./modules/history.js";
import {
  loadPreferences,
  savePreferences,
  parseInvitation,
  loopbackHost,
} from "./modules/preferences.js";
import { serverAddress } from "./modules/network.js";
import { SessionAccess, SessionPolling } from "./modules/session.js";
import { GameAudio } from "./modules/audio.js";
import { BackgroundMusic } from "./modules/music.js";
import { applyTheme, populateThemes } from "./modules/themes.js";
import { modeDefinition } from "./modules/modes.js";
import { Mascot } from "./modules/mascot.js";
import { Commentator } from "./modules/commentary.js";
import {
  TitleScreen,
  renderMatch,
  renderAppearance,
  materialBalance,
  populateThemeGrid,
  markChoice,
} from "./modules/interface.js";

const byId = (id) => document.getElementById(id);

const preferences = loadPreferences();
const store = new GameStore();
const api = new ChessApi();
const audio = new GameAudio(preferences);
const music = new BackgroundMusic(audio, preferences);

const board = new BoardView(
    byId("board"),
    store,
    preferences,
    selectSquare
);

const historyView = new HistoryView(
    byId("move-history"),
    store,
    preferences,
    reviewPosition
);

let sessionVersion = 0;
let warningTimer = null;
let sessionBusy = false;
let waitingTimer = null;
const waitingChatDelay = 11000;

const announcedResults = new Set();
const sessions = new SessionAccess(api);

const titleMascot = new Mascot(byId("title-mascot"), audio, preferences);
const gameMascot = new Mascot(byId("game-mascot"), audio, preferences);
let hoveredEntry = null;

const title = new TitleScreen({
  onStart: (mode) => openSession(null, mode),
  onJoin: joinSession,
  onContinue: () => showScreen("game"),
  onError: () => titleMascot.talk("error", 2),
  onHover: (id, hint) => {
    if (id === hoveredEntry) return;
    hoveredEntry = id;
    titleMascot.say(hint);
  },
});

const commentator = new Commentator();

function mascotReact(kind, priority = 1, delay = 650, options = {}, vars = {}) {
  setTimeout(() => {
    if (document.body.dataset.screen === "game") gameMascot.talk(kind, priority, options, vars);
  }, delay);
}

function scheduleWaitingChat() {
  clearTimeout(waitingTimer);
  if (!store.state?.waitingForOpponent) return;
  waitingTimer = setTimeout(() => {
    if (document.body.dataset.screen === "game" && store.state?.waitingForOpponent) {
      gameMascot.talk("waiting");
    }
    scheduleWaitingChat();
  }, waitingChatDelay);
}

populateThemes(byId("theme-select"));

function pickOption(selectId, value) {
  const select = byId(selectId);
  if (select.value === value) return;
  select.value = value;
  select.dispatchEvent(new Event("input"));
}

populateThemeGrid(byId("theme-grid"), (id) => pickOption("theme-select", id));

let lastHover = 0;
function hoverSound() {
  const now = performance.now();
  if (now - lastHover < 60) return;
  lastHover = now;
  audio.play("hover");
}

const interfaceTargets = ".title-option, .choice, [role='tab'], .match-actions button, .result-card button, .promotion-choice, .tool-button";
for (const type of ["pointerover", "focusin"]) {
  document.addEventListener(type, (event) => {
    const target = event.target.closest?.(interfaceTargets);
    if (!target || target.disabled || target.contains(event.relatedTarget)) return;
    hoverSound();
  });
}
document.addEventListener("click", (event) => {
  const target = event.target.closest?.(".title-option, .choice, .result-card button, .promotion-choice");
  if (target && !target.disabled) audio.play("confirm");
});

for (const grid of document.querySelectorAll(".choice-grid")) {
  grid.addEventListener("keydown", (event) => {
    const step = { ArrowRight: 1, ArrowDown: 1, ArrowLeft: -1, ArrowUp: -1 }[event.key];
    if (!step) return;
    event.preventDefault();
    const options = [...grid.children];
    const next = options[(options.indexOf(document.activeElement) + step + options.length) % options.length];
    next.focus();
    next.click();
  });
}

function syncSettings() {
  markChoice(byId("theme-grid"), preferences.theme);
  for (const id of ["volume", "music-volume"]) {
    const input = byId(id);
    input.style.setProperty("--fill", `${input.value}%`);
    byId(`${id}-value`).textContent = `${input.value}%`;
  }
  byId("music-volume").disabled = !preferences.music;
  byId("volume").disabled = preferences.muted;
}

const settingsTabs = ["look", "sound", "mascot"];
function showSettingsTab(name, focus = false) {
  for (const tab of settingsTabs) {
    const selected = tab === name;
    byId(`tab-${tab}`).setAttribute("aria-selected", String(selected));
    byId(`tab-${tab}`).tabIndex = selected ? 0 : -1;
    byId(`panel-${tab}`).hidden = !selected;
  }
  if (focus) byId(`tab-${name}`).focus();
}

for (const tab of settingsTabs) {
  byId(`tab-${tab}`).addEventListener("click", () => showSettingsTab(tab));
  byId(`tab-${tab}`).addEventListener("keydown", (event) => {
    const step = { ArrowRight: 1, ArrowLeft: -1 }[event.key];
    if (!step) return;
    const index = settingsTabs.indexOf(tab);
    showSettingsTab(settingsTabs[(index + step + settingsTabs.length) % settingsTabs.length], true);
  });
}
applyTheme(preferences.theme);

const liveSession = new SessionPolling(api, {
  sessionId: () => store.session?.sessionId,

  canPoll: () =>
      !!store.session &&
      store.state?.status === "ACTIVE" &&
      !store.selection.busy,

  onState: (response) => {
    setConnection("Conectado", "online");
    applyGameState(response);
  },

  onError: (error) => {
    if ([404, 405, 501].includes(error.status)) {
      liveSession.stop();
      setConnection("Sin partida");
      setNotice("La partida ya no existe en el servidor. Vuelve al menú y empieza una nueva.");
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
}

function applyGameState(payload) {
  const before = store.state;
  const next = normalizeState(payload);
  const signature = stateSignature(next);

  if (before && signature === store.lastStateSignature) {
    return false;
  }

  const moved = before && next.moves.length !== before.moves.length;
  const state = store.applyNormalized(next);

  if (moved && !store.selection.busy) {
    store.selection.reset();
    closePromotionPicker();
  }
  store.lastStateSignature = signature;

  clearMoveWarning();
  byId("move-choice-dialog").close();
  renderGame();

  if (before?.waitingForOpponent && !state.waitingForOpponent) {
    audio.play("confirm");
    mascotReact("opponentJoined", 2, 0);
  }
  scheduleWaitingChat();

  if (before && state.moves.length > before.moves.length) {
    const move = state.moves.at(-1);
    const contactDelay = move?.capture ? board.captureDelay : 0;

    const promotion =
        move?.type === "PAWN" && /[18]$/.test(move.to ?? "") || !!move?.promotion;

    audio.play(
        move?.capture
            ? "capture"
            : promotion
                ? "promotion"
                : move?.type === "KING"
                    ? "king_move"
                    : "move",
        contactDelay
    );

    if (promotion && move?.capture) {
      audio.play("promotion", contactDelay + 0.18);
    }

    if (state.status === "ACTIVE" && state.checkedColor) {
      audio.play("check", contactDelay + (promotion ? 0.45 : 0.22));
    }

    if (state.checkedColor) {
      shakeBoard(contactDelay);
    }

    const reaction = commentator.react({
      move,
      before,
      after: state,
      userColors: store.practice
          ? ["WHITE", "BLACK"]
          : store.session?.playerColor ? [store.session.playerColor] : [],
    });

    if (reaction) {
      mascotReact(reaction.kind, reaction.priority, 650, { stare: reaction.stare }, reaction.vars);
    }
  }

  if (["WHITE_WIN", "BLACK_WIN", "DRAW"].includes(state.status)) {
    showResult(state.status);
  } else if (store.reviewPly === null) {
    setNotice(
        state.waitingForOpponent
            ? "Esperando oponente"
            : state.checkedColor
                ? "Jaque"
                : ""
    );
  }

  return true;
}

function shakeBoard(delay = 0) {
  const frame = document.querySelector(".board-frame");
  setTimeout(() => {
    frame.classList.remove("shake");
    void frame.offsetWidth;
    frame.classList.add("shake");
  }, delay * 1000 + 120);
}

function renderResultStats(winner) {
  const moves = store.state?.moves ?? [];
  const captures = (color) => moves.filter((move) => move.capture && move.color === color).length;
  const { lead } = materialBalance(store.state?.pieces);
  byId("stat-moves").textContent = String(Math.ceil(moves.length / 2));
  byId("stat-captures").textContent = `${captures("WHITE")} · ${captures("BLACK")}`;
  byId("stat-material").textContent = !lead ? "=" : `${lead > 0 ? "B" : "N"} +${Math.abs(lead)}`;
  byId("result-rematch").textContent = store.session?.gamemode === "PVP" ? "Nueva sala" : "Revancha";
  const confetti = document.querySelector(".confetti");
  confetti.replaceChildren(
    ...(winner ? Array.from({ length: 18 }, (_, index) => {
      const bit = document.createElement("i");
      bit.style.left = `${(index * 37) % 100}%`;
      bit.style.animationDelay = `${(index % 6) * 0.12}s`;
      bit.style.background = ["var(--accent)", "#edf4ff", "#e05a5a", "#6fc4ff"][index % 4];
      return bit;
    }) : []),
  );
}

function showResult(status) {
  const key = `${store.session.sessionId}:${status}`;

  if (announcedResults.has(key)) {
    return;
  }

  announcedResults.add(key);

  const winner = status === "WHITE_WIN"
      ? "WHITE"
      : status === "BLACK_WIN"
          ? "BLACK"
          : null;

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

  renderResultStats(winner);

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

  audio.play(
      !winner
          ? "draw"
          : title === "Derrota"
              ? "defeat"
              : "victory",
      0.45
  );

  mascotReact(!winner ? "draw" : title === "Derrota" ? "defeat" : "victory", 3, 0);

  if (!byId("result-dialog").open) {
    byId("result-dialog").showModal();
  }
}

function reviewPosition(ply) {
  if (store.selection.busy) {
    return;
  }

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

  store.selection.reset();
  clearMoveWarning();
  renderGame();
}

function selectSquare(tile) {
  const command = store.selection.click(tile, {
    pieces: store.pieces,
    movableColors: store.movableColors,
  });

  if (command.type === "ignore") {
    return;
  }

  clearMoveWarning();

  switch (command.type) {
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
      return submitMove(command);
  }
}

async function loadLegalMoves({ from, token }) {
  const generation = sessionVersion;

  try {
    const response = await api.legal(store.session.sessionId, from);

    if (sessionVersion === generation && store.selection.loaded(token, response)) {
      board.render();
    }
  } catch (error) {
    if (sessionVersion !== generation || error.name === "AbortError") {
      return;
    }

    if (store.selection.failed(token)) {
      setNotice(errorMessage(error));
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
    const button = document.createElement("button");
    button.type = "button";
    button.className = "promotion-choice";
    button.style.height = `${size}px`;
    button.setAttribute("aria-label", `Coronar como ${labels[type]}`);
    button.append(pieceImage(type, color, preferences.theme));
    button.addEventListener("click", () => {
      const command = store.selection.promote(type);
      byId("promotion-layer").hidden = true;

      if (command) {
        submitMove(command);
      } else {
        closePromotionPicker();
      }
    });
    return button;
  });
  const cancel = document.createElement("button");
  cancel.type = "button";
  cancel.className = "promotion-cancel";
  cancel.textContent = "×";
  cancel.setAttribute("aria-label", "Cancelar coronación");
  cancel.addEventListener("click", closePromotionPicker);
  picker.replaceChildren(...(downwards ? [...buttons, cancel] : [cancel, ...buttons.reverse()]));
  byId("promotion-layer").hidden = false;
  audio.play("select");
  buttons[0].focus({ preventScroll: true });
}

function closePromotionPicker() {
  if (byId("promotion-layer").hidden) return;
  byId("promotion-layer").hidden = true;
  store.selection.reset();
  board.render();
}

byId("promotion-layer").addEventListener("click", (event) => {
  if (event.target === event.currentTarget) closePromotionPicker();
});
byId("promotion-layer").addEventListener("keydown", (event) => {
  if (event.key === "Escape") closePromotionPicker();
});

function offerMoveChoice({ options }) {
  const fragment = document.createDocumentFragment();

  for (const option of options) {
    const button = document.createElement("button");
    const name = document.createElement("span");

    button.type = "button";
    button.className = "choice";
    name.className = "choice-name";
    name.textContent = option.label || `Movimiento ${option.moveId + 1}`;
    button.append(name);

    button.addEventListener("click", () => {
      const command = store.selection.choose(option.moveId);
      byId("move-choice-dialog").close();

      if (command) {
        submitMove(command);
      }
    });

    fragment.append(button);
  }

  byId("move-choice-options").replaceChildren(fragment);
  byId("move-choice-dialog").showModal();
}

async function submitMove({ from, to, moveId, promotion = null }) {
  clearMoveWarning();
  liveSession.stop();
  api.cancel();
  board.render();

  const generation = sessionVersion;
  let stateApplied = false;

  try {
    const response = await api.move(
        store.session.sessionId,
        from,
        to,
        moveId,
        promotion
    );

    if (sessionVersion === generation) {
      stateApplied = applyGameState(response);
    }
  } catch (error) {
    if (
        sessionVersion === generation &&
        error.name !== "AbortError"
    ) {
      setNotice(
          error.status === 404
              ? "La jugada se envió, pero falta la consulta de estado para actualizar el tablero."
              : errorMessage(error),
          true
      );
    }
  } finally {
    if (sessionVersion === generation) {
      store.selection.reset();

      if (!stateApplied) {
        board.render();
      }

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
  commentator.reset();

  api.cancel();
  liveSession.stop();
  clearMoveWarning();
  clearTimeout(waitingTimer);

  store.reset();
  store.lastStateSignature = null;
  api.playerId = null;

  byId("promotion-layer").hidden = true;

  for (const id of [
    "move-choice-dialog",
    "result-dialog",
    "invite-dialog",
  ]) {
    byId(id).close();
  }
}

function showScreen(screen) {
  document.body.dataset.screen = screen;
  (screen === "title" ? gameMascot : titleMascot).silence();

  if (screen === "title") {
    title.canContinue = !!store.session;
    title.render();
    title.showMenu();
    hoveredEntry = null;
    titleMascot.talk("greeting");
  }
}

async function activateSession(session, state) {
  store.session = session;
  api.playerId = session.playerId ?? null;

  applyGameState(state);
  sessions.persist(session);
  showScreen("game");
  gameMascot.talk(
      store.state?.waitingForOpponent ? "waiting" : store.state?.moves.length ? "idle" : "start", 1
  );

  setConnection("Conectado", "online");
  schedulePoll();
}

function joinSession(value) {
  const id = parseInvitation(value);

  if (!id) {
    title.setError("Introduce un ID o enlace válido.");
    return;
  }

  return openSession(id, "PVP");
}

async function openSession(id, mode) {
  if (sessionBusy || (!id && !modeDefinition(mode).available)) {
    return;
  }

  sessionBusy = true;
  title.setBusy(true, id ? "join" : mode);
  titleMascot.talk("loading", 1);
  title.setError();
  resetSession();

  const generation = sessionVersion;

  renderGame();

  try {
    const { session, state } = await sessions.open(id, mode);

    if (generation === sessionVersion) {
      await activateSession(session, state);
    }
  } catch (error) {
    if (
        generation === sessionVersion &&
        error.name !== "AbortError"
    ) {
      if (document.body.dataset.screen !== "title") showScreen("title");
      title.setError(errorMessage(error));
      setConnection("No disponible");
    }
  } finally {
    sessionBusy = false;
    title.setBusy(false);
  }
}

byId("menu-button").addEventListener("click", () => {
  if (!sessionBusy) {
    showScreen("title");
  }
});

byId("result-rematch").addEventListener("click", () => {
  const mode = store.session?.gamemode ?? "PRACTICE";
  byId("result-dialog").close();
  openSession(null, mode);
});

byId("result-new").addEventListener("click", () => {
  byId("result-dialog").close();
  showScreen("title");
});

for (const button of document.querySelectorAll("[data-close]")) {
  button.addEventListener("click", () => {
    byId(button.dataset.close).close();
  });
}

byId("move-choice-dialog").addEventListener("close", () => {
  if (store.selection.cancelChoice()) {
    board.render();
  }
});

byId("flip").addEventListener("click", () => {
  store.flipped = !store.flipped;
  renderGame();
});

byId("return-live").addEventListener("click", () => {
  reviewPosition(null);
});

byId("history-live").addEventListener("click", () => {
  reviewPosition(null);
});

byId("history-start").addEventListener("click", () => {
  reviewPosition(0);
});

byId("history-prev").addEventListener("click", () => {
  reviewPosition(
      Math.max(
          0,
          (store.reviewPly ?? store.state.moves.length) - 1
      )
  );
});

byId("history-next").addEventListener("click", () => {
  const next = (store.reviewPly ?? store.state.moves.length) + 1;

  reviewPosition(
      next >= store.state.moves.length ? null : next
  );
});

document.addEventListener("keydown", (event) => {
  if (
      document.body.dataset.screen !== "game" ||
      event.altKey || event.ctrlKey || event.metaKey ||
      document.querySelector("dialog[open]") ||
      event.target.closest?.("input, select, textarea, .board")
  ) {
    return;
  }

  const button = byId({
    ArrowLeft: "history-prev",
    ArrowRight: "history-next",
    Home: "history-start",
    End: "history-live",
  }[event.key]);

  if (button && !button.disabled) {
    event.preventDefault();
    button.click();
  }
});

byId("leave").addEventListener("click", () => {
  if (!sessionBusy) {
    showScreen("title");
  }
});

byId("retry").addEventListener("click", () => {
  liveSession.failures = 0;

  if (store.session) {
    refresh();
  } else {
    showScreen("title");
  }
});

async function inviteOrigin() {
  if (!loopbackHost()) return location.origin;
  const found = await serverAddress(api);
  if (!found) return null;
  try {
    return new URL(`${location.protocol}//${found.address}${found.port ? `:${found.port}` : ""}`).origin;
  } catch {
    return null;
  }
}

byId("invite").addEventListener("click", async () => {
  if (!store.session) {
    return;
  }

  const sessionId = store.session.sessionId;
  byId("invite-link").value = "";
  byId("invite-link").placeholder = "Preparando el enlace…";
  byId("invite-copy").disabled = true;
  byId("invite-status").textContent = "";
  byId("invite-dialog").showModal();

  const origin = await inviteOrigin();
  if (store.session?.sessionId !== sessionId) return;
  byId("invite-link").value = origin ? sessions.invitation(sessionId, origin) : "";
  byId("invite-link").placeholder = "";
  byId("invite-copy").disabled = !origin;
  byId("invite-status").textContent = origin
      ? ""
      : "No se pudo obtener la dirección de este equipo en la red. Inténtalo de nuevo.";
});

byId("invite-copy").addEventListener("click", async () => {
  const link = byId("invite-link").value;
  if (!link) return;

  let copied = false;
  try {
    await navigator.clipboard.writeText(link);
    copied = true;
  } catch {
    byId("invite-link").select();
    copied = document.execCommand?.("copy") ?? false;
  }

  byId("invite-status").textContent = copied
      ? "Invitación copiada."
      : "No se pudo copiar. Selecciona el enlace y cópialo a mano.";
});

function applyMascot() {
  document.body.dataset.mascot = preferences.mascot ? "on" : "off";
  if (!preferences.mascot) {
    titleMascot.silence();
    gameMascot.silence();
  }
  byId("mascot-enabled").checked = preferences.mascot;
}

for (const button of document.querySelectorAll(".mascot-dismiss")) {
  button.addEventListener("click", (event) => {
    event.stopPropagation();
    preferences.mascot = false;
    applyMascot();
    savePreferences(preferences);
  });
}

function updateAudio() {
  audio.updatePreferences();
  music.sync();
  savePreferences(preferences);
  renderAppearance(preferences);
  syncSettings();
}

for (const [id, key] of [
  ["theme-select", "theme"],
  ["volume", "volume"],
  ["music-volume", "musicVolume"],
  ["music-enabled", "music"],
  ["muted", "muted"],
  ["mascot-enabled", "mascot"],
]) {
  const element = byId(id);
  const checkbox = element.type === "checkbox";
  const numeric = element.type === "range";

  if (checkbox) {
    element.checked = preferences[key];
  } else {
    element.value = preferences[key];
  }

  element.addEventListener("input", () => {
    preferences[key] = checkbox
        ? element.checked
        : numeric
            ? Number(element.value)
            : element.value;

    syncSettings();

    if (key === "mascot") {
      applyMascot();
      savePreferences(preferences);
      if (preferences.mascot) {
        const mascot = document.body.dataset.screen === "game" ? gameMascot : titleMascot;
        mascot.talk("greeting");
      }
      return;
    }

    if (key === "theme") {
      applyTheme(preferences.theme);
      savePreferences(preferences);
      renderGame();
      titleMascot.dress(preferences.theme);
      gameMascot.dress(preferences.theme);
      const mascot = document.body.dataset.screen === "game" ? gameMascot : titleMascot;
      mascot.talk(`theme-${preferences.theme}`, 1);
      return;
    }

    updateAudio();
  });
}

byId("volume").addEventListener("change", () => audio.play("move"));

byId("sound-toggle").addEventListener("click", () => {
  preferences.muted = !preferences.muted;
  byId("muted").checked = preferences.muted;
  updateAudio();
});

byId("music-toggle").addEventListener("click", async () => {
  preferences.music = !preferences.music;
  byId("music-enabled").checked = preferences.music;
  await audio.unlock();
  updateAudio();
});

byId("settings-open").addEventListener("click", () => {
  syncSettings();
  byId("settings-dialog").showModal();
  byId("settings-dialog").querySelector('[role="tab"][aria-selected="true"]').focus({ preventScroll: true });
});

async function unlockAudio() {
  await audio.unlock();
  music.sync();
}

document.addEventListener("pointerdown", unlockAudio, { once: true });
document.addEventListener("keydown", unlockAudio, { once: true });

function pauseAudio() {
  music.stop();
  audio.context?.suspend().catch(() => {});
}

document.addEventListener("visibilitychange", async () => {
  if (document.hidden) {
    pauseAudio();
    return;
  }

  try {
    await audio.context?.resume();
  } catch {}
  music.sync();
});

window.addEventListener("pagehide", () => {
  api.cancel();
  liveSession.stop();
  pauseAudio();
});

applyMascot();
syncSettings();
renderGame();
showScreen("title");

const invitation = parseInvitation(
    new URL(location.href).searchParams.get("session") ?? ""
);

if (invitation) {
  const saved = sessions.saved(invitation);

  if (saved) {
    resetSession();

    const generation = sessionVersion;

    api.playerId = saved.playerId;
    setConnection("Conectando…");

    api.state(invitation)
        .then((response) => {
          if (generation === sessionVersion) {
            activateSession(saved, response);
          }
        })
        .catch((error) => {
          if (
              generation === sessionVersion &&
              error.name !== "AbortError"
          ) {
            if ([404, 500].includes(error.status)) {
              sessions.forget(invitation);
              setConnection("Sin partida");
              title.showMenu();
              title.setError("La partida anterior ya no existe en el servidor. Empieza una nueva.");
              return;
            }
            title.showJoin(location.href);
            title.setError(errorMessage(error));
          }
        });
  } else {
    title.showJoin(location.href);
  }
}
