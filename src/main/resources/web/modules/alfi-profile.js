import { labels } from "./state.js";

// Comentarios de Alfi basados en el historial del jugador. Los perfiles aún no existen: cuando el
// servidor los ofrezca, basta con guardarlos en session.profile con esta forma.
//
// PlayerProfile = {
//   games: number,              partidas jugadas contra Alfi
//   winsVsAlfi: number,         cuántas le ha ganado
//   favoriteFirstMove?: string, primera jugada más repetida, p. ej. "e2-e4"
//   favoritePiece?: string,     pieza que más mueve: "PAWN" | "KNIGHT" | "BISHOP" | "ROOK" | "QUEEN" | "KING"
// }
//
// Devuelve { kind, vars } para Mascot.talk, o null si no hay nada que decir.
export function profileLine(profile) {
  if (!profile || !(profile.games > 0)) return null;
  const options = [];
  if (profile.winsVsAlfi > 0) options.push({ kind: "bossProfileWins", vars: { victorias: String(profile.winsVsAlfi) } });
  else options.push({ kind: "bossProfileLosses", vars: { partidas: String(profile.games) } });
  if (profile.favoriteFirstMove) options.push({ kind: "bossProfileFirstMove", vars: { jugada: profile.favoriteFirstMove } });
  if (labels[profile.favoritePiece]) options.push({ kind: "bossProfilePiece", vars: { pieza: labels[profile.favoritePiece] } });
  return options[Math.floor(Math.random() * options.length)];
}
