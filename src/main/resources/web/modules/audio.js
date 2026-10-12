import { selectedSoundSet, soundFiles } from "./soundsets.js";

export class GameAudio {
  constructor(preferences) {
    this.preferences = preferences;
    this.context = null;
    this.output = null;
    this.buffers = new Map();
    this.sources = new Set();
    this.soundSet = selectedSoundSet(preferences);
    this.generation = 0;
  }
  async unlock() {
    try {
      if (!this.context) {
        this.context = new (window.AudioContext || window.webkitAudioContext)();
        this.output = this.context.createGain();
        this.output.connect(this.context.destination);
        this.updatePreferences();
      }
      if (this.context.state === "suspended") await this.context.resume();
      this.preload();
    } catch { /* Audio support or autoplay permission may be unavailable. */ }
  }

  updatePreferences() {
    const next = selectedSoundSet(this.preferences);
    const changed = next !== this.soundSet;
    this.soundSet = next;
    const volume = this.preferences.muted ? 0 : this.preferences.volume / 100;
    if (this.output) this.output.gain.setTargetAtTime(volume, this.context.currentTime, 0.01);
    if (changed || !volume) {
      this.generation++;
      for (const source of this.sources) source.stop();
      this.sources.clear();
    }
    if (changed) this.preload();
  }

  load(soundSet, kind) {
    const file = soundFiles[kind];
    const key = `${soundSet}:${file}`;
    if (!this.buffers.has(key)) {
      const url = new URL(`../assets/sfx/${soundSet}/${file}.mp3`, import.meta.url);
      const buffer = fetch(url)
        .then(response => {
          if (!response.ok) throw new Error("Sound unavailable");
          return response.arrayBuffer();
        })
        .then(data => this.context.decodeAudioData(data))
        .catch(error => {
          this.buffers.delete(key);
          throw error;
        });
      this.buffers.set(key, buffer);
    }
    return this.buffers.get(key);
  }

  preload() {
    if (!this.context) return;
    // Share in-flight loads with playback, so the first click is not lost.
    void Promise.allSettled(Object.keys(soundFiles).map(kind => this.load(this.soundSet, kind)));
  }

  async play(kind, delay = 0) {
    if (!this.context || this.preferences.muted || !this.preferences.volume || !soundFiles[kind]) return;
    this.updatePreferences();
    const generation = this.generation;
    const soundSet = this.soundSet;
    const start = this.context.currentTime + delay;
    try {
      if (this.context.state === "suspended") await this.context.resume();
      const buffer = await this.load(soundSet, kind);
      if (generation !== this.generation || this.preferences.muted || !this.preferences.volume ||
          soundSet !== selectedSoundSet(this.preferences) || this.context.state !== "running") return;
      const source = this.context.createBufferSource();
      source.buffer = buffer;
      source.connect(this.output);
      source.onended = () => {
        this.sources.delete(source);
        source.disconnect();
      };
      this.sources.add(source);
      source.start(Math.max(start, this.context.currentTime));
    } catch { /* A missing sound must not interrupt a game. */ }
  }
}
