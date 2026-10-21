import { square } from "./squares.js";
import { Selection, boardInteractive, normalizeLegalMoves } from "./selection.js";

export { square, normalizeLegalMoves };

export const types = ["KING", "QUEEN", "ROOK", "BISHOP", "KNIGHT", "PAWN"];

export const pieceValues = { PAWN: 1, KNIGHT: 3, BISHOP: 3, ROOK: 5, QUEEN: 9, KING: 0 };

export const labels = {
  KING: "rey",
  QUEEN: "dama",
  ROOK: "torre",
  BISHOP: "alfil",
  KNIGHT: "caballo",
  PAWN: "peón",
};

export const initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

export function unwrap(payload) {
  if (!payload || typeof payload !== "object") return payload;
  if (payload.state != null) return unwrap(payload.state);
  if (payload.gameState != null) return unwrap(payload.gameState);

  if (
      payload.session != null &&
      typeof payload.session === "object" &&
      (payload.session.gameState != null || payload.session.state != null)
  ) {
    return unwrap(payload.session);
  }

  return payload;
}

export function piece(value) {
  const color = String(value?.color ?? "").toUpperCase();
  const type = String(value?.type ?? "").toUpperCase();

  return ["WHITE", "BLACK"].includes(color) && types.includes(type)
      ? { color, type }
      : null;
}

export function parseFen(fen) {
  const rows = String(fen).split(" ")[0].split("/");

  if (rows.length !== 8) {
    throw new Error("Posición FEN no válida.");
  }

  const pieces = new Map();

  const names = {
    k: "KING",
    q: "QUEEN",
    r: "ROOK",
    b: "BISHOP",
    n: "KNIGHT",
    p: "PAWN",
  };

  rows.forEach((row, index) => {
    let column = 0;

    for (const char of row) {
      if (/^[1-8]$/.test(char)) {
        column += Number(char);
      } else {
        if (!names[char.toLowerCase()] || column >= 8) {
          throw new Error("Posición FEN no válida.");
        }

        pieces.set(`${"abcdefgh"[column++]}${8 - index}`, {
          type: names[char.toLowerCase()],
          color: char === char.toUpperCase() ? "WHITE" : "BLACK",
        });
      }
    }

    if (column !== 8) {
      throw new Error("Posición FEN no válida.");
    }
  });

  return pieces;
}

export function normalizePieces(raw) {
  if (typeof raw === "string") {
    return parseFen(raw);
  }

  const result = new Map();

  if (Array.isArray(raw)) {
    for (const entry of raw) {
      const tile = square(entry.square ?? entry.tile ?? entry.position);
      const item = piece(entry.piece ?? entry);

      if (tile && item) {
        result.set(tile, item);
      }
    }
  } else if (raw && typeof raw === "object") {
    for (const [key, value] of Object.entries(raw.pieces ?? raw.tiles ?? raw)) {
      const tile = square(key);
      const item = piece(value?.piece ?? value);

      if (tile && item) {
        result.set(tile, item);
      }
    }
  }

  return result;
}

