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
export const unwrap = (payload) => payload?.state ?? payload;
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
export function normalizeState(payload) {
  const raw = unwrap(payload);
  if (!raw || (!raw.pieces && !raw.board && !raw.fen && !raw.gameStateFen))
    throw new Error("El servidor no ha proporcionado el tablero.");
  const javaHistory = !Array.isArray(raw.moves) && Array.isArray(raw.movesHistory);
  const history = javaHistory
    ? [...raw.movesHistory].reverse()
    : (raw.moves ?? raw.moveHistory ?? []);
  const moves = history.map((entry, index) => {
    const item = typeof entry === "string" ? { notation: entry } : entry;
    const type =
      typeof item.piece === "string"
        ? item.piece
        : (item.piece?.type ?? item.pieceType ?? item.pieceMoved?.type ?? "PAWN");
    const from = square(item.from),
      to = square(item.to);
    const color = item.color ?? item.pieceMoved?.color ?? (index % 2 ? "BLACK" : "WHITE");
    return {
      ply: index + 1,
      color,
      type,
      from,
      to,
      capture: !!(item.capture ?? item.pieceCaptured),
      notation:
        item.notation ??
        `${from ?? ""}${item.capture || item.pieceCaptured ? "×" : "–"}${to ?? ""}`,
      fen: item.gameStateFen ?? item.fen ?? null,
      position: item.position ?? item.state ?? null,
    };
  });
  return {
    ...raw,
    pieces: normalizePieces(raw.fen ?? raw.gameStateFen ?? raw.pieces ?? raw.board),
    moves,
    turn: String(raw.turn ?? raw.turnColor ?? raw.colorTurn ?? "").toUpperCase(),
    status: String(raw.status ?? "ACTIVE").toUpperCase(),
    checkedColor: raw.checkedColor ?? null,
  };
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
    this.targets = new Set();
    this.flipped = false;
  }
  apply(payload) {
    const next = normalizeState(payload);
    if (next.initialFen) this.snapshots.set(0, parseFen(next.initialFen));
    else if (!next.moves.length) this.snapshots.set(0, new Map(next.pieces));
    else if (!this.snapshots.has(0)) this.snapshots.set(0, parseFen(initialFen));
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
    this.state = next;
    this.selected = null;
    this.targets.clear();
    return next;
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
      this.interactive && !!piece && (this.practice || piece.color === this.session.playerColor)
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
