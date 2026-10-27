import { ChessApi, HttpStatus } from "./modules/api.js";
import { GameStore, labels, normalizeState, stateSignature } from "./modules/state.js";
import { BoardView } from "./modules/board.js";
import { gamePiece, logoUrl } from "./modules/assets.js";
import { setColorSwap, shownColor } from "./modules/colors.js";
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
import { cpuLevels, modeDefinition } from "./modules/modes.js";
import { legalOptions, outOfTurnPawnPush } from "./modules/cpu.js";
import { cheatChances, cleanPlies, flipLead, freeNeighbours, materialLead, secondMove, teleport } from "./modules/cheats.js";
import { AlfiBoss, fullEnergy } from "./modules/alfi-boss.js";
import { rivalKind } from "./modules/alfi-reactions.js";
import { profileLine } from "./modules/alfi-profile.js";
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
const alfi = new AlfiBoss(audio, music, preferences);
// El aguante de Alfi se guarda con la sesión para que una recarga no se lo rellene.
alfi.onEnergyChange = (energy) => {
  if (!store.session?.cpu) return;
  store.session.alfiEnergy = energy;
  sessions.persist(store.session);
};
music.onBeat = (beat) => alfi.beat(beat);
// En el modo Alfi quien comenta es el rival sentado enfrente, no la mascota del panel.
const activeMascot = () => (alfi.active ? alfi.seat : gameMascot);
let hoveredEntry = null;
let cpuTimer = null;
let cpuFailures = 0;

const title = new TitleScreen({
  onStart: (mode) => mode === "PVP"
      ? byId("pvp-dialog").showModal()
      : mode === "PVCPU" ? byId("cpu-dialog").showModal() : openSession(null, mode),
  onJoin: joinSession,
  onContinue: () => {
    showScreen("game");
    if (alfi.active && !alfi.asleep) setTimeout(() => alfi.seat.talk("bossReturn", 2), 500);
  },
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
    if (document.body.dataset.screen !== "game" || store.session?.alfi === "revealing") return;
    // Dormido no comenta la partida; como mucho, murmura en sueños.
    if (alfi.active && alfi.asleep) {
      if (Math.random() < 0.25) alfi.seat.talk("bossSleepTalk", 0);
      return;
    }
    activeMascot().talk(kind, priority, options, vars);
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
    if (error.status === HttpStatus.NOT_FOUND) {
      liveSession.stop();
      setConnection("Sin partida");
      setNotice("La partida ya no existe en el servidor. Vuelve al menú y empieza una nueva.");
      return;
    }

    setConnection("Sin conexión");
    setNotice(error.message, true);
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
      userColors: store.practice || store.local
          ? ["WHITE", "BLACK"]
          : store.session?.playerColor ? [store.session.playerColor] : [],
    });

    if (reaction && alfi.active && store.session?.cpu) {
      // Contra Alfi comenta el rival, con sus propias frases.
      const kind = rivalKind(reaction.kind, { move, after: state, cpuColor: store.session.cpu.color });
      if (kind) mascotReact(kind, reaction.priority, 650, {}, reaction.vars);
    } else if (reaction) {
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

  faceSideToMove();
  scheduleCpu();
  scheduleAlfiWait();
  return true;
}

// Si el jugador tarda en mover contra Alfi, él le mete prisa (unas pocas veces por jugada).
let alfiWaitTimer = null;
function scheduleAlfiWait(nudges = 0) {
  clearTimeout(alfiWaitTimer);
  const session = store.session;
  if (!session?.cpu || session.alfi !== "revealed" || nudges >= 3) return;
  const ply = store.state?.moves.length;
  alfiWaitTimer = setTimeout(() => {
    const stillWaiting = store.state?.moves.length === ply && store.state?.status === "ACTIVE" &&
      store.state.turn === session.playerColor && !store.cpuBusy;
    if (!stillWaiting || document.body.dataset.screen !== "game") return;
    if (alfi.active && !alfi.asleep && !alfi.seat.timer) alfi.seat.talk("bossWaiting", 1);
    scheduleAlfiWait(nudges + 1);
  }, nudges ? 30000 : 22000);
}

// Asiento de máquina al que le toca mover: el rival en PvCPU/PvAI o cualquiera de los dos en IA contra IA.
function machineToMove() {
  const session = store.session;
  const turn = store.state?.turn;
  if (session?.cpu?.color === turn) return session.cpu;
  return session?.machines?.find((machine) => machine.color === turn) ?? null;
}

