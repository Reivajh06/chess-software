import { byId, dialog, optionButton } from "./ui.js";
import { modeDefinition, modeIcon, pluginLabel } from "./modes.js";


export class PluginDialog {
	constructor(api, onChosen) {
		this.api = api;
		this.onChosen = onChosen;
		this.opening = 0;
	}

	async open(mode) {
		const opening = ++this.opening;
		byId("plugin-title").textContent = modeDefinition(mode).name;
		this.show("Looking for plugins…");
		dialog("plugin-dialog");

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
			this.show("No plugins installed. Add one to the server to play this mode.");
		}
	}

	ask(mode, plugins, chosen) {
		const prompts = modeDefinition(mode).pluginPrompts;
		if(chosen.length === prompts.length) {
			dialog("plugin-dialog", false);
			this.onChosen(mode, chosen, plugins);
			return;
		}
		this.show(prompts[chosen.length], plugins.map((name) =>
			optionButton(pluginLabel(name), null, modeIcon(mode), () => this.ask(mode, plugins, [...chosen, name]))));
		byId("plugin-options").firstElementChild.focus();
	}

	show(note, options = []) {
		byId("plugin-note").textContent = note;
		byId("plugin-options").replaceChildren(...options);
	}
}
