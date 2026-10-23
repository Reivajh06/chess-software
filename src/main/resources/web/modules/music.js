export const tempo = 96;
export const stepsPerBar = 16;
const swing = 0.18;

const chords = [
  { bass: 38, tones: [53, 57, 60, 64] },
  { bass: 34, tones: [53, 57, 58, 62] },
  { bass: 43, tones: [57, 58, 62, 65] },
  { bass: 45, tones: [55, 58, 61, 64] },
  { bass: 38, tones: [53, 57, 60, 64] },
  { bass: 34, tones: [53, 57, 58, 62] },
  { bass: 43, tones: [57, 58, 62, 65] },
  { bass: 45, tones: [55, 58, 61, 64] },
  { bass: 41, tones: [53, 57, 60, 64] },
  { bass: 36, tones: [55, 59, 60, 64] },
  { bass: 38, tones: [53, 57, 60, 62] },
  { bass: 45, tones: [55, 57, 61, 64] },
  { bass: 34, tones: [53, 57, 58, 62] },
  { bass: 43, tones: [55, 58, 62, 65] },
  { bass: 40, tones: [55, 58, 62, 64] },
  { bass: 45, tones: [55, 58, 61, 64] },
];

const melody = [
  [[0, 74, 3], [3, 72, 1], [4, 69, 2], [6, 72, 2], [8, 74, 4], [12, 77, 2], [14, 76, 2]],
  [[0, 74, 6], [6, 72, 2], [8, 70, 2], [10, 69, 2], [12, 70, 4]],
  [[0, 67, 2], [2, 70, 2], [4, 74, 2], [6, 77, 4], [10, 76, 2], [12, 74, 4]],
  [[0, 73, 4], [4, 70, 2], [6, 69, 2], [8, 67, 2], [10, 69, 6]],
  [[0, 74, 3], [3, 72, 1], [4, 69, 2], [6, 72, 2], [8, 74, 4], [12, 77, 2], [14, 76, 2]],
  [[0, 74, 4], [4, 77, 2], [6, 79, 2], [8, 81, 6], [14, 79, 2]],
  [[0, 77, 2], [2, 76, 2], [4, 74, 4], [8, 70, 2], [10, 72, 2], [12, 74, 4]],
  [[0, 73, 6], [6, 76, 2], [8, 73, 2], [10, 70, 2], [12, 69, 4]],
  [[8, 72, 4], [12, 76, 4]],
  [[0, 79, 8], [12, 76, 4]],
  [[8, 74, 4], [12, 77, 4]],
  [[0, 76, 6], [6, 73, 2], [8, 69, 8]],
  [[8, 74, 4], [12, 77, 4]],
  [[0, 82, 6], [6, 81, 2], [8, 79, 4], [12, 77, 4]],
  [[0, 76, 4], [4, 74, 4], [8, 70, 4], [12, 67, 4]],
  [[0, 69, 2], [2, 73, 2], [4, 76, 2], [6, 79, 2], [8, 82, 4], [12, 81, 4]],
];

const bassLine = [[0, 0, 2], [3, 12, 1], [6, 0, 1], [7, 7, 1], [10, 0, 2], [13, 10, 1], [14, "next", 2]];
const harpsichord = [0, 2, 1, 2, 0, 2, 1, 2, 3, 2, 1, 2, 3, 2, 1, 2];
const kicks = [0, 6, 10];

export const loopSteps = chords.length * stepsPerBar;

export function stepDuration() {
  return 60 / tempo / 4;
}

export function frequency(midi) {
  return 440 * 2 ** ((midi - 69) / 12);
}

export function notesAt(step) {
  const bar = Math.floor(step / stepsPerBar) % chords.length;
  const beat = step % stepsPerBar;
  const chord = chords[bar];
  const notes = [];

  if (beat === 0) notes.push({ voice: "pad", tones: chord.tones, steps: stepsPerBar });

  for (const [at, interval, steps] of bassLine) {
    if (at !== beat) continue;
    const midi = interval === "next" ? chords[(bar + 1) % chords.length].bass - 1 : chord.bass + interval;
    notes.push({ voice: "bass", midi, steps });
  }

  for (const [at, midi, steps] of melody[bar]) {
    if (at === beat) notes.push({ voice: "lead", midi, steps });
  }

  if (bar >= 8) notes.push({ voice: "harpsichord", midi: chord.tones[harpsichord[beat]] + 12, steps: 1 });

  if (beat % 4 === 0) notes.push({ voice: beat % 8 === 0 ? "tick" : "tock", steps: 1 });
  if (kicks.includes(beat)) notes.push({ voice: "kick", steps: 1 });

  return notes;
}