// Las máquinas juegan cuando les toca; la jugada la calcula el servidor. En la dificultad
// máxima, antes de nada, entra Alfi.
function scheduleCpu() {
  clearTimeout(cpuTimer);
  const session = store.session;
  if (!(session?.cpu || session?.machines) || store.cpuBusy || store.state?.status !== "ACTIVE") return;
  if (store.state.waitingForOpponent) return;
  if (session.alfi === "hidden") {
    revealAlfi();
    return;
  }
  const machine = machineToMove();
  if (!machine) return;
  const generation = sessionVersion;
  // Si el servidor falla, cada reintento espera más (hasta 15 s).
  const backoff = cpuFailures ? Math.min(15000, 1000 * 2 ** cpuFailures) : 0;
  cpuTimer = setTimeout(() => playCpu(generation, machine), 700 + Math.random() * 600 + backoff);
}

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// ¿Puede Alfi hacer esta trampa ahora? Solo contra él, ya revelado, con la partida en juego, pasadas
// las primeras jugadas, con aguante y si la suerte acompaña.
function wantsCheat(kind) {
  const session = store.session;
  return alfi.active && session?.alfi === "revealed" && store.state?.status === "ACTIVE" &&
    store.state.moves.length >= cleanPlies && alfi.canAfford(kind) && Math.random() < cheatChances[kind];
}

async function playCpu(generation, machine) {
  if (generation !== sessionVersion) return;
  store.cpuBusy = true;
  try {
    const rival = machine === store.session.cpu;
    if (rival && await alfiFlip()) return;
    // La doble jugada sale de las jugadas legales de antes de la primera: después ya no es su turno y
    // legal-moves no le ofrece nada. Solo se piden si Alfi va a hacer la trampa.
    const double = rival && wantsCheat("double");
    const options = double
      ? await legalOptions(api, store.session.sessionId, store.state.pieces, machine.color)
      : null;
    if (generation !== sessionVersion) return;
    const move = await submitMachineMove(machine);
    cpuFailures = move ? 0 : cpuFailures + 1;
    // Doble jugada: el turno ya es del jugador, pero Alfi vuelve a mover.
    if (move && options && generation === sessionVersion) {
      const second = secondMove(options, move, store.state.pieces);
      if (second && alfi.cheat("double")) {
        await wait(700);
        if (generation === sessionVersion && store.state?.status === "ACTIVE") await submitMove(second);
      }
    }
  } finally {
    if (generation === sessionVersion) {
      store.cpuBusy = false;
      renderGame();
      scheduleCpu();
    }
  }
}

// Si el jugador va ganando por mucho, Alfi gira el tablero una vez: cada uno se queda con las piezas
// del otro. Solo cambian los asientos en este equipo; el servidor no valida quién mueve qué.
async function alfiFlip() {
  const session = store.session;
  if (session.flipped || !wantsCheat("flip") || materialLead(store.state.pieces, session.playerColor) < flipLead) return false;
  if (!alfi.cheat("flip")) return false;
  session.flipped = true;
  [session.playerColor, session.cpu.color] = [session.cpu.color, session.playerColor];
  sessions.persist(session);
  alfi.side = session.cpu.color;
  await wait(600);
  await board.spin();
  // Alfi sigue viéndose blanco: los colores vuelven a cambiar y su corona pasa a su nuevo rey.
  await board.colorWave(() => alfi.rebrand(true), () => audio.play("scramble"));
  renderGame();
  return true;
}

// En el modo Alfi las jugadas del jugador pasan por sus trampas: puede esquivar antes y rebobinar después.
async function playerMove(command) {
  if (!alfi.active || !store.session?.cpu) return submitMove(command);
  const generation = sessionVersion;
  store.cpuBusy = true;
  try {
    await alfiDodge(command);
    if (generation !== sessionVersion) return;
    await submitMove(command);
    if (generation === sessionVersion) await alfiRewind(command);
  } finally {
    if (generation === sessionVersion) {
      store.cpuBusy = false;
      renderGame();
      scheduleCpu();
    }
  }
}

