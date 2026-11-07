import { uiIcon } from "./assets.js";
import { modeDefinition, pluginLabel } from "./modes.js";

const byId = (id) => document.getElementById(id);

export class PluginDialog {
	constructor(api, onChosen) {
		this.api = api;
		this.onChosen = onChosen;
		this.opening = 0;
	}

	async open(mode) {
		const opening = ++this.opening;
		byId("plugin-title").textContent = modeDefinition(mode).name;
		this.show("Buscando plugins…");
		byId("plugin-dialog").showModal();

		let plugins;
		try {
			plugins = await this.api.plugins();
		} catch (error) {
			if(opening === this.opening) this.show(error.message);
			return;
		}
		if(opening !== this.opening || !byId("plugin-dialog").open) return;

		if(plugins.length) {
			this.ask(mode, plugins, []);
		} else {
			this.show("No hay plugins instalados. Añade uno al servidor para jugar este modo.");
		}
	}

	ask(mode, plugins, chosen) {
		const prompts = modeDefinition(mode).pluginPrompts;
		if(chosen.length === prompts.length) {
			byId("plugin-dialog").close();
			this.onChosen(mode, chosen);
			return;
		}
		this.show(prompts[chosen.length], plugins.map((name) =>
			this.option(mode, name, () => this.ask(mode, plugins, [...chosen, name]))));
		byId("plugin-options").firstElementChild.focus();
	}

	option(mode, name, action) {
		const button = document.createElement("button");
		const text = document.createElement("span");
		const label = document.createElement("strong");
		button.type = "button";
		button.className = "title-option";
		label.textContent = pluginLabel(name);
		text.append(label);
		button.append(uiIcon(`mode-${mode.toLowerCase()}`, "title-option-art"), text);
		button.addEventListener("click", action);
		return button;
	}

	show(note, options = []) {
		byId("plugin-note").textContent = note;
		byId("plugin-options").replaceChildren(...options);
	}
}
