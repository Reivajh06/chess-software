import { synthCues } from "./sfx-synth.js";

export class GameAudio {
	constructor(preferences) {
		this.preferences = preferences;
		this.context = null;
		this.output = null;
		this.voices = new Set();
	}
	async unlock() {
		try {
			if(!this.context) {
				this.context = new (window.AudioContext || window.webkitAudioContext)();
				this.output = this.context.createGain();
				this.output.connect(this.context.destination);
				this.updatePreferences();
			}
			if(this.context.state === "suspended") await this.context.resume();
		} catch {}
	}

	updatePreferences() {
		const volume = this.preferences.muted ? 0 : this.preferences.volume / 100;
		if(this.output) this.output.gain.setTargetAtTime(volume, this.context.currentTime, 0.01);
		if(!volume) {
			for(const voice of this.voices) {
				try { voice.stop(); } catch {}
			}
			this.voices.clear();
		}
	}

	async play(kind, delay = 0) {
		if(!this.context || this.preferences.muted || !this.preferences.volume || !synthCues[kind]) return;
		this.updatePreferences();
		try {
			if(this.context.state === "suspended") await this.context.resume();
			if(this.context.state !== "running" || this.preferences.muted) return;
			const start = Math.max(this.context.currentTime + delay, this.context.currentTime);
			for(const voice of synthCues[kind](this.context, this.output, start)) {
				this.voices.add(voice);
				voice.onended = () => {
					this.voices.delete(voice);
					voice.disconnect();
				};
			}
		} catch {}
	}
}
