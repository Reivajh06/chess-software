import { materialLead } from "./cheats.js";

// En el modo Alfi, el comentario de cada jugada lo hace el rival: traduce el evento que detecta el
// comentarista a la frase de Alfi según quién lo ha provocado. null si Alfi prefiere callarse.
export function rivalKind(kind, { move, after, cpuColor }) {
  const byPlayer = move.color !== cpuColor;
  switch (kind) {
    case "check":
    case "checkAgain":
      return after.checkedColor === cpuColor ? "bossChecked" : "bossCheck";
    case "blunder":
      return "bossBlunder";
    case "queenWon":
      return "bossQueenLost";
    case "queenLost":
      return move.capturedPiece?.color === cpuColor ? "bossQueenLost" : "bossQueenWon";
    case "capture":
    case "minorWon":
      return byPlayer ? "bossLostPiece" : null;
    case "bigLead":
      return materialLead(after.pieces, cpuColor) > 0 ? "bossAhead" : "bossBehind";
    case "comeback":
      return "bossComeback";
    case "promotion":
      return byPlayer ? "bossPromotion" : null;
    case "quickMate":
      // De eso se encarga la escena final.
      return null;
    default:
      // Cualquier otra rareza del jugador merece un comentario; las suyas, no.
      return byPlayer ? "bossTaunt" : null;
  }
}
