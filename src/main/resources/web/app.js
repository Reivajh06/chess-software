"use strict";

/*
 * Change this value if your Javalin API uses another host or port.
 * If the frontend is served by Javalin itself, use "/api" instead.
 */
const API_BASE_URL = "http://localhost:7070/api";

const FALLBACK_OPTIONS = {
  gameModes: [
    { value: "PLAYER_VS_CPU", label: "Player vs CPU" },
    { value: "PLAYER_VS_PLAYER", label: "Player vs Player" },
    { value: "PLAYER_VS_AI", label: "Player vs AI" },
    { value: "AI_VS_AI", label: "AI vs AI" }
  ],
  difficulties: [
    { value: "EASY", label: "Easy" },
    { value: "MEDIUM", label: "Medium" },
    { value: "HARD", label: "Hard" },
    { value: "EXPERT", label: "Expert" }
  ]
};

/* Filled monochrome chess glyphs are used for both colors and recolored with CSS.
 * This avoids multicolor emoji rendering while keeping the frontend dependency-free.
 */
const PIECE_GLYPHS = {
  KING: "♚︎",
  QUEEN: "♛︎",
  ROOK: "♜︎",
  BISHOP: "♝︎",
  KNIGHT: "♞︎",
  PAWN: "♟︎"
};

const TERMINAL_STATUSES = new Set([
  "CHECKMATE",
  "STALEMATE",
  "DRAW",
  "RESIGNED",
  "ABORTED"
]);

const state = {
  options: FALLBACK_OPTIONS,
  game: null,
  selectedSquare: null,
  legalMoves: [],
  waitingForMove: false,
  promotionResolver: null
};

const ui = {
  board: document.querySelector("#board"),
  gameModeSelect: document.querySelector("#gameModeSelect"),
  difficultySelect: document.querySelector("#difficultySelect"),
  difficultyField: document.querySelector("#difficultyField"),
  startGameButton: document.querySelector("#startGameButton"),
  advanceTurnButton: document.querySelector("#advanceTurnButton"),
  gameIdBadge: document.querySelector("#gameIdBadge"),
  setupHint: document.querySelector("#setupHint"),
  connectionDot: document.querySelector("#connectionDot"),
  connectionText: document.querySelector("#connectionText"),
  positionTitle: document.querySelector("#positionTitle"),
  positionMessage: document.querySelector("#positionMessage"),
  turnColorBadge: document.querySelector("#turnColorBadge"),
  localName: document.querySelector("#localName"),
  localMeta: document.querySelector("#localMeta"),
  opponentName: document.querySelector("#opponentName"),
  opponentMeta: document.querySelector("#opponentMeta"),
  localTurn: document.querySelector("#localTurn"),
  opponentTurn: document.querySelector("#opponentTurn"),
  capturedByWhite: document.querySelector("#capturedByWhite"),
  capturedByBlack: document.querySelector("#capturedByBlack"),
  moveHistory: document.querySelector("#moveHistory"),
  moveCount: document.querySelector("#moveCount"),
  promotionOverlay: document.querySelector("#promotionOverlay"),
  promotionChoices: document.querySelector("#promotionChoices"),
  resultOverlay: document.querySelector("#resultOverlay"),
  resultEyebrow: document.querySelector("#resultEyebrow"),
  resultTitle: document.querySelector("#resultTitle"),
  resultMessage: document.querySelector("#resultMessage"),
  resultNewGameButton: document.querySelector("#resultNewGameButton"),
  toast: document.querySelector("#toast")
};

