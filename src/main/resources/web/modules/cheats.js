import { pieceValues } from "./state.js";

// Trampas de Alfi. Todas usan las rutas existentes: /moves aplica cualquier índice del moveset sin
// mirar el turno ni el camino, así que el índice 0 sirve para "teletransportar" una pieza a una casilla
// vacía, y el turno pasa al rival de quien movió.
export const cheatCosts = { double: 20, dodge: 15, rewind: 25, flip: 40 };
// Probabilidad de intentarlo cuando la ocasión se presenta (y le queda aguante).
export const cheatChances = { double: 0.3, dodge: 0.5, rewind: 0.65, flip: 1 };
// Ventaja de material del jugador a partir de la cual Alfi gira el tablero (una vez por partida).
export const flipLead = 5;
// Las primeras jugadas son limpias, salvo la trampa de la entrada.
export const cleanPlies = 6;

const files = "abcdefgh";

export function teleport(from, to) {
  return { from, to, moveId: 0, promotion: null };
}

export function neighbours(tile) {
  const file = files.indexOf(tile[0]);
  const rank = Number(tile[1]);
  const result = [];
  for (const df of [-1, 0, 1]) {
    for (const dr of [-1, 0, 1]) {
      if (!df && !dr) continue;
      const f = file + df;
      const r = rank + dr;
      if (f >= 0 && f < 8 && r >= 1 && r <= 8) result.push(`${files[f]}${r}`);
    }
  }
  return result;
}

export function freeNeighbours(pieces, tile) {
  return neighbours(tile).filter((square) => !pieces.has(square));
}

// Material de un bando menos el del otro.
export function materialLead(pieces, color) {
  let lead = 0;
  for (const piece of pieces.values()) lead += (pieceValues[piece.type] ?? 0) * (piece.color === color ? 1 : -1);
  return lead;
}

// Segunda jugada de la doble jugada: otra pieza, sin tocar las casillas de la primera ni capturar un rey.
// Sale de la misma lista de jugadas legales, calculada antes de la primera.
export function secondMove(options, first, pieces) {
  const busy = new Set([first.from, first.to]);
  const candidates = options.filter((option) =>
    !busy.has(option.from) && !busy.has(option.to) && pieces.get(option.to)?.type !== "KING");
  return candidates[Math.floor(Math.random() * candidates.length)] ?? null;
}
