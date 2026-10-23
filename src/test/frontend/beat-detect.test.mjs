import test from "node:test";
import assert from "node:assert/strict";
import { analyzeBeat } from "../../main/resources/web/modules/beat-detect.js";

// Clics cortos a un tempo conocido, empezando con un pequeño desfase.
function clicks(bpm, offset, seconds = 20, sampleRate = 22050) {
  const samples = new Float32Array(seconds * sampleRate);
  for (let time = offset; time < seconds; time += 60 / bpm) {
    const start = Math.round(time * sampleRate);
    for (let index = 0; index < 200 && start + index < samples.length; index++) {
      samples[start + index] = Math.sin(index * 1.3) * Math.exp(-index / 40);
    }
  }
  return { samples, sampleRate };
}

test("the tempo and first beat of a click track are recovered", () => {
  const { samples, sampleRate } = clicks(120, 0.13);
  const { bpm, offset } = analyzeBeat(samples, sampleRate);
  assert.ok(Math.abs(bpm - 120) < 1.5, `bpm ${bpm}`);
  assert.ok(Math.abs(offset - 0.13) < 0.05, `offset ${offset}`);
});

test("fast tracks are not halved", () => {
  const { samples, sampleRate } = clicks(150, 0);
  assert.ok(Math.abs(analyzeBeat(samples, sampleRate).bpm - 150) < 2);
});
