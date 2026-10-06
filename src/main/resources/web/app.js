"use strict";

/**
 * Frontend contract summary
 * -------------------------
 * POST /api/sessions
 *   body: { gamemode: "PVP"|"PVAI"|"PVCPU"|"AIVAI", difficulty: "EASY"|"MEDIUM"|"HARD"|"NOT_CPU" }
 *   returns: { sessionId, playerToken?, playerColor?, state? }
 *
 * POST /api/sessions/:sessionId/join
 *   body: {}
 *   returns: { sessionId, playerToken, playerColor, state? }
 *
 * GET /api/sessions/:sessionId
 *   returns a GameState (shape documented in FRONTEND_API_CONTRACT.md)
 *
 * GET /api/sessions/:sessionId/legal-moves?from=e2
 *   returns: { moves: ["e3", "e4"] }
 *   The frontend also accepts { moves: [{ to: "e3" }, { to: "e4" }] }.
 *
 * POST /api/sessions/:sessionId/moves
 *   body: { from: "e2", to: "e4", promotion?: "QUEEN"|"ROOK"|"BISHOP"|"KNIGHT" }
 *   returns the updated GameState (or { state: GameState }).
 *
 * If playerToken is returned by create/join, it is sent as X-Player-Token.
 * Every API path is scoped by sessionId so separate games never share frontend state.
 */

const API_BASE = "/api";
const POLL_INTERVAL_MS = 1000;

const GAME_MODES = Object.freeze({
    PVP: "Player vs Player",
    PVAI: "Player vs AI",
    PVCPU: "Player vs CPU",
    AIVAI: "AI vs AI"
});

const DIFFICULTIES = Object.freeze({
    EASY: "Easy",
    MEDIUM: "Medium",
    HARD: "Hard",
    NOT_CPU: "Not CPU"
});

const PIECE_GLYPHS = Object.freeze({
    WHITE: Object.freeze({
        KING: "\u2654",
        QUEEN: "\u2655",
        ROOK: "\u2656",
        BISHOP: "\u2657",
        KNIGHT: "\u2658",
        PAWN: "\u2659"
    }),
    BLACK: Object.freeze({
        KING: "\u265A",
        QUEEN: "\u265B",
        ROOK: "\u265C",
        BISHOP: "\u265D",
        KNIGHT: "\u265E",
        PAWN: "\u265F"
    })
});

const FILES = ["a", "b", "c", "d", "e", "f", "g", "h"];
const RANKS = ["1", "2", "3", "4", "5", "6", "7", "8"];

const dom = {
    board: document.querySelector("#board"),
    boardStatus: document.querySelector("#board-status"),
    setupModal: document.querySelector("#setup-modal"),
    setupError: document.querySelector("#setup-error"),
    difficultyGroup: document.querySelector("#difficulty-group"),
    pvpActionGroup: document.querySelector("#pvp-action-group"),
    joinIdField: document.querySelector("#join-id-field"),
    joinSessionId: document.querySelector("#join-session-id"),
    startGameButton: document.querySelector("#start-game-button"),
    newGameButton: document.querySelector("#new-game-button"),
    promotionModal: document.querySelector("#promotion-modal"),
    promotionOptions: document.querySelector("#promotion-options"),
    resultModal: document.querySelector("#result-modal"),
    resultTitle: document.querySelector("#result-title"),
    resultMessage: document.querySelector("#result-message"),
    resultIcon: document.querySelector("#result-icon"),
    resultNewGameButton: document.querySelector("#result-new-game-button"),
    resultCloseButton: document.querySelector("#result-close-button"),
    sessionCard: document.querySelector("#session-card"),
    sessionId: document.querySelector("#session-id"),
    sessionHelp: document.querySelector("#session-help"),
    copySessionButton: document.querySelector("#copy-session-button"),
    gameModeLabel: document.querySelector("#game-mode-label"),
    difficultyLabel: document.querySelector("#difficulty-label"),
    connectionIndicator: document.querySelector("#connection-indicator"),
    moveHistory: document.querySelector("#move-history"),
    moveCount: document.querySelector("#move-count"),
    playerName: document.querySelector("#player-name"),
    playerDetail: document.querySelector("#player-detail"),
    opponentName: document.querySelector("#opponent-name"),
    opponentDetail: document.querySelector("#opponent-detail"),
    playerTurn: document.querySelector("#player-turn"),
    opponentTurn: document.querySelector("#opponent-turn")
};

