import { byId, dialog, confirmDialog, optionButton } from "./ui.js";
import { HttpStatus } from "./api.js";
import { parseInvitation, parseWatchInvitation } from "./preferences.js";
import { cpuLevelsFor, modeDefinition } from "./modes.js";
import { PluginDialog } from "./plugins.js";
import { TitleScreen } from "./interface.js";
import { AccountDialog, AccountScreen, loadAccount, saveAccount, verifySavedAccount } from "./account.js";
import { accountTone } from "./identity.js";
import { ProfileScreen } from "./profile.js";
import { setupInvitations } from "./invitations.js";

export function setupNavigation({ game, api, sessions, preferences, alfi, titleMascot, gameMascot }) {
	const { store } = game;
	let sessionBusy = false;
	let hoveredEntry = null;
	let leaving = false;
	const openInvite = setupInvitations(api, sessions, store);
	let cpuPlugins = null;

	const pluginDialog = new PluginDialog(api, (mode, pluginNames, installed) => {
		if(mode === "PVCPU") {
			cpuPlugins = pluginNames;
			renderCpuLevels(installed);
			dialog("cpu-dialog");
		} else {
			openSession(null, mode, undefined, pluginNames);
		}
	});

	const title = new TitleScreen({
		onStart: (mode) => mode === "PVP"
				? dialog("pvp-dialog")
				: modeDefinition(mode).pluginPrompts ? pluginDialog.open(mode) : openSession(null, mode),
		onJoin: joinSession,
		onError: () => titleMascot.talk("error", 2),
		onHover: (id, hint) => {
			if(id === hoveredEntry) return;
			hoveredEntry = id;
			titleMascot.say(hint);
		},
	});

	function showScreen(screen) {
		document.body.dataset.screen = screen;
		(screen === "title" ? gameMascot : titleMascot).silence();
		alfi.apply(screen === "game" && store.session?.alfi === "revealed");

		if(screen === "title") {
			title.render();
			title.showMenu();
			hoveredEntry = null;
			titleMascot.talk("greeting");
		}
	}

	function joinSession(value) {
		// A link to watch (?watch=) follows the game without taking a seat.
		const watchId = parseWatchInvitation(value);
		if(watchId) return openSession(watchId, "SPECTATE");

		const id = parseInvitation(value);

		if(!id) {
			title.setError("Enter a valid ID or link.");
			return;
		}

		return openSession(id, "PVP");
	}

	async function openSession(id, mode, difficulty, pluginNames) {
		if(sessionBusy) return;

		sessionBusy = true;
		title.setBusy(true, id ? "join" : mode);
		titleMascot.talk("loading", 1);
		title.setError();
		game.reset(id);

		const generation = game.version();

		game.render();

		try {
			const { session, state } = await sessions.open(id, mode, difficulty, pluginNames);

			if(generation === game.version()) {
				await game.activate(session, state);
				if(!id && session.gamemode === "PVP") openInvite();
			}
		} catch (error) {
			if(
					generation === game.version() &&
					error.name !== "AbortError"
			) {
				if(document.body.dataset.screen !== "title") showScreen("title");
				title.setError(error.message);
				setConnection("Unavailable");
			}
		} finally {
			sessionBusy = false;
			title.setBusy(false);
		}
	}

	const account = new AccountScreen(api, {
		onLeave: () => showScreen("title"),
		onLoggedIn: (current) => {
			renderAccount(current);
			showScreen("title");
		},
	});

	function renderAccount(current) {
		byId("account-open").hidden = !!current;
		byId("account-chip").hidden = !current;
		byId("account-logout").hidden = !current;
		document.body.toggleAttribute("data-account", !!current);
		if(!current) return;
		byId("account-chip-mark").textContent = current.username[0].toUpperCase();
		byId("account-chip-name").textContent = current.username;
		byId("account-chip-mark").style.setProperty("--tone", accountTone(current.username));
	}

	renderAccount(loadAccount());
	// After a server restart the saved account may no longer be logged in there: check before showing it.
	verifySavedAccount(api).then(() => renderAccount(loadAccount()));

	byId("account-open").addEventListener("click", () => {
		leaveGame(() => {
			showScreen("account");
			account.open();
		});
	});

	async function endAccountSession() {
		const button = byId("account-logout");
		button.disabled = true;
		try {
			await api.logout();
		} catch {}
		button.disabled = false;
		saveAccount(null);
		renderAccount(null);
		// Logging out changes who the player is, so the game in progress cannot go on.
		if(store.session) { game.reset(); game.render(); }
		if(!sessionBusy) showScreen("title");
	}

	byId("account-logout").addEventListener("click", endAccountSession);

	const accountDialog = new AccountDialog(api, { onDeleted: endAccountSession });

	const profile = new ProfileScreen(api, preferences, {
		audio: titleMascot.audio,
		onDeleteAccount: () => accountDialog.open(loadAccount()),
		onSignIn: async () => {
			await endAccountSession();
			showScreen("account");
			account.open();
		},
	});

	byId("account-chip").addEventListener("click", () => {
		const current = loadAccount();
		if(!current) return;
		leaveGame(() => {
			showScreen("profile");
			profile.open(current);
			byId("profile-screen").focus();
		});
	});

	byId("result-rematch").addEventListener("click", () => {
		const mode = store.session?.gamemode ?? "PRACTICE";
		dialog("result-dialog", false);
		openSession(null, mode, store.session?.alfiBoss ? "ALFI" : store.session?.difficulty, store.session?.pluginNames);
	});


	for(const id of ["menu-button", "leave", "result-new"]) {
		byId(id).addEventListener("click", () => {
			dialog("result-dialog", false);
			if(!sessionBusy) leaveGame(() => showScreen("title"));
		});
	}

	async function leaveGame(then) {
		if(leaving) return;
		const inProgress = !store.session?.watching && store.state?.status === "ACTIVE" && store.state.moves.length > 0;
		if(inProgress) {
			leaving = true;
			const confirmed = await confirmDialog("leave-dialog");
			leaving = false;
			if(!confirmed) return;
		}
		game.reset();
		game.render();
		then();
	}
	function renderCpuLevels(installed) {
		byId("cpu-levels").replaceChildren(...cpuLevelsFor(installed).map((level) =>
			optionButton(level.name, level.detail, "mode-pvcpu", () => {
				dialog("cpu-dialog", false);
				openSession(null, "PVCPU", level.id, cpuPlugins);
			})));
	}

	for(const [id, mode] of [["pvp-invite", "PVP"], ["pvp-local", "LOCAL"]]) {
		byId(id).addEventListener("click", () => {
			dialog("pvp-dialog", false);
			openSession(null, mode);
		});
	}


	game.render();
	showScreen("title");
	const invitation = parseInvitation(
			new URL(location.href).searchParams.get("session") ?? ""
	);

	if(invitation) {
		const saved = sessions.saved(invitation);

		if(saved) {
			game.reset();

			const generation = game.version();

			setConnection("Connecting…");

			api.state(invitation)
					.then((response) => {
						if(generation === game.version()) {
							game.activate(saved, response);
						}
					})
					.catch((error) => {
						if(
								generation === game.version() &&
								error.name !== "AbortError"
						) {
							if(error.status === HttpStatus.NOT_FOUND) {
								sessions.forget(invitation);
								setConnection("No game");
								title.showMenu();
								title.setError("The previous game no longer exists on the server. Start a new one.");
								return;
							}
							title.showJoin(location.href);
							title.setError(error.message);
						}
					});
		} else {
			title.showJoin(location.href);
		}
	}

	// A link to watch (?watch=<id>): follow that game without taking a seat.
	const watchedGame = invitation ? null : parseWatchInvitation(location.href);
	if(watchedGame) openSession(watchedGame, "SPECTATE");

	function setConnection(text) {
		byId("connection").textContent = text;
		byId("connection").dataset.state = "";
	}
	return showScreen;
}
