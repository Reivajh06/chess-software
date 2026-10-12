export class GameAudio {
  constructor(preferences) {
    this.preferences = preferences;
    this.context = null;
  }
  async unlock() {
    try {
      this.context ??= new (window.AudioContext || window.webkitAudioContext)();
      if (this.context.state === "suspended") await this.context.resume();
    } catch {}
  }
  play(kind) {
    if (
      !this.context ||
      this.context.state !== "running" ||
      this.preferences.muted ||
      !this.preferences.volume
    )
      return;
    const sequences = {
      move: [280, 220],
      capture: [170, 90],
      check: [620, 830],
      victory: [392, 494, 587, 784],
      defeat: [392, 330, 262, 196],
      draw: [392, 440, 392],
    };
    const frequencies = sequences[kind] ?? sequences.move;
    frequencies.forEach((frequency, index) => {
      const start = this.context.currentTime + index * 0.11;
      const oscillator = this.context.createOscillator(),
        gain = this.context.createGain();
      oscillator.type = ["move", "capture"].includes(kind) ? "triangle" : "sine";
      oscillator.frequency.value = frequency;
      gain.gain.setValueAtTime(0, start);
      gain.gain.linearRampToValueAtTime((this.preferences.volume / 100) * 0.13, start + 0.008);
      gain.gain.exponentialRampToValueAtTime(0.0001, start + 0.18);
      oscillator.connect(gain);
      gain.connect(this.context.destination);
      oscillator.start(start);
      oscillator.stop(start + 0.19);
    });
  }
}