// Cada pista expone su tempo y su partitura; el bucle vuelve a loopStart al llegar a loopSteps.
export const calmTrack = { tempo, swing, loopSteps, loopStart: 0, notesAt };

export class BackgroundMusic {
  constructor(audio, preferences) {
    this.audio = audio;
    this.preferences = preferences;
    this.timer = null;
    this.output = null;
    this.waves = null;
    this.track = calmTrack;
    // Corte dramático: mientras dure, sync() no vuelve a arrancar la música.
    this.silenced = false;
  }

  get playing() {
    return this.timer !== null;
  }

  sync() {
    const context = this.audio.context;
    const wanted = !!context && !this.silenced && this.preferences.music && !this.preferences.muted &&
      this.preferences.musicVolume > 0;

    if (wanted && !this.playing) this.start(context);
    else if (!wanted && this.playing) this.stop();

    if (this.output) {
      this.output.gain.setTargetAtTime(this.volume(), context.currentTime, 0.05);
    }
  }

  // Silencio en seco, sin fundido.
  cut() {
    this.silenced = true;
    this.stop(true);
  }

  play(track) {
    this.silenced = false;
    if (track !== this.track) {
      this.track = track;
      // Cambio de tema con un fundido corto, no en seco.
      if (this.playing) this.stop();
    }
    this.sync();
  }

  volume() {
    return (this.preferences.musicVolume / 100) * 0.9;
  }

  start(context) {
    if (this.track.kind === "file") return this.startFile(context);
    this.waves ??= { pulse: pulseWave(context, 0.25), thin: pulseWave(context, 0.125) };
    this.output = context.createGain();
    this.output.gain.value = 0;
    this.output.connect(context.destination);
    this.warble = context.createOscillator();
    this.warble.frequency.value = 0.4;
    this.warbleDepth = context.createGain();
    this.warbleDepth.gain.value = 14;
    this.warble.connect(this.warbleDepth);
    this.warble.start();
    this.step = 0;
    this.nextTime = context.currentTime + 0.1;
    this.schedule(context);
    this.timer = setInterval(() => this.schedule(context), 100);
  }

  // Pista de audio: el archivo suena en bucle (desde loopFrom si tiene intro) y cada pulso se avisa con onBeat.
  startFile(context) {
    const track = this.track;
    if (!track.buffer) {
      // Sin archivo, suena la música normal.
      if (track.failed) {
        this.track = calmTrack;
        return this.start(context);
      }
      track.load(context).then(() => track === this.track && this.sync());
      return;
    }
    this.output = context.createGain();
    this.output.gain.value = 0;
    this.output.connect(context.destination);
    const source = context.createBufferSource();
    source.buffer = track.buffer;
    source.loop = true;
    source.loopStart = track.loopFrom ?? 0;
    source.loopEnd = track.loopTo ?? track.buffer.duration;
    const level = context.createGain();
    level.gain.value = 0.55;
    source.connect(level).connect(this.output);
    this.source = source;
    this.warble = null;
    this.fileStart = context.currentTime + 0.05;
    source.start(this.fileStart);
    this.beatIndex = 0;
    this.scheduleFile(context);
    this.timer = setInterval(() => this.scheduleFile(context), 100);
  }

  scheduleFile(context) {
    const { buffer, bpm, offset } = this.track;
    if (!(bpm > 30)) return;
    const beat = 60 / bpm;
    const from = this.track.loopFrom ?? 0;
    const to = this.track.loopTo ?? buffer.duration;
    for (;;) {
      const elapsed = offset + this.beatIndex * beat;
      const time = this.fileStart + elapsed;
      if (time > context.currentTime + 0.4) return;
      // Posición dentro del archivo: tras la primera vuelta se repite solo el tramo del bucle.
      const within = elapsed < to ? elapsed : from + (elapsed - to) % (to - from);
      if (time >= context.currentTime - 0.05 && this.onBeat) {
        const info = { step: Math.round((within - offset) / beat) * 4, track: this.track };
        setTimeout(() => this.playing && this.track === info.track && this.onBeat(info), Math.max(0, (time - context.currentTime) * 1000));
      }
      this.beatIndex++;
    }
  }

  stop(immediate = false) {
    clearInterval(this.timer);
    this.timer = null;
    const output = this.output;
    const warble = this.warble;
    const source = this.source;
    this.source = null;
    this.output = null;
    if (!output) return;
    const context = this.audio.context;
    output.gain.cancelScheduledValues(context.currentTime);
    output.gain.setTargetAtTime(0, context.currentTime, immediate ? 0.004 : 0.08);
    setTimeout(() => {
      output.disconnect();
      warble?.stop();
      source?.stop();
    }, 600);
  }