export function normalizeState(payload) {
  const raw = unwrap(payload);

  if (
      !raw ||
      typeof raw !== "object" ||
      (
          raw.pieces == null &&
          raw.board == null &&
          raw.fen == null &&
          raw.gameStateFen == null
      )
  ) {
    throw new Error("El servidor no ha proporcionado el tablero.");
  }

  let events = raw.movesHistory ?? null;

  if (typeof events === "string") {
    try {
      events = JSON.parse(events);
    } catch {
      throw new Error("El historial de movimientos no es válido.");
    }
  }

  if (raw.movesHistory != null && !Array.isArray(events)) {
    throw new Error("El historial de movimientos no es válido.");
  }

  const history = events
      ? [...events].reverse()
      : (raw.moves ?? raw.moveHistory ?? []);

  if (!Array.isArray(history)) {
    throw new Error("El historial de movimientos no es válido.");
  }

  const moves = history.map((entry, index) => {
    const event = typeof entry === "string"
        ? { notation: entry }
        : entry ?? {};

    const movedPiece = event.pieceMoved ?? event.piece;

    const type = typeof movedPiece === "string"
        ? movedPiece
        : movedPiece?.type ?? event.pieceType ?? "PAWN";

    const from = square(event.from);
    const to = square(event.to);

    const color = String(
        event.color ??
        movedPiece?.color ??
        (index % 2 ? "BLACK" : "WHITE")
    ).toUpperCase();

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
    captures[color] = Array.isArray(items)
        ? items.map(piece).filter(Boolean)
        : [];
  }

  const fen = raw.fen ?? raw.gameStateFen ?? moves.at(-1)?.fen ?? null;
  const pieces = normalizePieces(fen ?? raw.pieces ?? raw.board);

  const turn = String(
      raw.turnColor ??
      raw.turn ??
      raw.colorTurn ??
      (typeof fen === "string" && fen.split(/\s+/)[1] === "b" ? "BLACK" : "WHITE")
  ).toUpperCase();

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

export function stateSignature(state) {
  return JSON.stringify({
    turn: state.turn,
    status: state.status,
    checkedColor: state.checkedColor,
    waitingForOpponent: state.waitingForOpponent,
    pieces: [...state.pieces].sort(([a], [b]) => a.localeCompare(b)),
    moves: state.moves.map((move) => [
      move.color,
      move.type,
      move.from,
      move.to,
      move.capture,
      move.capturedPiece?.color,
      move.capturedPiece?.type,
      move.fen,
      move.notation,
    ]),
    eatenPieces: state.eatenPieces,
  });
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
    this.selection ??= new Selection();
    this.selection.reset();
    this.flipped = false;
    // Multijugador local: bando que tiene el tablero de frente y si está girando hacia el otro.
    this.localSide = "WHITE";
    this.turning = false;
  }

  apply(payload) {
    return this.applyNormalized(normalizeState(payload));
  }

  applyNormalized(next) {
    this.snapshots = new Map();

    if (next.initialFen) {
      this.snapshots.set(0, parseFen(next.initialFen));
    } else if (!next.moves.length) {
      this.snapshots.set(0, new Map(next.pieces));
    } else {
      this.snapshots.set(0, parseFen(initialFen));
    }

    for (const move of next.moves) {
      try {
        if (move.fen) {
          this.snapshots.set(move.ply, parseFen(move.fen));
        } else if (move.position) {
          this.snapshots.set(
              move.ply,
              normalizePieces(
                  move.position.pieces ??
                  move.position.board ??
                  move.position.fen ??
                  move.position
              )
          );
        }
      } catch {
        this.snapshots.delete(move.ply);
      }
    }

    this.snapshots.set(next.moves.length, new Map(next.pieces));

    if (this.practice) {
      next.waitingForOpponent = false;
    }

    if (this.reviewPly !== null && !this.snapshots.has(this.reviewPly)) {
      this.reviewPly = null;
    }

    this.state = next;
    this.selection.reset();

    return next;
  }

  get selected() {
    return this.selection.selected;
  }

  get targets() {
    return this.selection.targets;
  }

  get pieces() {
    return this.reviewPly === null
        ? (this.state?.pieces ?? parseFen(initialFen))
        : this.snapshots.get(this.reviewPly);
  }

  get orientation() {
    const black = (this.local ? this.localSide : this.session?.playerColor) === "BLACK";
    return black !== this.flipped ? "BLACK" : "WHITE";
  }

  get practice() {
    return this.session?.gamemode === "PRACTICE";
  }

  get local() {
    return this.session?.gamemode === "LOCAL";
  }

  // Color que puede seleccionarse ahora mismo; null cuando no hay restricción en el cliente.
  get movableColor() {
    return this.local ? this.state?.turn ?? null : null;
  }

  get interactive() {
    return this.reviewPly === null && !this.turning && boardInteractive(this.session, this.state);
  }
}