const client = {
    sessionId: null,
    playerToken: null,
    playerColor: null,
    gamemode: null,
    difficulty: null,
    state: null,
    selectedSquare: null,
    legalTargets: new Set(),
    pollTimer: null,
    pollInFlight: false,
    resultShownForStatus: null,
    pendingPromotion: null
};

function selectedRadioValue(name) {
    return document.querySelector(`input[name="${name}"]:checked`)?.value ?? null;
}

function isPvp() {
    return selectedRadioValue("gamemode") === "PVP";
}

function setSetupError(message = "") {
    dom.setupError.textContent = message;
    dom.setupError.hidden = !message;
}

function syncSetupControls() {
    const gamemode = selectedRadioValue("gamemode");
    const pvp = gamemode === "PVP";
    const pvpAction = selectedRadioValue("pvpAction");

    dom.difficultyGroup.hidden = pvp;
    dom.pvpActionGroup.hidden = !pvp;
    dom.joinIdField.hidden = !(pvp && pvpAction === "join");

    if (!pvp) {
        dom.startGameButton.textContent = "Start game";
    } else if (pvpAction === "join") {
        dom.startGameButton.textContent = "Join session";
    } else {
        dom.startGameButton.textContent = "Create session";
    }

    setSetupError();
}

function difficultyFor(gamemode) {
    return gamemode === "PVP" ? "NOT_CPU" : selectedRadioValue("difficulty");
}

