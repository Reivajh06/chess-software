import { normalizeLegalMoves } from "./selection.js";

// Trampa de apertura: un peón avanza aunque no sea su turno. legal-moves no ofrece jugadas fuera de
// turno, pero /moves aplica cualquier índice del moveset (0: una casilla, 1: dos casillas).
export function outOfTurnPawnPush(pieces, color) {
  const [start, one, two] = color === "BLACK" ? ["7", "6", "5"] : ["2", "3", "4"];
  const files = [..."abcdefgh"].filter((file) =>
    pieces.get(`${file}${start}`)?.type === "PAWN" && !pieces.has(`${file}${one}`));
  if (!files.length) return null;
  const file = files[Math.floor(Math.random() * files.length)];
  const double = !pieces.has(`${file}${two}`) && Math.random() < 0.5;
  return { from: `${file}${start}`, to: `${file}${double ? two : one}`, moveId: double ? 1 : 0, promotion: null };
}

// Jugadas legales que el servidor ofrece para las piezas de un color (solo si es su turno).
// Se piden de una en una: el servidor simula cada jugada sobre el tablero real de la partida, y varias
// peticiones a la vez se pisan y lo corrompen (piezas que desaparecen o cambian).
export async function legalOptions(api, sessionId, pieces, color) {
  const tiles = [...pieces].filter(([, piece]) => piece.color === color).map(([tile]) => tile);
  const answers = [];
  for (const from of tiles) {
    answers.push(await api.legal(sessionId, from).then((payload) => ({ from, payload }), () => null));
  }

  const options = [];
  for (const answer of answers) {
    if (!answer) continue;
    let destinations;
    try {
      destinations = normalizeLegalMoves(answer.payload);
    } catch {
      continue;
    }
    for (const [to, moves] of destinations) {
      for (const { moveId } of moves) options.push({ from: answer.from, to, moveId });
    }
  }
  return options.map((move) => {
    const promotes = pieces.get(move.from)?.type === "PAWN" && /[18]$/.test(move.to);
    return { ...move, promotion: promotes ? "QUEEN" : null };
  });
}

// Rival simulado mientras el servidor no integra un motor: elige al azar entre las jugadas legales.
export function pickMove(options) {
  return options[Math.floor(Math.random() * options.length)] ?? null;
}
