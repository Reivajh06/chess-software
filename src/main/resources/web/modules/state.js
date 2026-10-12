export const types = ["KING", "QUEEN", "ROOK", "BISHOP", "KNIGHT", "PAWN"];
export const labels = {
  KING: "rey",
  QUEEN: "dama",
  ROOK: "torre",
  BISHOP: "alfil",
  KNIGHT: "caballo",
  PAWN: "peón",
};
export const initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
// State reads may expose a GameState directly, wrap it in `state`, or return
// Gson's GameSession shape (`gameState`). They are not different game formats.
export function unwrap(payload) {
  if (!payload || typeof payload !== "object") return payload;
  if (payload.state != null) return unwrap(payload.state);
  if (payload.gameState != null) return unwrap(payload.gameState);
  if (payload.session != null && typeof payload.session === "object" &&
      (payload.session.gameState != null || payload.session.state != null))
    return unwrap(payload.session);
  return payload;
}
export function square(value) {
  if (typeof value === "string") return /^[a-h][1-8]$/i.test(value) ? value.toLowerCase() : null;
  if (!value) return null;
  const file = String(value.file?.name ?? value.file ?? "")
    .replace(/^File\./i, "")
    .toLowerCase();
  const rank = String(value.rank?.number ?? value.rank?.name ?? value.rank ?? "").replace(
    /^R/i,
    "",
  );
  return square(file + rank);
}
export function piece(value) {
  const color = String(value?.color ?? "").toUpperCase();
  const type = String(value?.type ?? "").toUpperCase();
  return ["WHITE", "BLACK"].includes(color) && types.includes(type) ? { color, type } : null;
}
export function parseFen(fen) {
  const rows = String(fen).split(" ")[0].split("/");
  if (rows.length !== 8) throw new Error("Posición FEN no válida.");
  const pieces = new Map();
  const names = { k: "KING", q: "QUEEN", r: "ROOK", b: "BISHOP", n: "KNIGHT", p: "PAWN" };
  rows.forEach((row, index) => {
    let column = 0;
    for (const char of row) {
      if (/^[1-8]$/.test(char)) column += Number(char);
      else {
        if (!names[char.toLowerCase()] || column >= 8) throw new Error("Posición FEN no válida.");
        pieces.set(`${"abcdefgh"[column++]}${8 - index}`, {
          type: names[char.toLowerCase()],
          color: char === char.toUpperCase() ? "WHITE" : "BLACK",
        });
      }
    }
    if (column !== 8) throw new Error("Posición FEN no válida.");
  });
  return pieces;
}
export function normalizePieces(raw) {
  if (typeof raw === "string") return parseFen(raw);
  const result = new Map();
  if (Array.isArray(raw)) {
    for (const entry of raw) {
      const tile = square(entry.square ?? entry.tile ?? entry.position);
      const item = piece(entry.piece ?? entry);
      if (tile && item) result.set(tile, item);
    }
  } else if (raw && typeof raw === "object") {
    for (const [key, value] of Object.entries(raw.pieces ?? raw.tiles ?? raw)) {
      const tile = square(key);
      const item = piece(value?.piece ?? value);
      if (tile && item) result.set(tile, item);
    }
  }
  return result;
}
// GameState.movesHistory is a Deque<MoveEvent>, populated with addFirst().
// It therefore arrives newest-first when serialized. Preserve the event's
// gameStateFen (the position AFTER that move) for history navigation.
// The Java GameState record has `board`, NOT a `fen` field. Its newest-first
// MoveEvent deque *does* store a complete post-move FEN per entry. For the
// position after a move, that snapshot is authoritative when a standalone
// FEN was not included in the response DTO.
export function normalizeState(payload) {
  const raw = unwrap(payload);
  if (!raw || typeof raw !== "object" ||
      (raw.pieces == null && raw.board == null && raw.fen == null && raw.gameStateFen == null))
    throw new Error("El servidor no ha proporcionado el tablero.");

  // GetReadCurrentState embeds its serialized history as a JSON string.
  // Also accept an array when the endpoint returns structured history.
  let events = raw.movesHistory ?? null;
  if (typeof events === "string") {
    try { events = JSON.parse(events); }
    catch { throw new Error("El historial de movimientos no es válido."); }
  }
  if (raw.movesHistory != null && !Array.isArray(events))
    throw new Error("El historial de movimientos no es válido.");
  const history = events ? [...events].reverse() : (raw.moves ?? raw.moveHistory ?? []);
  if (!Array.isArray(history))
    throw new Error("El historial de movimientos no es válido.");

  const moves = history.map((entry, index) => {
    const event = typeof entry === "string" ? { notation: entry } : entry ?? {};
    const movedPiece = event.pieceMoved ?? event.piece;
    const type = typeof movedPiece === "string"
      ? movedPiece : movedPiece?.type ?? event.pieceType ?? "PAWN";
    const from = square(event.from);
    const to = square(event.to);
    const color = String(event.color ?? movedPiece?.color ?? (index % 2 ? "BLACK" : "WHITE")).toUpperCase();
    const capturedPiece = piece(event.pieceCaptured ?? event.capturedPiece);
    const capture = Boolean(event.capture ?? event.pieceCaptured ?? capturedPiece);
    return {
      ply: index + 1,
      color,
      type: String(type).toUpperCase(),
      from,
      to,
      capture,
      capturedPiece,
      notation: event.notation ?? `${from ?? ""}${capture ? "×" : "–"}${to ?? ""}`,
      fen: event.gameStateFen ?? event.fen ?? null,
      position: event.position ?? event.state ?? null,
    };
  });

  const captures = {};
  for (const color of ["WHITE", "BLACK"]) {
    const items = raw.eatenPieces?.[color];
    captures[color] = Array.isArray(items) ? items.map(piece).filter(Boolean) : [];
  }

  // Do not fall back to the *initial* board after a successful move. The
  // final MoveEvent is the best available FEN for raw Java GameState responses.
  const fen = raw.fen ?? raw.gameStateFen ?? moves.at(-1)?.fen ?? null;
  const pieces = normalizePieces(fen ?? raw.pieces ?? raw.board);
  // FEN can also be absent on a newly-created game, in which case parse the
  // actual Board.tiles map supplied by GameState instead of assuming 32 pieces.
  const turn = String(raw.turnColor ?? raw.turn ?? raw.colorTurn ??
    (typeof fen === "string" && fen.split(/\s+/)[1] === "b" ? "BLACK" : "WHITE")).toUpperCase();
  return {
    ...raw,
    fen,
    pieces,
    moves,
    eatenPieces: captures,
    turn,
    status: String(raw.status ?? "ACTIVE").toUpperCase(),
    checkedColor: raw.checkedColor ?? null,
    waitingForOpponent: raw.waitingForOpponent ?? false,
  };
}