// Esquiva: la pieza atacada se aparta a una casilla vecina libre y el golpe cae en el vacío.
async function alfiDodge(command) {
  const target = store.state.pieces.get(command.to);
  if (!target || target.color !== store.session.cpu.color || target.type === "KING" || !wantsCheat("dodge")) return;
  const free = freeNeighbours(store.state.pieces, command.to).filter((tile) => tile !== command.from);
  if (!free.length || !alfi.cheat("dodge")) return;
  await wait(250);
  await submitMove(teleport(command.to, free[Math.floor(Math.random() * free.length)]));
  await wait(350);
}

// Rebobinar: si la jugada del jugador le da jaque (y no capturó, coronó ni enrocó), la pieza vuelve a
// su casilla y el turno pasa a Alfi.
async function alfiRewind(command) {
  const moved = store.state?.pieces.get(command.to);
  const last = store.state?.moves.at(-1);
  const castled = moved?.type === "KING" && Math.abs(command.from.charCodeAt(0) - command.to.charCodeAt(0)) > 1;
  if (!moved || last?.capture || command.promotion || castled) return;
  if (store.state.checkedColor !== store.session.cpu.color || !wantsCheat("rewind")) return;
  if (!alfi.cheat("rewind")) return;
  await wait(500);
  await submitMove(teleport(command.to, command.from));
}

// Cuántas veces se ha enfrentado el jugador a Alfi en este navegador (para que lo reconozca).
function alfiEncounters(add = 0) {
  try {
    const count = Number(localStorage.getItem("archchess.alfi.encounters")) || 0;
    if (add) localStorage.setItem("archchess.alfi.encounters", String(count + add));
    return count + add;
  } catch {
    return 0;
  }
}

async function revealAlfi() {
  const generation = sessionVersion;
  const session = store.session;
  alfi.side = session.cpu.color;
  store.cpuBusy = true;
  session.alfi = "revealing";
  gameMascot.silence();
  renderGame();

  await alfi.reveal({
    // Su primera trampa: mueve las negras aunque sea el turno del jugador.
    cheat: async () => {
      const move = outOfTurnPawnPush(store.state.pieces, session.cpu.color);
      if (move) await submitMove(move);
    },
    alive: () => generation === sessionVersion,
    encounters: alfiEncounters(),
    onRevealed: () => {
      session.alfi = "revealed";
      sessions.persist(session);
      alfiEncounters(1);
    },
    // Las piezas de Alfi pasan a verse blancas y las del jugador negras, fila a fila.
    swapColors: async (onRow) => {
      await board.colorWave(() => setColorSwap(true), onRow);
      renderGame();
    },
    // Su rey cae como capturado y en su casilla aterriza la miniatura de Alfi con corona.
    crownKing: async () => {
      const tile = [...store.state.pieces].find(([, item]) => item.type === "KING" && item.color === session.cpu.color)?.[0];
      if (!tile) return;
      audio.play("capture");
      await board.topple(tile);
      alfi.crown();
      board.render();
      audio.play("promotion");
      await board.dropIn(tile);
      renderGame();
    },
  });
  if (generation !== sessionVersion) return;
  store.cpuBusy = false;
  renderGame();
  scheduleCpu();
  scheduleAlfiWait();
  // Si algún día hay perfiles, Alfi demuestra que se acuerda de ti.
  const comment = profileLine(session.profile);
  if (comment) setTimeout(() => alfi.active && alfi.seat.talk(comment.kind, 2, {}, comment.vars), 5000);
}

