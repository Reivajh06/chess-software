// Alfi, the ArchChess mascot: the classic white bishop with a speech bubble.
// Text is typed letter by letter with a pitched blip per letter (Balatro
// style). Everything about the mascot lives in this module, its markup
// (.mascot) and the "Mascot" block in styles.css, so it is easy to drop.
import { pieceImage } from "./assets.js";
import { alfiLines } from "./alfi-lines.js";

const letterDelay = 28;
const punctuationPause = { ".": 220, "!": 220, "?": 220, ",": 110, "…": 260 };
// After this long without talking Alfi pulls out a book.
const readingDelay = 12000;
// How long Alfi stares at the board after a blunder before saying anything.
const stareDelay = 1600;
// How long a finished line stays up where Alfi only pops in to talk (mobile).
const lingerDelay = 5000;

// Funny lines show up only now and then, so Alfi stays helpful.
const funnyChance = 0.3;

// Keyboard tips make no sense on touch screens.
const keyboardTip = /[←→]/;
const touchScreen = () => !!globalThis.matchMedia?.("(hover: none)").matches;

export function line(kind, vars = {}) {
  const { calm = [], funny = [] } = alfiLines[kind] ?? alfiLines.idle;
  const touch = touchScreen();
  const usable = (lines) => {
    const fitting = lines.filter((text) => !touch || !keyboardTip.test(text));
    return fitting.length ? fitting : null;
  };
  const preferFunny = Math.random() < funnyChance;
  const text = pick((preferFunny ? usable(funny) ?? usable(calm) : usable(calm) ?? usable(funny)) ?? calm.concat(funny));
  return text.replace(/\{(\w+)\}/g, (_, key) => vars[key] ?? "");
}

export function pick(lines) {
  return lines[Math.floor(Math.random() * lines.length)];
}

export class Mascot {
  constructor(root, audio, preferences) {
    this.root = root;
    this.audio = audio;
    this.preferences = preferences;
    this.text = root.querySelector(".mascot-text");
    this.bubble = root.querySelector(".mascot-bubble");
    this.timer = null;
    this.idleTimer = null;
    this.lingerTimer = null;
    this.letters = [];
    this.priority = 0;
    const sprite = root.querySelector(".mascot-sprite");
    const book = document.createElement("span");
    book.className = "mascot-book";
    book.setAttribute("aria-hidden", "true");
    book.append(document.createElement("i"));
    sprite.replaceChildren(pieceImage("BISHOP", "WHITE", "classic"), book);
    // Poke Alfi for a tip; click the bubble to finish the current line.
    sprite.addEventListener("click", () => this.say(this.line("idle")));
    this.bubble.addEventListener("click", () => this.finish());
  }

  line(kind, vars) {
    return line(kind, vars);
  }

  // Lower-priority lines never cut off a more important one still typing.
  // With `stare`, Alfi first leans towards the board in silence.
  say(message, priority = 0, { stare = false } = {}) {
    if (this.preferences?.mascot === false) return;
    if (this.timer && priority < this.priority) return;
    this.reset(priority);
    if (stare) {
      this.root.classList.add("is-staring");
      this.show("…", false);
      this.timer = setTimeout(() => {
        this.root.classList.remove("is-staring");
        this.type(message);
      }, stareDelay);
      return;
    }
    this.type(message);
  }

  reset(priority) {
    clearTimeout(this.timer);
    clearTimeout(this.idleTimer);
    clearTimeout(this.lingerTimer);
    this.root.classList.add("is-active");
    this.root.classList.remove("is-reading", "is-staring");
    this.priority = priority;
  }

  // Lay out every letter hidden first, so the bubble keeps its final size
  // while typing instead of reflowing on each character.
  show(message, hidden = true) {
    const letters = [];
    const words = message.split(/(\s+)/).map((chunk) => {
      if (/^\s+$/.test(chunk)) {
        const space = document.createElement("span");
        space.className = "space";
        space.textContent = " ";
        return space;
      }
      const word = document.createElement("span");
      word.className = "word";
      for (const character of chunk) {
        const letter = document.createElement("span");
        letter.className = hidden ? "letter" : "letter shown";
        letter.textContent = character;
        word.append(letter);
        letters.push(letter);
      }
      return word;
    });
    this.text.replaceChildren(...words);
    this.bubble.hidden = false;
    this.letters = letters;
    return letters;
  }

  type(message) {
    const letters = this.show(message);
    this.root.classList.add("is-talking");
    let index = 0;
    const step = () => {
      if (index >= letters.length) return this.done();
      const letter = letters[index++];
      letter.classList.add("shown");
      const character = letter.textContent;
      if (/[\p{L}\p{N}]/u.test(character) && index % 2) this.audio?.play("talk");
      this.timer = setTimeout(step, letterDelay + (punctuationPause[character] ?? 0));
    };
    step();
  }

  // Stops typing and pending lines at once (hidden, or its screen left).
  silence() {
    clearTimeout(this.timer);
    clearTimeout(this.lingerTimer);
    this.timer = null;
    this.priority = 0;
    this.root.classList.remove("is-talking", "is-staring", "is-active");
    for (const letter of this.letters) letter.classList.add("shown");
  }

  finish() {
    if (!this.timer || this.root.classList.contains("is-staring")) return;
    clearTimeout(this.timer);
    for (const letter of this.letters) letter.classList.add("shown");
    this.done();
  }

  done() {
    this.timer = null;
    this.priority = 0;
    this.root.classList.remove("is-talking");
    clearTimeout(this.idleTimer);
    this.idleTimer = setTimeout(() => this.root.classList.add("is-reading"), readingDelay);
    this.lingerTimer = setTimeout(() => this.root.classList.remove("is-active"), lingerDelay);
  }
}
