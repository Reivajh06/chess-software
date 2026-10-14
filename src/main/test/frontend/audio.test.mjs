import test from "node:test";
import assert from "node:assert/strict";
import { GameAudio } from "../../main/resources/web/modules/audio.js";
import { loadPreferences, savePreferences } from "../../main/resources/web/modules/preferences.js";

class AudioContextStub {
  constructor() {
    this.state = "suspended";
    this.currentTime = 10;
    this.destination = {};
    this.voices = [];
  }
  async resume() { this.state = "running"; }
  createGain() {
    return { connect() {}, gain: { setTargetAtTime(value) { this.value = value; } } };
  }
  async decodeAudioData(data) { return { file: new TextDecoder().decode(data) }; }
  createBufferSource() {
    const voice = { connect() {}, disconnect() {}, start(time) { this.time = time; }, stop() { this.stopped = true; } };
    this.voices.push(voice);
    return voice;
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

async function setup(t, preferences = { soundSet: "8bit", muted: false, volume: 55 }, fetcher) {
  stubGlobal(t, "window", { AudioContext: AudioContextStub });
  const paths = [];
  t.mock.method(globalThis, "fetch", async url => {
    paths.push(url.pathname);
    return fetcher ? fetcher(url) : new Response(url.pathname);
  });
  const audio = new GameAudio(preferences);
  await audio.unlock();
  await Promise.allSettled(audio.buffers.values());
  return { audio, paths, preferences };
}

test("uses distinct packed samples, caches them and schedules delayed result cues", async t => {
  const { audio, paths } = await setup(t);
  for (const [kind, file] of [["select", "select"], ["move", "move"], ["capture", "capture"],
    ["king_move", "king_move"], ["check", "check"], ["victory", "win"], ["defeat", "lose"], ["draw", "draw"]]) {
    await audio.play(kind, 0.45);
    const voice = audio.context.voices.at(-1);
    assert.ok(voice.buffer.file.endsWith(`/8bit/${file}.mp3`));
    assert.equal(voice.time, 10.45);
  }
  assert.equal(paths.length, 8);
  assert.equal(audio.output.gain.value, 0.55);
});

test("switching soundsets stops old voices and uses the new samples without reloading", async t => {
  const { audio, preferences } = await setup(t);
  await audio.play("move");
  const oldVoice = audio.context.voices.at(-1);
  preferences.soundSet = "16bit";
  audio.updatePreferences();
  await audio.play("capture");
  assert.equal(oldVoice.stopped, true);
  assert.ok(audio.context.voices.at(-1).buffer.file.endsWith("/16bit/capture.mp3"));
  await Promise.allSettled(audio.buffers.values());
});

test("muting cancels a sample that is still loading, stops voices and honors zero volume", async t => {
  const { audio, preferences } = await setup(t);
  await audio.play("move");
  const voice = audio.context.voices.at(-1);
  let resolve;
  audio.buffers.set("8bit:capture", new Promise(done => { resolve = done; }));
  const pending = audio.play("capture");
  preferences.muted = true;
  audio.updatePreferences();
  resolve({ file: "capture" });
  await pending;
  assert.equal(voice.stopped, true);
  assert.equal(audio.context.voices.length, 1);
  preferences.muted = false;
  preferences.volume = 0;
  await audio.play("victory");
  assert.equal(audio.context.voices.length, 1);
});

test("a failed sample does not break playback and can be retried", async t => {
  let failed = false;
  const { audio } = await setup(t, undefined, url => {
    if (url.pathname.endsWith("/select.mp3") && !failed) {
      failed = true;
      return new Response("", { status: 404 });
    }
    return new Response(url.pathname);
  });
  await audio.play("select");
  assert.ok(audio.context.voices.at(-1).buffer.file.endsWith("/select.mp3"));
});

test("persists the soundset independently of the board theme and defaults old preferences", t => {
  let stored = JSON.stringify({ theme: "warm", volume: 42 });
  stubGlobal(t, "localStorage", {
    getItem: () => stored,
    setItem: (_key, value) => { stored = value; },
  });
  const preferences = loadPreferences();
  assert.equal(preferences.soundSet, "8bit");
  preferences.soundSet = "16bit";
  savePreferences(preferences);
  assert.equal(loadPreferences().soundSet, "16bit");
  assert.equal(loadPreferences().theme, "warm");
  stored = JSON.stringify({ soundSet: "invalid" });
  assert.equal(loadPreferences().soundSet, "8bit");
});