// Multijugador local: cuando cambia el turno, el tablero gira para dejar abajo al bando que mueve.
async function faceSideToMove() {
  if (!store.local || store.turning) return;

  const turn = store.state?.turn;
  if (store.localSide === null) {
    store.localSide = turn ?? "WHITE";
    renderGame();
    return;
  }
  if (store.state?.status !== "ACTIVE" || turn === store.localSide) return;

  const generation = sessionVersion;
  store.turning = true;
  board.render();

  await board.settle();
  await new Promise((resolve) => setTimeout(resolve, 250));
  if (generation !== sessionVersion) return;

  store.localSide = store.state.turn;
  store.reviewPly = null;
  renderMatch(store, preferences);
  historyView.render();
  await board.spin();
  if (generation !== sessionVersion) return;

  store.turning = false;
  renderGame();
  faceSideToMove();
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

async function showResult(status) {
  const key = `${store.session.sessionId}:${status}`;

  if (announcedResults.has(key)) {
    return;
  }

  announcedResults.add(key);

  // Contra Alfi, antes del resultado hay escena final.
  const finale = alfi.active && store.session?.cpu && store.session.alfi === "revealed";
  if (finale) {
    const generation = sessionVersion;
    const winnerColor = { WHITE_WIN: "WHITE", BLACK_WIN: "BLACK" }[status];
    const outcome = !winnerColor ? "draw" : winnerColor === store.session.playerColor ? "win" : "loss";
    await alfi.ending(outcome, () => generation === sessionVersion);
    if (generation !== sessionVersion) return;
  }

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

  byId("result-title").textContent = finale
      ? (!winner ? "Tablas con Alfi" : title === "Derrota" ? "Alfi gana" : "Le has ganado a Alfi")
      : title;

  byId("result-detail").textContent = finale
      ? (!winner ? "Nadie gana. Alfi dice que él sí." : title === "Derrota" ? "Otra vez será. O no." : "Ni sus trampas lo han salvado.")
      : winner
          ? `Ganan las ${shownColor(winner) === "WHITE" ? "blancas" : "negras"}`
          : "Tablas";

  renderResultStats(winner);

  byId("result-card").className =
      `result-card ${shownColor(winner) === "BLACK" ? "black" : winner ? "white" : "draw"}`;

  const resultImage = winner
      ? gamePiece("KING", winner, preferences.theme)
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

  if (!finale) mascotReact(!winner ? "draw" : title === "Derrota" ? "defeat" : "victory", 3, 0);

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
  const movableColor = store.movableColor;
  const command = store.selection.click(tile, {
    pieces: store.pieces,
    interactive: store.interactive &&
        (!movableColor || store.pieces.get(tile)?.color === movableColor),
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
      return playerMove(command);
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
    const button = document.createElement("button");
    button.type = "button";
    button.className = "promotion-choice";
    button.style.height = `${size}px`;
    button.setAttribute("aria-label", `Coronar como ${labels[type]}`);
    button.append(gamePiece(type, color, preferences.theme));
    button.addEventListener("click", () => {
      const command = store.selection.promote(type);
      byId("promotion-layer").hidden = true;

      if (command) {
        playerMove(command);
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
        playerMove(command);
      }
    });

    fragment.append(button);
  }

  byId("move-choice-options").replaceChildren(fragment);
  byId("move-choice-dialog").showModal();
}

function submitMove({ from, to, moveId, promotion = null }) {
  return sendMove(() => api.move(
      store.session.sessionId,
      from,
      to,
      moveId,
      promotion
  ));
}

// Pide al servidor la jugada de esa máquina y devuelve la jugada hecha (o null si falla).
async function submitMachineMove(machine) {
  let played = null;
  await sendMove(async () => {
    const { move, state } = await api.machineMove(
        store.session.sessionId,
        machine.playerType ?? "CPU",
        machine.playerId
    );
    played = move;
    return state;
  });
  return played;
}

async function sendMove(request) {
  clearMoveWarning();
  liveSession.stop();
  api.cancel();
  board.render();

  const generation = sessionVersion;
  let stateApplied = false;

  try {
    const response = await request();

    if (sessionVersion === generation) {
      stateApplied = applyGameState(response);
    }
  } catch (error) {
    if (
        sessionVersion === generation &&
        error.name !== "AbortError"
    ) {
      setNotice(error.message, true);
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

  clearTimeout(cpuTimer);
  cpuFailures = 0;
  clearTimeout(alfiWaitTimer);
  alfi.abort();

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
  alfi.apply(screen === "game" && store.session?.alfi === "revealed");

  if (screen === "title") {
    title.canContinue = !!store.session;
    title.render();
    title.showMenu();
    hoveredEntry = null;
    titleMascot.talk("greeting");
  }
}

async function activateSession(session, state) {
  // Una partida contra Alfi ya empezada no repite la entrada.
  if (session.alfi === "hidden" && normalizeState(state).moves.length) session.alfi = "revealed";
  store.session = session;
  if (session.cpu) {
    alfi.side = session.cpu.color;
    alfi.setEnergy(session.alfiEnergy ?? fullEnergy);
  }
  store.localSide = null;
  api.playerId = session.playerId ?? null;

  applyGameState(state);
  sessions.persist(session);
  showScreen("game");
  if (alfi.active && session.alfi === "revealed") {
    // Al recargar una partida contra Alfi, te recibe él (salvo que esté dormido).
    if (!alfi.asleep) alfi.seat.talk("bossReturn", 1);
  } else {
    activeMascot().talk(
        store.state?.waitingForOpponent ? "waiting" : store.state?.moves.length ? "idle" : "start", 1
    );
  }

  setConnection("Conectado", "online");
  schedulePoll();
  scheduleCpu();
  scheduleAlfiWait();
}

function joinSession(value) {
  const id = parseInvitation(value);

  if (!id) {
    title.setError("Introduce un ID o enlace válido.");
    return;
  }

  return openSession(id, "PVP");
}

async function openSession(id, mode, difficulty) {
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
    const { session, state } = await sessions.open(id, mode, difficulty);

    if (generation === sessionVersion) {
      await activateSession(session, state);
      if (!id && session.gamemode === "PVP") openInvite();
    }
  } catch (error) {
    if (
        generation === sessionVersion &&
        error.name !== "AbortError"
    ) {
      if (document.body.dataset.screen !== "title") showScreen("title");
      title.setError(error.message);
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
  openSession(null, mode, store.session?.difficulty);
});

for (const level of cpuLevels) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "title-option";
  button.innerHTML = `<img class="title-option-art" src="./assets/ui/mode-pvcpu.png" alt="" aria-hidden="true" />
    <span><strong></strong><small></small></span>`;
  button.querySelector("strong").textContent = level.name;
  button.querySelector("small").textContent = level.detail;
  button.addEventListener("click", () => {
    byId("cpu-dialog").close();
    openSession(null, "PVCPU", level.id);
  });
  byId("cpu-levels").append(button);
}

for (const [id, mode] of [["pvp-invite", "PVP"], ["pvp-local", "LOCAL"]]) {
  byId(id).addEventListener("click", () => {
    byId("pvp-dialog").close();
    openSession(null, mode);
  });
}

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

byId("invite").addEventListener("click", openInvite);

async function openInvite() {
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
  if (origin) byId("invite-copy").focus();
}

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
    const value = checkbox
        ? element.checked
        : numeric
            ? Number(element.value)
            : element.value;

    if (alfi.resist(key, value)) {
      syncControls();
      return;
    }
    preferences[key] = value;

    syncSettings();

    if (key === "mascot") {
      applyMascot();
      savePreferences(preferences);
      if (preferences.mascot) {
        const mascot = document.body.dataset.screen === "game" ? activeMascot() : titleMascot;
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
      alfi.dress(preferences.theme);
      const mascot = document.body.dataset.screen === "game" ? activeMascot() : titleMascot;
      mascot.talk(`theme-${preferences.theme}`, 1);
      return;
    }

    updateAudio();
  });
}

byId("volume").addEventListener("change", () => audio.play("move"));

// Refleja en los controles de ajustes los valores actuales (p. ej. tras imponerlos Alfi).
function syncControls() {
  for (const [id, key] of [
    ["theme-select", "theme"],
    ["volume", "volume"],
    ["music-volume", "musicVolume"],
    ["music-enabled", "music"],
    ["muted", "muted"],
    ["mascot-enabled", "mascot"],
  ]) {
    const element = byId(id);
    if (element.type === "checkbox") element.checked = preferences[key];
    else element.value = preferences[key];
  }
  syncSettings();
}

alfi.onRulesChanged = () => {
  applyTheme(preferences.theme);
  applyMascot();
  audio.updatePreferences();
  music.sync();
  renderAppearance(preferences);
  syncControls();
  renderGame();
};

byId("sound-toggle").addEventListener("click", () => {
  if (alfi.resist("muted", !preferences.muted)) return syncControls();
  preferences.muted = !preferences.muted;
  byId("muted").checked = preferences.muted;
  updateAudio();
});

byId("music-toggle").addEventListener("click", async () => {
  if (alfi.resist("music", !preferences.music)) return syncControls();
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
            if (error.status === HttpStatus.NOT_FOUND) {
              sessions.forget(invitation);
              setConnection("Sin partida");
              title.showMenu();
              title.setError("La partida anterior ya no existe en el servidor. Empieza una nueva.");
              return;
            }
            title.showJoin(location.href);
            title.setError(error.message);
          }
        });
  } else {
    title.showJoin(location.href);
  }
}
