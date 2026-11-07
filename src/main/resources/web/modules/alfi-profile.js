import { labels } from "./state.js";

export function profileLine(profile) {
	if(!profile || !(profile.games > 0)) return null;
	const options = [];
	if(profile.winsVsAlfi > 0) options.push({ kind: "bossProfileWins", vars: { victorias: String(profile.winsVsAlfi) } });
	else options.push({ kind: "bossProfileLosses", vars: { partidas: String(profile.games) } });
	if(profile.favoriteFirstMove) options.push({ kind: "bossProfileFirstMove", vars: { jugada: profile.favoriteFirstMove } });
	if(labels[profile.favoritePiece]) options.push({ kind: "bossProfilePiece", vars: { pieza: labels[profile.favoritePiece] } });
	return options[Math.floor(Math.random() * options.length)];
}