// Compare gameplay state, not raw Gson serialization: full GameSession JSON
// can contain large Piece.moveset graphs and unstable map iteration order.
// A repeated poll must not clear a piece selection or redraw the board.
export function stateSignature(state) {
  return JSON.stringify({
    turn: state.turn,
    status: state.status,
    checkedColor: state.checkedColor,
    waitingForOpponent: state.waitingForOpponent,
    pieces: [...state.pieces].sort(([a], [b]) => a.localeCompare(b)),
    moves: state.moves.map((m) => [m.color, m.type, m.from, m.to, m.capture,
      m.capturedPiece?.color, m.capturedPiece?.type, m.fen, m.notation]),
    eatenPieces: state.eatenPieces,
  });
}

// Preserve the backend's rule identifiers rather than deriving IDs from array order,
// tile order, or the selected piece's sprite/moveset on the client.
export function normalizeLegalMoves(payload) {
  if (!payload || !Array.isArray(payload.moves))
    throw new Error("El servidor no ha proporcionado movimientos legales válidos.");

  const byDestination = new Map();
  const seenIds = new Set();
  for (const rule of payload.moves) {
    if (!rule || !Number.isSafeInteger(rule.moveId) || rule.moveId < 0 ||
        !Array.isArray(rule.tiles) || seenIds.has(rule.moveId))
      throw new Error("El servidor ha enviado un identificador de movimiento no válido.");
    seenIds.add(rule.moveId);
    if (rule.label !== undefined && typeof rule.label !== "string")
      throw new Error("El servidor ha enviado una descripción de movimiento no válida.");

    for (const rawTile of rule.tiles) {
      const tile = square(rawTile);
      if (!tile) throw new Error("El servidor ha enviado una casilla legal no válida.");
      if (!byDestination.has(tile)) byDestination.set(tile, []);
      const options = byDestination.get(tile);
      if (!options.some((option) => option.moveId === rule.moveId))
        options.push({ moveId: rule.moveId, label: rule.label ?? null });
    }
  }
  return byDestination;
}

