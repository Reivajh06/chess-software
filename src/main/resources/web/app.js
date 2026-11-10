import { byId, bindDialogs } from "./modules/ui.js";
import { ChessApi } from "./modules/api.js";
import { loadPreferences } from "./modules/preferences.js";
import { SessionAccess } from "./modules/session.js";
import { GameAudio } from "./modules/audio.js";
import { BackgroundMusic } from "./modules/music.js";
import { Mascot } from "./modules/mascot.js";
import { AlfiBoss } from "./modules/alfi-boss.js";
import { createGame } from "./modules/game.js";
import { setupSettings } from "./modules/settings.js";
import { setupNavigation } from "./modules/navigation.js";

const preferences = loadPreferences();
const api = new ChessApi();
const sessions = new SessionAccess(api);
const audio = new GameAudio(preferences);
const music = new BackgroundMusic(audio, preferences);
const titleMascot = new Mascot(byId("title-mascot"), audio, preferences);
const gameMascot = new Mascot(byId("game-mascot"), audio, preferences);
const alfi = new AlfiBoss(audio, music, preferences);
music.onBeat = (beat) => alfi.beat(beat);
bindDialogs();

let showScreen;
const game = createGame({ api, preferences, audio, sessions, alfi, gameMascot,
	showScreen: (screen) => showScreen(screen) });
const pauseAudio = setupSettings({ preferences, audio, music, alfi, titleMascot, gameMascot,
	activeMascot: game.activeMascot, renderGame: game.render });
showScreen = setupNavigation({ game, api, sessions, preferences, alfi, titleMascot, gameMascot });

window.addEventListener("pagehide", () => {
	api.cancel();
	game.stop();
	pauseAudio();
});
