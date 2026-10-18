import test from "node:test";
import assert from "node:assert/strict";
import { GameAudio } from "../../main/resources/web/modules/audio.js";
import { synthCues } from "../../main/resources/web/modules/sfx-synth.js";
import { loadPreferences, savePreferences } from "../../main/resources/web/modules/preferences.js";

const param = () => ({
  value: 0,
  events: [],
  setValueAtTime(value, time) { this.events.push(["set", value, time]); },
  exponentialRampToValueAtTime(value, time) { this.events.push(["ramp", value, time]); },
  setTargetAtTime(value) { this.value = value; },
});

class AudioContextStub {
  constructor() {
    this.state = "suspended";
    this.currentTime = 10;
    this.sampleRate = 8000;
    this.destination = {};
    this.voices = [];
  }
  async resume() { this.state = "running"; }
  node(extra = {}) {
    return { connect(next) { return next; }, disconnect() {}, ...extra };
  }
  voice(kind) {
    const voice = this.node({
      kind,
      frequency: param(),
      start(time) { this.time = time; },
      stop() { this.stopped = true; },
    });
    this.voices.push(voice);
    return voice;
  }
  createGain() { return this.node({ gain: param() }); }
  createBiquadFilter() { return this.node({ frequency: param(), Q: param() }); }
  createOscillator() { return this.voice("oscillator"); }
  createBufferSource() { return this.voice("noise"); }
  createBuffer(_channels, length, sampleRate) {
    const data = new Float32Array(length);
    return { sampleRate, getChannelData: () => data };
  }
}

function stubGlobal(t, key, value) {
  const original = Object.getOwnPropertyDescriptor(globalThis, key);
  Object.defineProperty(globalThis, key, { value, configurable: true, writable: true });
  t.after(() => {
    if (original) Object.defineProperty(globalThis, key, original);
    else delete globalThis[key];
  });
}

async function setup(t, preferences = { muted: false, volume: 55 }) {
  stubGlobal(t, "window", { AudioContext: AudioContextStub });
  const audio = new GameAudio(preferences);
  await audio.unlock();
  return { audio, preferences };
}

test("every game event has its own synthesized cue, scheduled with its delay", async t => {
  const { audio } = await setup(t);
  for (const kind of ["select", "move", "king_move", "capture", "check", "promotion", "victory", "defeat", "draw"]) {
    const before = audio.context.voices.length;
    await audio.play(kind, 0.45);
    const voices = audio.context.voices.slice(before);
    assert.ok(voices.length > 0, kind);
    assert.ok(voices.every(voice => voice.time >= 10.45), kind);
  }
  assert.equal(audio.output.gain.value, 0.55);
});

test("check, king moves and promotion sound different from a plain move", () => {
  const signature = kind => {
    const context = new AudioContextStub();
    context.state = "running";
    const voices = synthCues[kind](context, context.createGain(), 0);
    return voices.map(voice => `${voice.kind}:${voice.frequency.events.length}`).join(",") + `#${voices.length}`;
  };
  const move = signature("move");
  for (const kind of ["king_move", "check", "promotion", "capture"]) assert.notEqual(signature(kind), move, kind);
});

test("muting stops ringing voices and honors zero volume", async t => {
  const { audio, preferences } = await setup(t);
  await audio.play("victory");
  const voices = [...audio.voices];
  assert.ok(voices.length > 0);
  preferences.muted = true;
  audio.updatePreferences();
  assert.ok(voices.every(voice => voice.stopped));
  assert.equal(audio.output.gain.value, 0);
  const count = audio.context.voices.length;
  await audio.play("move");
  preferences.muted = false;
  preferences.volume = 0;
  await audio.play("move");
  assert.equal(audio.context.voices.length, count);
});

test("unknown cues and a missing audio context are ignored", async t => {
  const silent = new GameAudio({ muted: false, volume: 50 });
  await silent.play("move");
  const { audio } = await setup(t);
  await audio.play("nonexistent");
  assert.equal(audio.context.voices.length, 0);
});

test("preferences keep volume and theme and drop the retired soundset", t => {
  let stored = JSON.stringify({ theme: "warm", volume: 42, soundSet: "16bit" });
  stubGlobal(t, "localStorage", {
    getItem: () => stored,
    setItem: (_key, value) => { stored = value; },
  });
  const preferences = loadPreferences();
  assert.equal(preferences.volume, 42);
  assert.equal(preferences.theme, "warm");
  assert.equal("soundSet" in preferences, false);
  savePreferences(preferences);
  assert.equal(JSON.parse(stored).soundSet, undefined);
});