export class GameStore {
  constructor() {
    this.reset();
  }
  reset() {
    this.session = null;
    this.state = null;
    this.snapshots = new Map();
    this.reviewPly = null;
    this.selected = null;
    this.clearLegalMoves();
    this.flipped = false;
  }
  apply(payload) {
    return this.applyNormalized(normalizeState(payload));
  }
  applyNormalized(next) {
    // Rebuild snapshots from the authoritative history. Never retain obsolete
    // plies after a reset, replay or a shortened server-side history.
    this.snapshots = new Map();
    if (next.initialFen) this.snapshots.set(0, parseFen(next.initialFen));
    else if (!next.moves.length) this.snapshots.set(0, new Map(next.pieces));
    else this.snapshots.set(0, parseFen(initialFen));
    for (const move of next.moves) {
      try {
        if (move.fen) this.snapshots.set(move.ply, parseFen(move.fen));
        else if (move.position)
          this.snapshots.set(
            move.ply,
            normalizePieces(
              move.position.pieces ?? move.position.board ?? move.position.fen ?? move.position,
            ),
          );
      } catch {
        this.snapshots.delete(move.ply);
      }
    }
    this.snapshots.set(next.moves.length, new Map(next.pieces));
    // The present backend incorrectly reports waitingForOpponent=true even
    // for practice on creation; practice has no opposing human to join.
    if (this.practice) next.waitingForOpponent = false;
    if (this.reviewPly !== null && !this.snapshots.has(this.reviewPly))
      this.reviewPly = null;
    this.state = next;
    this.selected = null;
    this.clearLegalMoves();
    return next;
  }
  clearLegalMoves() {
    this.moveOptionsByTile = new Map();
    this.targets = new Set();
  }
  setLegalMoves(payload) {
    const byTile = normalizeLegalMoves(payload);
    this.moveOptionsByTile = byTile;
    this.targets = new Set(byTile.keys());
  }
  moveOptionsAt(tile) {
    return this.moveOptionsByTile.get(tile) ?? [];
  }
  get pieces() {
    return this.reviewPly === null
      ? (this.state?.pieces ?? parseFen(initialFen))
      : this.snapshots.get(this.reviewPly);
  }
  get orientation() {
    const black = this.session?.playerColor === "BLACK";
    return black !== this.flipped ? "BLACK" : "WHITE";
  }
  get practice() {
    return this.session?.gamemode === "PRACTICE";
  }

  canSelectPiece(piece) {
    return (
      this.interactive && !!piece && (this.practice
        ? piece.color === this.state.turn
        : piece.color === this.session.playerColor)
    );
  }

  get interactive() {
    if (
      this.session?.gamemode === "AIVAI" ||
      this.reviewPly !== null ||
      this.state?.status !== "ACTIVE"
    )
      return false;
    if (this.practice) return true;
    return (
      !this.state.waitingForOpponent &&
      ["WHITE", "BLACK"].includes(this.session?.playerColor) &&
      this.state.turn === this.session.playerColor
    );
  }
}