class ApiClient {
  static async request(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        ...(options.headers || {})
      }
    });

    if (!response.ok) {
      let message = `Request failed with HTTP ${response.status}`;
      try {
        const error = await response.json();
        message = error.message || error.error || message;
      } catch (_) {
        // The response did not contain JSON. Keep the HTTP status message.
      }
      throw new Error(message);
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  }

  static getOptions() {
    return this.request("/game-options");
  }

  static createGame(config) {
    return this.request("/games", {
      method: "POST",
      body: JSON.stringify(config)
    });
  }

  static getGame(gameId) {
    return this.request(`/games/${encodeURIComponent(gameId)}`);
  }

  static getLegalMoves(gameId, square) {
    const params = new URLSearchParams({ from: square });
    return this.request(`/games/${encodeURIComponent(gameId)}/legal-moves?${params}`);
  }

  static makeMove(gameId, move) {
    return this.request(`/games/${encodeURIComponent(gameId)}/moves`, {
      method: "POST",
      body: JSON.stringify(move)
    });
  }

  static advanceTurn(gameId) {
    return this.request(`/games/${encodeURIComponent(gameId)}/turns/advance`, {
      method: "POST",
      body: JSON.stringify({})
    });
  }
}

function filesForOrientation(orientation) {
  return orientation === "BLACK"
    ? ["h", "g", "f", "e", "d", "c", "b", "a"]
    : ["a", "b", "c", "d", "e", "f", "g", "h"];
}

function ranksForOrientation(orientation) {
  return orientation === "BLACK"
    ? [1, 2, 3, 4, 5, 6, 7, 8]
    : [8, 7, 6, 5, 4, 3, 2, 1];
}

function squareColor(file, rank) {
  const fileIndex = file.charCodeAt(0) - 97;
  return (fileIndex + rank) % 2 === 0 ? "light" : "dark";
}

function makePreviewPosition() {
  const pieces = [];
  const backRank = ["ROOK", "KNIGHT", "BISHOP", "QUEEN", "KING", "BISHOP", "KNIGHT", "ROOK"];
  const files = ["a", "b", "c", "d", "e", "f", "g", "h"];

  files.forEach((file, index) => {
    pieces.push({ id: `preview-w-${file}-1`, type: backRank[index], color: "WHITE", square: `${file}1` });
    pieces.push({ id: `preview-w-${file}-2`, type: "PAWN", color: "WHITE", square: `${file}2` });
    pieces.push({ id: `preview-b-${file}-7`, type: "PAWN", color: "BLACK", square: `${file}7` });
    pieces.push({ id: `preview-b-${file}-8`, type: backRank[index], color: "BLACK", square: `${file}8` });
  });

  return pieces;
}

function getPieceAt(square) {
  return state.game?.board?.find(piece => piece.square === square) || null;
}

function isTerminal(game = state.game) {
  return Boolean(game && TERMINAL_STATUSES.has(game.status));
}

function isHumanTurn() {
  return state.game?.currentController === "HUMAN" && !isTerminal();
}

function populateSelect(select, options) {
  select.innerHTML = "";
  for (const option of options) {
    const element = document.createElement("option");
    element.value = option.value;
    element.textContent = option.label || prettifyEnum(option.value);
    select.append(element);
  }
}

