import { byId, dialog, tabGroup } from "./ui.js";
import { savePreferences } from "./preferences.js";
import { applyTheme, populateThemes } from "./themes.js";
import { renderAppearance, populateThemeGrid, markChoice } from "./interface.js";

export function setupSettings({ preferences, audio, music, alfi, titleMascot, gameMascot, activeMascot, renderGame }) {
	const controls = { "theme-select": "theme", volume: "volume", "music-volume": "musicVolume",
		"music-enabled": "music", muted: "muted", "mascot-enabled": "mascot" };
	populateThemes(byId("theme-select"));

	function pickOption(selectId, value) {
		const select = byId(selectId);
		if(select.value === value) return;
		select.value = value;
		select.dispatchEvent(new Event("input"));
	}

	populateThemeGrid(byId("theme-grid"), (id) => pickOption("theme-select", id));

	let lastHover = 0;
	function hoverSound() {
		const now = performance.now();
		if(now - lastHover < 60) return;
		lastHover = now;
		audio.play("hover");
	}

	const interfaceTargets = ".title-option, .choice, [role='tab'], .match-actions button, .result-card button, .promotion-choice, .tool-button";
	for(const type of ["pointerover", "focusin"]) {
		document.addEventListener(type, (event) => {
			const target = event.target.closest?.(interfaceTargets);
			if(!target || target.disabled || target.contains(event.relatedTarget)) return;
			hoverSound();
		});
	}
	document.addEventListener("click", (event) => {
		const target = event.target.closest?.(".title-option, .choice, .result-card button, .promotion-choice");
		if(target && !target.disabled) audio.play("confirm");
	});

	for(const grid of document.querySelectorAll(".choice-grid")) {
		grid.addEventListener("keydown", (event) => {
			const step = { ArrowRight: 1, ArrowDown: 1, ArrowLeft: -1, ArrowUp: -1 }[event.key];
			if(!step) return;
			event.preventDefault();
			const options = [...grid.children];
			const next = options[(options.indexOf(document.activeElement) + step + options.length) % options.length];
			next.focus();
			next.click();
		});
	}

	function syncSettings() {
		markChoice(byId("theme-grid"), preferences.theme);
		for(const id of ["volume", "music-volume"]) {
			const input = byId(id);
			input.style.setProperty("--fill", `${input.value}%`);
			byId(`${id}-value`).textContent = `${input.value}%`;
		}
		byId("music-volume").disabled = !preferences.music;
		byId("volume").disabled = preferences.muted;
	}

	const settingsTabs = ["look", "sound", "mascot"];
	tabGroup(Object.fromEntries(settingsTabs.map((name) => [name, byId(`tab-${name}`)])), (name) => {
		for(const tab of settingsTabs) byId(`panel-${tab}`).hidden = tab !== name;
	});
	applyTheme(preferences.theme);

	function applyMascot() {
		document.body.dataset.mascot = preferences.mascot ? "on" : "off";
		if(!preferences.mascot) {
			titleMascot.silence();
			gameMascot.silence();
		}
		byId("mascot-enabled").checked = preferences.mascot;
	}

	for(const button of document.querySelectorAll(".mascot-dismiss")) {
		button.addEventListener("click", (event) => {
			event.stopPropagation();
			preferences.mascot = false;
			applyMascot();
			savePreferences(preferences);
		});
	}

	function updateAudio() {
		audio.updatePreferences();
		music.sync();
		savePreferences(preferences);
		renderAppearance(preferences);
		syncSettings();
	}

	for(const [id, key] of Object.entries(controls)) {
		const element = byId(id);
		const checkbox = element.type === "checkbox";
		const numeric = element.type === "range";

		if(checkbox) {
			element.checked = preferences[key];
		} else {
			element.value = preferences[key];
		}

		element.addEventListener("input", () => {
			const value = checkbox
					? element.checked
					: numeric
							? Number(element.value)
							: element.value;

			if(alfi.resist(key, value)) {
				syncControls();
				return;
			}
			preferences[key] = value;

			syncSettings();

			if(key === "mascot") {
				applyMascot();
				savePreferences(preferences);
				if(preferences.mascot) {
					const mascot = document.body.dataset.screen === "game" ? activeMascot() : titleMascot;
					mascot.talk("greeting");
				}
				return;
			}

			if(key === "theme") {
				applyTheme(preferences.theme);
				savePreferences(preferences);
				renderGame();
				titleMascot.dress(preferences.theme);
				gameMascot.dress(preferences.theme);
				alfi.dress(preferences.theme);
				const mascot = document.body.dataset.screen === "game" ? activeMascot() : titleMascot;
				mascot.talk(`theme-${preferences.theme}`, 1);
				return;
			}

			updateAudio();
		});
	}

	byId("volume").addEventListener("change", () => audio.play("move"));

	function syncControls() {
		for(const [id, key] of Object.entries(controls)) {
			const element = byId(id);
			if(element.type === "checkbox") element.checked = preferences[key];
			else element.value = preferences[key];
		}
		syncSettings();
	}

	alfi.onRulesChanged = () => {
		applyTheme(preferences.theme);
		applyMascot();
		audio.updatePreferences();
		music.sync();
		renderAppearance(preferences);
		syncControls();
		renderGame();
	};

	byId("sound-toggle").addEventListener("click", () => {
		if(alfi.resist("muted", !preferences.muted)) return syncControls();
		preferences.muted = !preferences.muted;
		byId("muted").checked = preferences.muted;
		updateAudio();
	});

	byId("music-toggle").addEventListener("click", async () => {
		if(alfi.resist("music", !preferences.music)) return syncControls();
		preferences.music = !preferences.music;
		byId("music-enabled").checked = preferences.music;
		await audio.unlock();
		updateAudio();
	});

	byId("settings-open").addEventListener("click", () => {
		syncSettings();
		dialog("settings-dialog");
		byId("settings-dialog").querySelector('[role="tab"][aria-selected="true"]').focus({ preventScroll: true });
	});

	async function unlockAudio() {
		await audio.unlock();
		music.sync();
	}

	document.addEventListener("pointerdown", unlockAudio, { once: true });
	document.addEventListener("keydown", unlockAudio, { once: true });

	function pauseAudio() {
		music.stop();
		audio.context?.suspend().catch(() => {});
	}

	document.addEventListener("visibilitychange", async () => {
		if(document.hidden) {
			pauseAudio();
			return;
		}

		try {
			await audio.context?.resume();
		} catch {}
		music.sync();
	});


	applyMascot();
	syncControls();
	return pauseAudio;
}
