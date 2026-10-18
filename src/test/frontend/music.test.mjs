import test from "node:test";
import assert from "node:assert/strict";
import { frequency, loopSteps, notesAt, stepDuration } from "../../main/resources/web/modules/music.js";

const pitched = (note) => typeof note.midi === "number";

test("the loop lasts sixteen bars of sixteenth notes at 96 BPM", () => {
  assert.equal(loopSteps, 256);
  assert.equal(stepDuration(), 60 / 96 / 4);
});

test("every bar opens with a pad chord, a bass note and a kick", () => {
  for (let step = 0; step < loopSteps; step += 16) {
    const voices = notesAt(step).map((note) => note.voice);
    for (const voice of ["pad", "bass", "kick"]) assert.ok(voices.includes(voice), `${voice} at ${step}`);
  }
});

test("the chess clock ticks on beats 1 and 3 and tocks on the backbeats", () => {
  for (let step = 0; step < loopSteps; step++) {
    const voices = notesAt(step).map((note) => note.voice);
    const beat = step % 16;
    assert.equal(voices.includes("tick"), beat === 0 || beat === 8, `tick at ${step}`);
    assert.equal(voices.includes("tock"), beat === 4 || beat === 12, `tock at ${step}`);
  }
});

test("the harpsichord plays only in the B section, one note per step", () => {
  for (let step = 0; step < loopSteps; step++) {
    const harpsichord = notesAt(step).filter((note) => note.voice === "harpsichord");
    assert.equal(harpsichord.length, step >= 128 ? 1 : 0, `step ${step}`);
  }
});

test("the score wraps around and stays within a comfortable range", () => {
  assert.deepEqual(notesAt(loopSteps), notesAt(0));
  for (let step = 0; step < loopSteps; step++) {
    for (const note of notesAt(step)) {
      assert.ok(note.steps >= 1);
      const pitches = note.tones ?? (pitched(note) ? [note.midi] : []);
      for (const midi of pitches) assert.ok(midi >= 33 && midi <= 84, `midi ${midi} at step ${step}`);
    }
  }
});

test("MIDI notes convert to concert pitch", () => {
  assert.equal(frequency(69), 440);
  assert.equal(frequency(81), 880);
});