function prettifyEnum(value) {
  if (!value) return "--";
  return String(value)
    .toLowerCase()
    .split("_")
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

function modeNeedsDifficulty(mode) {
  return mode !== "PLAYER_VS_PLAYER";
}

function updateDifficultyVisibility() {
  const needsDifficulty = modeNeedsDifficulty(ui.gameModeSelect.value);
  ui.difficultyField.classList.toggle("hidden", !needsDifficulty);
  ui.difficultySelect.disabled = !needsDifficulty;

  if (ui.gameModeSelect.value === "AI_VS_AI") {
    ui.setupHint.textContent = "This frontend uses one difficulty value for both AI sides. Use per-player settings later if you want asymmetric AI matches.";
  } else if (ui.gameModeSelect.value === "PLAYER_VS_PLAYER") {
    ui.setupHint.textContent = "Both sides are controlled by humans. The backend may still randomize which side starts at the bottom of the board.";
  } else {
    ui.setupHint.textContent = "The backend randomizes the human player's color.";
  }
}

function renderBoard() {
  const orientation = state.game?.boardOrientation || state.game?.humanColor || "WHITE";
  const boardPieces = state.game?.board || makePreviewPosition();
  const pieceMap = new Map(boardPieces.map(piece => [piece.square, piece]));
  const legalMap = new Map(state.legalMoves.map(move => [move.to, move]));
  const lastMove = state.game?.moveHistory?.at(-1) || null;

  ui.board.innerHTML = "";

  const files = filesForOrientation(orientation);
  const ranks = ranksForOrientation(orientation);

  ranks.forEach((rank, rankIndex) => {
    files.forEach((file, fileIndex) => {
      const coordinate = `${file}${rank}`;
      const square = document.createElement("button");
      const piece = pieceMap.get(coordinate);
      const legalMove = legalMap.get(coordinate);

      square.type = "button";
      square.className = `square ${squareColor(file, rank)}`;
      square.dataset.coordinate = coordinate;
      square.setAttribute("role", "gridcell");
      square.setAttribute("aria-label", buildSquareLabel(coordinate, piece, legalMove));

      const showCoordinate = fileIndex === 0 || rankIndex === 7;
      if (showCoordinate) square.classList.add("show-coordinate");
      if (state.selectedSquare === coordinate) square.classList.add("selected");
      if (lastMove && (lastMove.from === coordinate || lastMove.to === coordinate)) square.classList.add("last-move");

      if (legalMove) {
        const moveKind = legalMove.kind || "NORMAL";
        if (moveKind === "CAPTURE" || moveKind === "EN_PASSANT" || piece) {
          square.classList.add("legal-capture");
        } else {
          square.classList.add("legal");
        }
      }

      if (piece) {
        const glyph = document.createElement("span");
        glyph.className = `piece ${piece.color.toLowerCase()}`;
        glyph.textContent = PIECE_GLYPHS[piece.type] || "?";
        glyph.setAttribute("aria-hidden", "true");
        square.append(glyph);
      }

      square.addEventListener("click", () => handleSquareClick(coordinate));
      ui.board.append(square);
    });
  });
}

function buildSquareLabel(coordinate, piece, legalMove) {
  const parts = [coordinate];
  if (piece) parts.push(`${prettifyEnum(piece.color)} ${prettifyEnum(piece.type)}`);
  if (legalMove) parts.push("legal destination");
  return parts.join(", ");
}

async function handleSquareClick(coordinate) {
  if (!state.game || isTerminal() || state.waitingForMove) return;

  if (!isHumanTurn()) {
    showToast("The current side is controlled by the backend, not by a human player.");
    return;
  }

  const clickedPiece = getPieceAt(coordinate);
  const clickedLegalMove = state.legalMoves.find(move => move.to === coordinate);

  if (state.selectedSquare && clickedLegalMove) {
    await submitSelectedMove(clickedLegalMove);
    return;
  }

  if (clickedPiece && clickedPiece.color === state.game.turn) {
    await selectPiece(coordinate);
    return;
  }

  clearSelection();
}

async function selectPiece(square) {
  state.selectedSquare = square;
  state.legalMoves = [];
  renderBoard();

  try {
    state.waitingForMove = true;
    const response = await ApiClient.getLegalMoves(state.game.gameId, square);
    state.legalMoves = response.moves || [];
    renderBoard();

    if (state.legalMoves.length === 0) {
      showToast("That piece has no legal moves in the current position.");
    }
  } catch (error) {
    showApiError(error);
    clearSelection();
  } finally {
    state.waitingForMove = false;
  }
}

async function submitSelectedMove(legalMove) {
  const from = state.selectedSquare;
  let promotion = null;

  if (legalMove.promotionRequired) {
    promotion = await requestPromotion(legalMove.promotionOptions || ["QUEEN", "ROOK", "BISHOP", "KNIGHT"]);
    if (!promotion) return;
  }

  try {
    state.waitingForMove = true;
    const updatedGame = await ApiClient.makeMove(state.game.gameId, {
      from,
      to: legalMove.to,
      promotion
    });

    clearSelection(false);
    setGame(updatedGame);

    if (!isTerminal() && state.game.currentController !== "HUMAN" && state.game.mode !== "AI_VS_AI") {
      await advanceBackendTurn();
    }
  } catch (error) {
    showApiError(error);
  } finally {
    state.waitingForMove = false;
  }
}

function clearSelection(render = true) {
  state.selectedSquare = null;
  state.legalMoves = [];
  if (render) renderBoard();
}

function requestPromotion(options) {
  return new Promise(resolve => {
    state.promotionResolver = resolve;
    ui.promotionChoices.innerHTML = "";

    options.forEach(type => {
      const button = document.createElement("button");
      button.type = "button";
      button.className = "promotion-button";
      button.textContent = PIECE_GLYPHS[type] || prettifyEnum(type);
      button.title = prettifyEnum(type);
      button.addEventListener("click", () => {
        ui.promotionOverlay.classList.add("hidden");
        state.promotionResolver = null;
        resolve(type);
      });
      ui.promotionChoices.append(button);
    });

    ui.promotionOverlay.classList.remove("hidden");
  });
}

function setGame(game) {
  state.game = game;
  state.selectedSquare = null;
  state.legalMoves = [];
  renderAll();

  if (isTerminal(game)) {
    openResultDialog(game);
  }
}

function renderAll() {
  renderBoard();
  renderGameStatus();
  renderPlayers();
  renderCaptures();
  renderHistory();
}

function renderGameStatus() {
  const game = state.game;

  if (!game) {
    ui.positionTitle.textContent = "No active game";
    ui.positionMessage.textContent = "Start a game to receive the position from the Java backend.";
    setColorBadge(null);
    ui.gameIdBadge.textContent = "No game";
    ui.advanceTurnButton.classList.add("hidden");
    return;
  }

  ui.gameIdBadge.textContent = game.gameId;
  ui.gameIdBadge.title = game.gameId;
  ui.positionTitle.textContent = statusTitle(game);
  ui.positionMessage.textContent = statusMessage(game);
  setColorBadge(game.turn);

  const showAdvance = !isTerminal(game) && game.currentController !== "HUMAN";
  ui.advanceTurnButton.classList.toggle("hidden", !showAdvance);
  ui.advanceTurnButton.textContent = game.mode === "AI_VS_AI" ? "Advance AI turn" : "Play opponent turn";
}

function setColorBadge(color) {
  ui.turnColorBadge.className = "color-badge";
  if (!color) {
    ui.turnColorBadge.classList.add("neutral");
    ui.turnColorBadge.textContent = "--";
    return;
  }
  ui.turnColorBadge.classList.add(color.toLowerCase());
  ui.turnColorBadge.textContent = prettifyEnum(color);
}

function statusTitle(game) {
  switch (game.status) {
    case "CHECK": return `${prettifyEnum(game.turn)} is in check`;
    case "CHECKMATE": return "Checkmate";
    case "STALEMATE": return "Stalemate";
    case "DRAW": return "Draw";
    case "RESIGNED": return "Game resigned";
    case "ABORTED": return "Game aborted";
    default: return `${prettifyEnum(game.turn)} to move`;
  }
}

function statusMessage(game) {
  if (game.status === "CHECKMATE") {
    return `${prettifyEnum(game.winnerColor)} wins by checkmate.`;
  }
  if (game.status === "STALEMATE") {
    return "No legal move is available and the king is not in check.";
  }
  if (game.status === "DRAW") {
    return `Draw${game.drawReason ? ` by ${prettifyEnum(game.drawReason).toLowerCase()}` : ""}.`;
  }
  if (game.status === "RESIGNED") {
    return game.winnerColor ? `${prettifyEnum(game.winnerColor)} wins after resignation.` : "The game ended by resignation.";
  }
  if (game.status === "ABORTED") {
    return "The game was aborted.";
  }

  const controller = prettifyEnum(game.currentController || "HUMAN");
  const checkNote = game.status === "CHECK" ? " The king must escape check." : "";
  return `${prettifyEnum(game.turn)} is controlled by ${controller}.${checkNote}`;
}

function renderPlayers() {
  const game = state.game;

  if (!game) {
    ui.localName.textContent = "You";
    ui.localMeta.textContent = "Color assigned by backend";
    ui.opponentName.textContent = "Opponent";
    ui.opponentMeta.textContent = "Waiting for game";
    setTurnPill(ui.localTurn, false, "Waiting");
    setTurnPill(ui.opponentTurn, false, "Waiting");
    return;
  }

  const humanColor = game.humanColor || null;
  const localColor = humanColor || game.boardOrientation || "WHITE";
  const opponentColor = localColor === "WHITE" ? "BLACK" : "WHITE";
  const controllers = game.controllers || {};

  ui.localName.textContent = humanColor ? "You" : `${prettifyEnum(localColor)} side`;
  ui.localMeta.textContent = `${prettifyEnum(localColor)} · ${prettifyEnum(controllers[localColor] || (humanColor ? "HUMAN" : "AI"))}`;
  ui.opponentName.textContent = humanColor ? "Opponent" : `${prettifyEnum(opponentColor)} side`;
  ui.opponentMeta.textContent = `${prettifyEnum(opponentColor)} · ${prettifyEnum(controllers[opponentColor] || "AI")}`;

  setTurnPill(ui.localTurn, game.turn === localColor, game.turn === localColor ? "To move" : "Waiting");
  setTurnPill(ui.opponentTurn, game.turn === opponentColor, game.turn === opponentColor ? "To move" : "Waiting");
}

function setTurnPill(element, active, text) {
  element.classList.toggle("active", active);
  element.textContent = text;
}

function renderCaptures() {
  if (!state.game) {
    ui.capturedByWhite.textContent = "--";
    ui.capturedByBlack.textContent = "--";
    return;
  }

  renderCapturedList(ui.capturedByWhite, state.game.capturedByWhite || []);
  renderCapturedList(ui.capturedByBlack, state.game.capturedByBlack || []);
}

function renderCapturedList(container, pieces) {
  container.innerHTML = "";
  if (!pieces.length) {
    container.textContent = "None";
    return;
  }

  pieces.forEach(piece => {
    const span = document.createElement("span");
    const color = typeof piece === "string" ? null : piece.color;
    const type = typeof piece === "string" ? piece : piece.type;
    span.className = `captured-piece ${color ? color.toLowerCase() : ""}`;
    span.textContent = PIECE_GLYPHS[type] || "?";
    span.title = `${color ? `${prettifyEnum(color)} ` : ""}${prettifyEnum(type)}`;
    container.append(span);
  });
}

function renderHistory() {
  const moves = state.game?.moveHistory || [];
  ui.moveCount.textContent = `${moves.length} ${moves.length === 1 ? "move" : "moves"}`;

  if (!moves.length) {
    ui.moveHistory.innerHTML = '<div class="empty-state">No moves yet.</div>';
    return;
  }

  ui.moveHistory.innerHTML = "";
  const rows = new Map();

  moves.forEach((move, index) => {
    const ply = move.ply || index + 1;
    const fullMoveNumber = Math.ceil(ply / 2);
    const color = move.pieceColor || (ply % 2 === 1 ? "WHITE" : "BLACK");

    if (!rows.has(fullMoveNumber)) rows.set(fullMoveNumber, { white: null, black: null });
    rows.get(fullMoveNumber)[color === "WHITE" ? "white" : "black"] = move;
  });

  const lastPly = moves.at(-1)?.ply || moves.length;

  rows.forEach((row, moveNumber) => {
    const element = document.createElement("div");
    element.className = "move-row";

    const number = document.createElement("span");
    number.className = "move-number";
    number.textContent = `${moveNumber}.`;

    const white = document.createElement("span");
    white.className = "move-cell";
    white.textContent = moveNotation(row.white);
    if (row.white?.ply === lastPly) white.classList.add("current");

    const black = document.createElement("span");
    black.className = "move-cell";
    black.textContent = moveNotation(row.black);
    if (row.black?.ply === lastPly) black.classList.add("current");

    element.append(number, white, black);
    ui.moveHistory.append(element);
  });

  ui.moveHistory.scrollTop = ui.moveHistory.scrollHeight;
}

function moveNotation(move) {
  if (!move) return "";
  if (move.san) return move.san;
  return `${move.from || "?"}-${move.to || "?"}${move.promotion ? `=${move.promotion.charAt(0)}` : ""}`;
}

async function startGame() {
  const gameMode = ui.gameModeSelect.value;
  const difficulty = modeNeedsDifficulty(gameMode) ? ui.difficultySelect.value : null;

  ui.startGameButton.disabled = true;
  ui.startGameButton.textContent = "Starting...";

  try {
    const game = await ApiClient.createGame({ gameMode, difficulty });
    markConnection(true, "Backend connected");
    ui.resultOverlay.classList.add("hidden");
    setGame(game);

    if (!isTerminal() && state.game.currentController !== "HUMAN" && state.game.mode !== "AI_VS_AI") {
      await advanceBackendTurn();
    }
  } catch (error) {
    markConnection(false, "Backend unavailable");
    showApiError(error);
  } finally {
    ui.startGameButton.disabled = false;
    ui.startGameButton.textContent = "Start game";
  }
}

async function advanceBackendTurn() {
  if (!state.game || isTerminal()) return;

  ui.advanceTurnButton.disabled = true;
  const originalText = ui.advanceTurnButton.textContent;
  ui.advanceTurnButton.textContent = "Thinking...";

  try {
    const game = await ApiClient.advanceTurn(state.game.gameId);
    setGame(game);
  } catch (error) {
    showApiError(error);
  } finally {
    ui.advanceTurnButton.disabled = false;
    ui.advanceTurnButton.textContent = originalText;
  }
}

function openResultDialog(game) {
  let title = "Game finished";
  let message = statusMessage(game);
  let eyebrow = prettifyEnum(game.status);

  if (game.status === "CHECKMATE") {
    title = game.winnerColor ? `${prettifyEnum(game.winnerColor)} wins` : "Checkmate";
  } else if (game.status === "DRAW" || game.status === "STALEMATE") {
    title = "Draw";
  }

  ui.resultEyebrow.textContent = eyebrow;
  ui.resultTitle.textContent = title;
  ui.resultMessage.textContent = message;
  ui.resultOverlay.classList.remove("hidden");
}

function markConnection(online, text) {
  ui.connectionDot.classList.toggle("online", online);
  ui.connectionDot.classList.toggle("offline", !online);
  ui.connectionText.textContent = text;
}

let toastTimer = null;
function showToast(message) {
  clearTimeout(toastTimer);
  ui.toast.textContent = message;
  ui.toast.classList.remove("hidden");
  toastTimer = setTimeout(() => ui.toast.classList.add("hidden"), 3400);
}

function showApiError(error) {
  console.error(error);
  showToast(error?.message || "The request to the backend failed.");
}

async function loadOptions() {
  try {
    const options = await ApiClient.getOptions();
    state.options = {
      gameModes: options.gameModes?.length ? options.gameModes : FALLBACK_OPTIONS.gameModes,
      difficulties: options.difficulties?.length ? options.difficulties : FALLBACK_OPTIONS.difficulties
    };
    markConnection(true, "Backend connected");
  } catch (error) {
    state.options = FALLBACK_OPTIONS;
    markConnection(false, "Using local setup options");
  }

  populateSelect(ui.gameModeSelect, state.options.gameModes);
  populateSelect(ui.difficultySelect, state.options.difficulties);
  updateDifficultyVisibility();
}

function configureEvents() {
  ui.gameModeSelect.addEventListener("change", updateDifficultyVisibility);
  ui.startGameButton.addEventListener("click", startGame);
  ui.advanceTurnButton.addEventListener("click", advanceBackendTurn);
  ui.resultNewGameButton.addEventListener("click", () => {
    ui.resultOverlay.classList.add("hidden");
    ui.gameModeSelect.focus();
  });
}

async function init() {
  renderAll();
  configureEvents();
  await loadOptions();
}

init();