  advance(steps) {
    const { loopSteps: total, loopStart } = this.track;
    const next = this.step + steps;
    this.step = next < total ? next : loopStart + (next - loopStart) % (total - loopStart);
  }

  schedule(context) {
    const { tempo: bpm, swing: shuffle, notesAt: score } = this.track;
    const step = 60 / bpm / 4;
    if (this.nextTime < context.currentTime - step) {
      const missed = Math.ceil((context.currentTime - this.nextTime) / step);
      this.nextTime += missed * step;
      this.advance(missed);
    }
    while (this.nextTime < context.currentTime + 0.4) {
      const time = this.nextTime + (this.step % 2 ? step * shuffle : 0);
      for (const note of score(this.step)) this.sound(context, note, time, note.steps * step);
      if (this.onBeat && this.step % 4 === 0) {
        const beat = { step: this.step, track: this.track };
        setTimeout(() => this.playing && this.onBeat(beat), Math.max(0, (time - context.currentTime) * 1000));
      }
      this.nextTime += step;
      this.advance(1);
    }
  }

  sound(context, note, time, duration) {
    switch (note.voice) {
      case "pad":
        for (const midi of note.tones) {
          for (const cents of [-7, 7]) {
            this.voice(context, { wave: "triangle", midi, time, duration, gain: 0.018, attack: 0.35, cents, warble: true });
          }
        }
        return;
      case "bass":
        return this.voice(context, { wave: "triangle", midi: note.midi, time, duration, gain: 0.2, attack: 0.008, decay: 0.6 });
      case "lead":
        return this.voice(context, { wave: this.waves.pulse, midi: note.midi, time, duration, gain: 0.055, attack: 0.012, warble: true });
      case "harpsichord":
        return this.voice(context, { wave: this.waves.thin, midi: note.midi, time, duration: 0.22, gain: 0.03, attack: 0.002, decay: 0.15 });
      case "kick":
        return this.drum(context, { from: 130, to: 42, time, length: 0.14, gain: 0.32, type: "sine" });
      case "tick":
        return this.drum(context, { from: 2100, to: 2000, time, length: 0.025, gain: 0.05, type: "square" });
      case "tock":
        return this.drum(context, { from: 1300, to: 1200, time, length: 0.035, gain: 0.05, type: "square" });
    }
  }

  voice(context, { wave, midi, time, duration, gain, attack, decay = 1, cents = 0, warble = false }) {
    const oscillator = context.createOscillator();
    if (typeof wave === "string") oscillator.type = wave;
    else oscillator.setPeriodicWave(wave);
    oscillator.frequency.value = frequency(midi);
    oscillator.detune.value = cents;
    if (warble) {
      const depth = this.warbleDepth;
      depth.connect(oscillator.detune);
      oscillator.onended = () => depth.disconnect(oscillator.detune);
    }
    const envelope = context.createGain();
    envelope.gain.setValueAtTime(0, time);
    envelope.gain.linearRampToValueAtTime(gain, time + attack);
    if (decay < 1) envelope.gain.exponentialRampToValueAtTime(gain * decay, time + attack + duration * 0.3);
    envelope.gain.exponentialRampToValueAtTime(0.0001, time + duration * 0.98);
    oscillator.connect(envelope).connect(this.output);
    oscillator.start(time);
    oscillator.stop(time + duration);
  }

  drum(context, { from, to, time, length, gain, type }) {
    const oscillator = context.createOscillator();
    oscillator.type = type;
    oscillator.frequency.setValueAtTime(from, time);
    oscillator.frequency.exponentialRampToValueAtTime(to, time + length);
    const envelope = context.createGain();
    envelope.gain.setValueAtTime(gain, time);
    envelope.gain.exponentialRampToValueAtTime(0.0001, time + length);
    oscillator.connect(envelope).connect(this.output);
    oscillator.start(time);
    oscillator.stop(time + length + 0.01);
  }
}

function pulseWave(context, duty) {
  const harmonics = 32;
  const real = new Float32Array(harmonics);
  const imaginary = new Float32Array(harmonics);
  for (let n = 1; n < harmonics; n++) {
    real[n] = Math.sin(2 * Math.PI * n * duty) / (Math.PI * n);
    imaginary[n] = (1 - Math.cos(2 * Math.PI * n * duty)) / (Math.PI * n);
  }
  return context.createPeriodicWave(real, imaginary);
}