async function apiRequest(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");

    if (options.body && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    if (client.playerToken) {
        headers.set("X-Player-Token", client.playerToken);
    }

    let response;
    try {
        response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    } catch (error) {
        setConnectionState("error");
        throw new Error("Cannot reach the chess server.");
    }

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
        ? await response.json().catch(() => null)
        : await response.text().catch(() => "");

    if (!response.ok) {
        const message = payload?.message || payload?.error || (typeof payload === "string" && payload) || `Request failed (${response.status}).`;
        throw new Error(message);
    }

    setConnectionState("online");
    return payload;
}

async function createSession(gamemode, difficulty) {
    return apiRequest("/sessions", {
        method: "POST",
        body: JSON.stringify({ gamemode, difficulty })
    });
}

async function joinSession(sessionId) {
    return apiRequest(`/sessions/${encodeURIComponent(sessionId)}/join`, {
        method: "POST",
        body: JSON.stringify({})
    });
}

async function getGameState() {
    return apiRequest(`/sessions/${encodeURIComponent(client.sessionId)}`);
}

async function getLegalMoves(from) {
    return apiRequest(`/sessions/${encodeURIComponent(client.sessionId)}/legal-moves?from=${encodeURIComponent(from)}`);
}

async function submitMove(from, to, promotion = null) {
    return apiRequest(`/sessions/${encodeURIComponent(client.sessionId)}/moves`, {
        method: "POST",
        body: JSON.stringify({ from, to, ...(promotion ? { promotion } : {}) })
    });
}

function unwrapState(payload) {
    return payload?.state ?? payload;
}

function normalizeSquare(value) {
    if (!value) return null;
    if (typeof value === "string") return value.toLowerCase();
    if (value.name) return String(value.name).toLowerCase();
    if (value.file != null && value.rank != null) {
        const file = typeof value.file === "string" ? value.file.replace(/^File\./i, "") : value.file.name;
        const rankRaw = typeof value.rank === "string" ? value.rank : value.rank.number ?? value.rank.name;
        const rank = String(rankRaw).replace(/^R/i, "");
        return `${String(file).toLowerCase()}${rank}`;
    }
    return null;
}

function normalizePiece(piece) {
    if (!piece) return null;
    const color = String(piece.color ?? "").toUpperCase();
    const type = String(piece.type ?? "").toUpperCase();
    if (!PIECE_GLYPHS[color]?.[type]) return null;
    return { color, type };
}

function normalizePieces(state) {
    const pieces = new Map();
    const rawPieces = state?.pieces ?? state?.board?.pieces ?? state?.board ?? [];

    if (Array.isArray(rawPieces)) {
        for (const entry of rawPieces) {
            const square = normalizeSquare(entry.square ?? entry.tile ?? entry.position);
            const piece = normalizePiece(entry.piece ?? entry);
            if (square && piece) pieces.set(square, piece);
        }
        return pieces;
    }

    if (rawPieces && typeof rawPieces === "object") {
        for (const [key, value] of Object.entries(rawPieces)) {
            const square = normalizeSquare(key);
            const piece = normalizePiece(value?.piece ?? value);
            if (square && piece) pieces.set(square, piece);
        }
    }

    return pieces;
}

function normalizeHistory(state) {
    const raw = state?.moves ?? state?.moveHistory ?? state?.movesHistory ?? [];
    if (!Array.isArray(raw)) return [];

    return raw.map((move, index) => {
        if (typeof move === "string") {
            return { ply: index + 1, notation: move, color: index % 2 === 0 ? "WHITE" : "BLACK" };
        }

        const color = String(move.color ?? move.pieceMoved?.color ?? (index % 2 === 0 ? "WHITE" : "BLACK")).toUpperCase();
        const from = normalizeSquare(move.from);
        const to = normalizeSquare(move.to);
        const pieceType = String(move.piece ?? move.pieceType ?? move.pieceMoved?.type ?? "PAWN").toUpperCase();
        const capture = Boolean(move.capture ?? move.captured ?? move.pieceCaptured);
        const promotion = move.promotion ? String(move.promotion).toUpperCase() : null;
        const notation = move.notation || makeSimpleNotation(pieceType, from, to, capture, promotion);
        return { ply: move.ply ?? index + 1, notation, color, from, to };
    });
}

function makeSimpleNotation(pieceType, from, to, capture, promotion) {
    if (!to) return "\u2014";
    const letters = { KING: "K", QUEEN: "Q", ROOK: "R", BISHOP: "B", KNIGHT: "N", PAWN: "" };
    const piece = letters[pieceType] ?? "";
    const pawnCaptureFile = pieceType === "PAWN" && capture && from ? from[0] : "";
    const promo = promotion ? `=${letters[promotion] || promotion[0]}` : "";
    return `${piece}${pawnCaptureFile}${capture ? "x" : ""}${to}${promo}`;
}

function normalizeLegalTargets(payload) {
    const rawMoves = payload?.moves ?? payload?.legalMoves ?? payload ?? [];
    if (!Array.isArray(rawMoves)) return [];
    return rawMoves
        .map((move) => normalizeSquare(typeof move === "string" ? move : move.to ?? move.tile ?? move.square))
        .filter(Boolean);
}

function gameOrientation() {
    return client.playerColor === "BLACK" ? "BLACK" : "WHITE";
}

function orderedSquares() {
    const orientation = gameOrientation();
    const ranks = orientation === "WHITE" ? [...RANKS].reverse() : [...RANKS];
    const files = orientation === "WHITE" ? [...FILES] : [...FILES].reverse();
    const squares = [];
    for (const rank of ranks) {
        for (const file of files) squares.push(`${file}${rank}`);
    }
    return squares;
}

function initialBoardPieces() {
    const pieces = new Map();
    const backRank = ["ROOK", "KNIGHT", "BISHOP", "QUEEN", "KING", "BISHOP", "KNIGHT", "ROOK"];
    FILES.forEach((file, i) => {
        pieces.set(`${file}1`, { color: "WHITE", type: backRank[i] });
        pieces.set(`${file}2`, { color: "WHITE", type: "PAWN" });
        pieces.set(`${file}7`, { color: "BLACK", type: "PAWN" });
        pieces.set(`${file}8`, { color: "BLACK", type: backRank[i] });
    });
    return pieces;
}

function isOwnInteractivePiece(piece) {
    if (!piece || !client.sessionId || !client.state) return false;
    if (client.state.status && client.state.status !== "ACTIVE") return false;
    if (client.gamemode === "AIVAI") return false;
    if (!client.playerColor) return false;
    return piece.color === client.playerColor && String(client.state.turn).toUpperCase() === client.playerColor;
}

function findCheckedKingSquare(pieces) {
    const checkedColor = String(client.state?.checkedColor ?? "").toUpperCase();
    if (!checkedColor) return null;
    for (const [square, piece] of pieces.entries()) {
        if (piece.color === checkedColor && piece.type === "KING") return square;
    }
    return null;
}

function renderBoard() {
    const hasServerState = client.state && (client.state.pieces || client.state.board);
    const pieces = hasServerState ? normalizePieces(client.state) : initialBoardPieces();
    const history = client.state ? normalizeHistory(client.state) : [];
    const lastMove = history.at(-1);
    const checkedKingSquare = findCheckedKingSquare(pieces);
    const orientation = gameOrientation();

    dom.board.innerHTML = "";

    orderedSquares().forEach((square, index) => {
        const file = square[0];
        const rank = square[1];
        const fileIndex = FILES.indexOf(file);
        const rankIndex = Number(rank) - 1;
        const isLight = (fileIndex + rankIndex) % 2 === 1;
        const piece = pieces.get(square);
        const squareButton = document.createElement("button");

        squareButton.type = "button";
        squareButton.className = `chess-square ${isLight ? "light" : "dark"}`;
        squareButton.dataset.square = square;
        squareButton.setAttribute("role", "gridcell");
        squareButton.setAttribute("aria-label", piece ? `${square}: ${piece.color.toLowerCase()} ${piece.type.toLowerCase()}` : square);

        if (client.selectedSquare === square) squareButton.classList.add("selected");
        if (lastMove && (lastMove.from === square || lastMove.to === square)) squareButton.classList.add("last-move");
        if (checkedKingSquare === square) squareButton.classList.add("in-check");
        if (client.legalTargets.has(square)) squareButton.classList.add(piece ? "legal-capture" : "legal-move");
        if (isOwnInteractivePiece(piece) || client.legalTargets.has(square)) squareButton.classList.add("interactive");

        if (piece) {
            const pieceSpan = document.createElement("span");
            pieceSpan.className = `piece ${piece.color === "WHITE" ? "white-piece" : "black-piece"}`;
            pieceSpan.textContent = PIECE_GLYPHS[piece.color][piece.type];
            pieceSpan.setAttribute("aria-hidden", "true");
            squareButton.append(pieceSpan);
        }

        const row = Math.floor(index / 8);
        const col = index % 8;
        if (col === 0) {
            const rankLabel = document.createElement("span");
            rankLabel.className = "coordinate rank";
            rankLabel.textContent = rank;
            squareButton.append(rankLabel);
        }
        if (row === 7) {
            const fileLabel = document.createElement("span");
            fileLabel.className = "coordinate file";
            fileLabel.textContent = file;
            squareButton.append(fileLabel);
        }

        squareButton.addEventListener("click", () => onSquareClicked(square, piece));
        dom.board.append(squareButton);
    });

    dom.board.setAttribute("aria-label", `Chess board, ${orientation.toLowerCase()} at bottom`);
}

async function onSquareClicked(square, piece) {
    if (!client.sessionId || !client.state || client.state.status !== "ACTIVE") return;

    if (client.legalTargets.has(square) && client.selectedSquare) {
        const from = client.selectedSquare;
        const promotionNeeded = needsPromotion(from, square);
        client.selectedSquare = null;
        client.legalTargets.clear();
        renderBoard();

        if (promotionNeeded) {
            client.pendingPromotion = { from, to: square };
            renderPromotionOptions();
            dom.promotionModal.hidden = false;
        } else {
            await makeMove(from, square);
        }
        return;
    }

    if (!isOwnInteractivePiece(piece)) {
        clearSelection();
        return;
    }

    client.selectedSquare = square;
    client.legalTargets.clear();
    renderBoard();

    try {
        const payload = await getLegalMoves(square);
        if (client.selectedSquare !== square) return;
        client.legalTargets = new Set(normalizeLegalTargets(payload));
        renderBoard();
    } catch (error) {
        clearSelection();
        setBoardMessage(error.message);
    }
}

function clearSelection() {
    client.selectedSquare = null;
    client.legalTargets.clear();
    renderBoard();
}

function needsPromotion(from, to) {
    const piece = normalizePieces(client.state).get(from);
    return piece?.type === "PAWN" && (to.endsWith("8") || to.endsWith("1"));
}

function renderPromotionOptions() {
    const color = client.playerColor === "BLACK" ? "BLACK" : "WHITE";
    dom.promotionOptions.querySelectorAll("[data-promotion]").forEach((button) => {
        const type = button.dataset.promotion;
        const glyph = button.querySelector(".promotion-piece");
        if (glyph) glyph.textContent = PIECE_GLYPHS[color][type];
    });
}

async function makeMove(from, to, promotion = null) {
    setBoardMessage("Sending move\u2026");
    try {
        const response = await submitMove(from, to, promotion);
        const nextState = unwrapState(response);
        if (nextState?.status || nextState?.pieces || nextState?.board) {
            applyGameState(nextState);
        } else {
            await refreshState();
        }
    } catch (error) {
        setBoardMessage(error.message);
        await refreshState().catch(() => {});
    }
}

function applyGameState(rawState) {
    if (!rawState) return;

    const state = {
        ...rawState,
        status: String(rawState.status ?? "ACTIVE").toUpperCase(),
        turn: String(rawState.turn ?? rawState.colorTurn ?? "WHITE").toUpperCase(),
        checkedColor: rawState.checkedColor ? String(rawState.checkedColor).toUpperCase() : null
    };

    client.state = state;
    client.gamemode = String(state.gamemode ?? client.gamemode ?? "").toUpperCase() || null;
    client.difficulty = String(state.difficulty ?? client.difficulty ?? "").toUpperCase() || null;

    if (state.playerColor) client.playerColor = String(state.playerColor).toUpperCase();

    client.selectedSquare = null;
    client.legalTargets.clear();

    renderBoard();
    renderSidebar();
    renderPlayers();
    renderMoveHistory();
    renderStatus();
}

function renderSidebar() {
    dom.gameModeLabel.textContent = GAME_MODES[client.gamemode] ?? client.gamemode ?? "\u2014";
    dom.difficultyLabel.textContent = DIFFICULTIES[client.difficulty] ?? client.difficulty ?? "\u2014";

    const showSession = client.gamemode === "PVP" && client.sessionId;
    dom.sessionCard.hidden = !showSession;
    if (showSession) {
        dom.sessionId.textContent = client.sessionId;
        dom.sessionHelp.textContent = client.state?.waitingForOpponent
            ? "Waiting for another player. Share this ID so they can join."
            : "Share this ID so another player can join this game.";
    }
}

function renderPlayers() {
    if (!client.sessionId) return;

    const playerColor = client.playerColor;
    const opponentColor = playerColor === "BLACK" ? "WHITE" : "BLACK";
    const turn = client.state?.turn;

    if (client.gamemode === "AIVAI") {
        dom.playerName.textContent = "White AI";
        dom.playerDetail.textContent = "Automated player";
        dom.opponentName.textContent = "Black AI";
        dom.opponentDetail.textContent = "Automated player";
        dom.playerTurn.hidden = turn !== "WHITE";
        dom.opponentTurn.hidden = turn !== "BLACK";
        return;
    }

    dom.playerName.textContent = "You";
    dom.playerDetail.textContent = playerColor ? `${capitalize(playerColor)} pieces` : "Player";

    if (client.gamemode === "PVP") {
        dom.opponentName.textContent = "Opponent";
        dom.opponentDetail.textContent = client.state?.waitingForOpponent ? "Waiting to join" : `${capitalize(opponentColor)} pieces`;
    } else {
        dom.opponentName.textContent = client.gamemode === "PVCPU" ? "CPU" : "AI";
        dom.opponentDetail.textContent = client.difficulty ? `${DIFFICULTIES[client.difficulty] ?? client.difficulty} difficulty` : "Computer opponent";
    }

    dom.playerTurn.hidden = turn !== playerColor || client.state?.status !== "ACTIVE";
    dom.opponentTurn.hidden = turn !== opponentColor || client.state?.status !== "ACTIVE";
}

function renderMoveHistory() {
    const history = normalizeHistory(client.state);
    dom.moveCount.textContent = `${history.length} ${history.length === 1 ? "move" : "moves"}`;

    if (!history.length) {
        dom.moveHistory.innerHTML = '<div class="empty-state">Moves will appear here.</div>';
        return;
    }

    const rows = [];
    for (let i = 0; i < history.length; i += 2) {
        rows.push({
            number: Math.floor(i / 2) + 1,
            white: history[i] ?? null,
            black: history[i + 1] ?? null
        });
    }

    dom.moveHistory.innerHTML = rows.map((row) => {
        const whiteLatest = row.white?.ply === history.at(-1)?.ply ? " latest" : "";
        const blackLatest = row.black?.ply === history.at(-1)?.ply ? " latest" : "";
        return `
            <div class="move-row">
                <span class="move-number">${row.number}.</span>
                <span class="move-cell${whiteLatest}" title="${escapeHtml(row.white?.notation ?? "")}">${escapeHtml(row.white?.notation ?? "")}</span>
                <span class="move-cell${blackLatest}" title="${escapeHtml(row.black?.notation ?? "")}">${escapeHtml(row.black?.notation ?? "")}</span>
            </div>`;
    }).join("");

    dom.moveHistory.scrollTop = dom.moveHistory.scrollHeight;
}

function renderStatus() {
    const status = client.state?.status ?? "ACTIVE";

    if (client.state?.waitingForOpponent) {
        setBoardMessage("Waiting for opponent\u2026");
        return;
    }

    if (status === "ACTIVE") {
        const checked = client.state?.checkedColor;
        if (checked) {
            setBoardMessage(`${capitalize(checked)} king is in check`);
        } else if (client.gamemode !== "AIVAI" && client.playerColor && client.state?.turn !== client.playerColor) {
            setBoardMessage("Opponent's turn");
        } else {
            setBoardMessage("");
        }
        client.resultShownForStatus = null;
        return;
    }

    setBoardMessage(gameStatusMessage(status));
    if (client.resultShownForStatus !== status) {
        client.resultShownForStatus = status;
        showResult(status);
    }
}

function gameStatusMessage(status) {
    switch (status) {
        case "WHITE_WIN": return "Checkmate \u2014 White wins";
        case "BLACK_WIN": return "Checkmate \u2014 Black wins";
        case "DRAW": return "Draw";
        default: return "Game over";
    }
}

function showResult(status) {
    dom.resultTitle.textContent = status === "DRAW" ? "Draw" : "Checkmate";
    dom.resultMessage.textContent = gameStatusMessage(status);
    dom.resultIcon.textContent = status === "DRAW" ? "\u00BD" : status === "WHITE_WIN" ? "\u2654" : "\u265A";
    dom.resultModal.hidden = false;
}

function setBoardMessage(message) {
    dom.boardStatus.textContent = message || "";
}

function setConnectionState(state) {
    dom.connectionIndicator.classList.remove("online", "error");
    if (state === "online") {
        dom.connectionIndicator.textContent = "Connected";
        dom.connectionIndicator.classList.add("online");
    } else if (state === "error") {
        dom.connectionIndicator.textContent = "Connection issue";
        dom.connectionIndicator.classList.add("error");
    } else {
        dom.connectionIndicator.textContent = "Offline";
    }
}

async function startFromSetup() {
    const gamemode = selectedRadioValue("gamemode");
    const difficulty = difficultyFor(gamemode);
    const pvpAction = selectedRadioValue("pvpAction");

    if (!gamemode || !difficulty) return;

    let sessionIdToJoin = null;
    if (gamemode === "PVP" && pvpAction === "join") {
        sessionIdToJoin = dom.joinSessionId.value.trim();
        if (!sessionIdToJoin) {
            setSetupError("Paste the session ID you want to join.");
            dom.joinSessionId.focus();
            return;
        }
    }

    dom.startGameButton.disabled = true;
    dom.startGameButton.textContent = pvpAction === "join" ? "Joining\u2026" : "Starting\u2026";
    setSetupError();

    try {
        resetRuntimeSession();
        client.gamemode = gamemode;
        client.difficulty = difficulty;

        const response = sessionIdToJoin
            ? await joinSession(sessionIdToJoin)
            : await createSession(gamemode, difficulty);

        const responseSessionId = response?.sessionId ?? response?.id ?? sessionIdToJoin;
        if (!responseSessionId) throw new Error("The server did not return a sessionId.");

        client.sessionId = responseSessionId;
        client.playerToken = response?.playerToken ?? response?.token ?? null;
        client.playerColor = response?.playerColor ? String(response.playerColor).toUpperCase() : defaultPlayerColor(gamemode, pvpAction);

        persistSession();
        updateUrlSession(client.sessionId);

        const state = response?.state ?? (response?.status ? response : await getGameState());
        applyGameState(state);
        dom.setupModal.hidden = true;
        startPolling();
    } catch (error) {
        resetRuntimeSession();
        setSetupError(error.message);
        setConnectionState("error");
    } finally {
        dom.startGameButton.disabled = false;
        syncSetupControls();
    }
}

function defaultPlayerColor(gamemode, pvpAction) {
    if (gamemode === "AIVAI") return null;
    if (gamemode === "PVP" && pvpAction === "join") return "BLACK";
    return "WHITE";
}

async function refreshState() {
    if (!client.sessionId || client.pollInFlight) return;
    client.pollInFlight = true;
    try {
        const state = unwrapState(await getGameState());
        applyGameState(state);
    } finally {
        client.pollInFlight = false;
    }
}

function startPolling() {
    stopPolling();
    client.pollTimer = window.setInterval(() => {
        refreshState().catch(() => setConnectionState("error"));
    }, POLL_INTERVAL_MS);
}

function stopPolling() {
    if (client.pollTimer) window.clearInterval(client.pollTimer);
    client.pollTimer = null;
}

function resetRuntimeSession() {
    stopPolling();
    client.sessionId = null;
    client.playerToken = null;
    client.playerColor = null;
    client.state = null;
    client.selectedSquare = null;
    client.legalTargets.clear();
    client.resultShownForStatus = null;
    client.pendingPromotion = null;
    dom.promotionModal.hidden = true;
    clearPersistedSession();
}

function openNewGame() {
    resetRuntimeSession();
    updateUrlSession(null);
    dom.resultModal.hidden = true;
    dom.setupModal.hidden = false;
    dom.sessionCard.hidden = true;
    dom.gameModeLabel.textContent = "\u2014";
    dom.difficultyLabel.textContent = "\u2014";
    dom.moveHistory.innerHTML = '<div class="empty-state">Moves will appear here.</div>';
    dom.moveCount.textContent = "0 moves";
    dom.playerDetail.textContent = "Choose a game mode to begin";
    dom.opponentDetail.textContent = "Waiting for a game";
    dom.playerTurn.hidden = true;
    dom.opponentTurn.hidden = true;
    setConnectionState("offline");
    setBoardMessage("");
    renderBoard();
    syncSetupControls();
}

function persistSession() {
    const data = {
        sessionId: client.sessionId,
        playerToken: client.playerToken,
        playerColor: client.playerColor,
        gamemode: client.gamemode,
        difficulty: client.difficulty
    };
    sessionStorage.setItem("chess.activeSession", JSON.stringify(data));
}

function clearPersistedSession() {
    sessionStorage.removeItem("chess.activeSession");
}

function restorePersistedSession() {
    const fromStorage = (() => {
        try {
            return JSON.parse(sessionStorage.getItem("chess.activeSession") || "null");
        } catch {
            return null;
        }
    })();

    const urlSessionId = new URL(window.location.href).searchParams.get("session");
    const sessionId = urlSessionId || fromStorage?.sessionId;
    if (!sessionId) return false;

    client.sessionId = sessionId;
    client.playerToken = fromStorage?.sessionId === sessionId ? fromStorage?.playerToken ?? null : null;
    client.playerColor = fromStorage?.sessionId === sessionId ? fromStorage?.playerColor ?? null : null;
    client.gamemode = fromStorage?.sessionId === sessionId ? fromStorage?.gamemode ?? null : null;
    client.difficulty = fromStorage?.sessionId === sessionId ? fromStorage?.difficulty ?? null : null;

    return true;
}

function updateUrlSession(sessionId) {
    const url = new URL(window.location.href);
    if (sessionId) url.searchParams.set("session", sessionId);
    else url.searchParams.delete("session");
    window.history.replaceState({}, "", url);
}

async function attemptRestore() {
    if (!restorePersistedSession()) return;

    dom.setupModal.hidden = true;
    setBoardMessage("Loading game\u2026");
    try {
        const state = unwrapState(await getGameState());
        applyGameState(state);
        persistSession();
        startPolling();
    } catch (error) {
        clearPersistedSession();
        client.sessionId = null;
        dom.setupModal.hidden = false;
        setSetupError(`Could not restore that session: ${error.message}`);
        setConnectionState("error");
    }
}

async function copySessionId() {
    if (!client.sessionId) return;
    try {
        await navigator.clipboard.writeText(client.sessionId);
        const original = dom.copySessionButton.textContent;
        dom.copySessionButton.textContent = "\u2713";
        window.setTimeout(() => { dom.copySessionButton.textContent = original; }, 1200);
    } catch {
        dom.sessionId.focus?.();
    }
}

function capitalize(value) {
    const text = String(value || "").toLowerCase();
    return text ? text[0].toUpperCase() + text.slice(1) : "";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

document.querySelectorAll('input[name="gamemode"], input[name="pvpAction"]').forEach((input) => {
    input.addEventListener("change", syncSetupControls);
});

dom.startGameButton.addEventListener("click", startFromSetup);
dom.newGameButton.addEventListener("click", openNewGame);
dom.resultNewGameButton.addEventListener("click", openNewGame);
dom.resultCloseButton.addEventListener("click", () => { dom.resultModal.hidden = true; });
dom.copySessionButton.addEventListener("click", copySessionId);
dom.promotionOptions.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-promotion]");
    if (!button || !client.pendingPromotion) return;
    const pending = client.pendingPromotion;
    client.pendingPromotion = null;
    dom.promotionModal.hidden = true;
    await makeMove(pending.from, pending.to, button.dataset.promotion);
});
dom.joinSessionId.addEventListener("keydown", (event) => {
    if (event.key === "Enter") startFromSetup();
});

window.addEventListener("beforeunload", stopPolling);

syncSetupControls();
renderBoard();
attemptRestore();
