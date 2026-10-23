import { pieceImage } from "./assets.js";
import { alfiLines, themeLines } from "./alfi-lines.js";
import { createFace, drawFace, eraseCross } from "./alfi-face.js";

const letterDelay = 28;
const punctuationPause = { ".": 220, "!": 220, "?": 220, ",": 110, "…": 260 };
const idleDelay = 12000;
const sleepDelay = 40000;
const moodDelay = 4000;
const stareDelay = 1600;
const lingerDelay = 5000;

const funnyChance = 0.3;
const themeChance = 0.4;

const keyboardTip = /[←→]/;
const touchScreen = () => !!globalThis.matchMedia?.("(hover: none)").matches;

const kindMoods = {
  greeting: "guino", start: "contento", waiting: "pensando", opponentJoined: "estrellas", error: "triste",
  openingWeird: "sospecha", earlyQueen: "sospecha", castle: "contento", kingWalk: "sorpresa", bongcloud: "sorpresa",
  knightRim: "aburrido", fianchetto: "corazones", pawnRush: "contento", repeat: "aburrido", capture: "contento",
  minorWon: "contento", bishopLost: "triste", enPassant: "sorpresa", queenLost: "ko", queenWon: "estrellas",
  blunder: "aburrido", check: "sorpresa", checkAgain: "sorpresa", promotion: "estrellas", bigLead: "contento",
  comeback: "sorpresa", longGame: "aburrido", quickMate: "sorpresa", victory: "estrellas", defeat: "triste",
  draw: "contento", poked: "enfadado", wakeUp: "sorpresa",
  bossPoke: "sonrisa", bossPoked: "vacio", bossAsleep: "durmiendo",
  bossChecked: "guino", bossCheck: "sonrisa", bossBlunder: "sonrisa", bossQueenLost: "vacio", bossQueenWon: "sonrisa",
  bossLostPiece: "sospecha", bossAhead: "aburrido", bossBehind: "vacio", bossComeback: "brillo", bossPromotion: "sospecha",
  bossTaunt: "sonrisa", bossProfileLosses: "sonrisa", bossProfileWins: "sospecha", bossProfileFirstMove: "sospecha",
  bossProfilePiece: "sospecha",
  bossAgain: "guino", bossAgainMany: "sonrisa", bossWaiting: "aburrido", bossReturn: "sonrisa", bossSleepTalk: "durmiendo",
};
export const moodFor = (kind) => kindMoods[kind] ?? (kind.startsWith("theme-") ? "contento" : "normal");

const activities = { reading: "leyendo", humming: "contento", looking: "normal", juggling: "pensando" };
const idleClasses = ["is-reading", "is-humming", "is-looking", "is-juggling", "is-sleeping"];
const pokeLimit = 5;

export function line(kind, vars = {}, theme = null) {
  const themed = themeLines[theme]?.[kind];
  const source = themed && Math.random() < themeChance ? themed : alfiLines[kind];
  const { calm = [], funny = [] } = source ?? alfiLines.idle;
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
  constructor(root, audio, preferences, { idle = true, voice = "talk", pokes = {} } = {}) {
    this.root = root;
    this.idle = idle;
    this.voice = voice;
    // Frases al pincharle: una vez, muchas veces seguidas y despertándolo.
    this.pokeLines = { poke: "idle", annoyed: "poked", wake: "wakeUp", ...pokes };
    this.audio = audio;
    this.preferences = preferences;
    this.text = root.querySelector(".mascot-text");
    this.bubble = root.querySelector(".mascot-bubble");
    this.timer = null;
    this.idleTimer = null;
    this.sleepTimer = null;
    this.moodTimer = null;
    this.pokeTimer = null;
    this.lingerTimer = null;
    this.letters = [];
    this.priority = 0;
    this.pokes = 0;
    this.lastActivity = null;
    this.theme = preferences?.theme ?? "classic";
    this.mood = "normal";
    // Cara (y clase, p. ej. is-sleeping) a la que vuelve tras hablar.
    this.restMood = "normal";
    this.restClass = null;

    const body = pieceImage("BISHOP", "WHITE", "classic");
    this.face = createFace(body.querySelector(".piece-art"));
    eraseCross(body.querySelector("img"));
    body.append(this.face);
    const book = document.createElement("span");
    book.className = "mascot-book";
    book.setAttribute("aria-hidden", "true");
    book.append(document.createElement("i"));
    const pawn = document.createElement("span");
    pawn.className = "mascot-pawn";
    pawn.setAttribute("aria-hidden", "true");
    pawn.append(pieceImage("PAWN", "WHITE", "classic"));
    const zzz = document.createElement("span");
    zzz.className = "mascot-zzz";
    zzz.setAttribute("aria-hidden", "true");
    zzz.innerHTML = "<i>z</i><i>z</i><i>Z</i>";
    const sprite = root.querySelector(".mascot-sprite");
    sprite.replaceChildren(body, book, pawn, zzz);
    this.sprite = sprite;
    this.draw();

    sprite.addEventListener("click", () => this.poke());
    this.bubble.addEventListener("click", () => this.finish());
  }

  dress(theme) {
    this.theme = theme;
    this.draw();
  }

  setMood(mood) {
    clearTimeout(this.moodTimer);
    if (mood === this.mood) return;
    this.mood = mood;
    this.draw();
  }

  draw() {
    drawFace(this.face, { mood: this.mood, theme: this.theme });
  }

  line(kind, vars) {
    return line(kind, vars, this.theme);
  }

  talk(kind, priority = 0, options = {}, vars = {}) {
    this.say(this.line(kind, vars), priority, { mood: moodFor(kind), ...options });
  }

  poke() {
    const asleep = this.root.classList.contains("is-sleeping");
    clearTimeout(this.pokeTimer);
    this.pokeTimer = setTimeout(() => (this.pokes = 0), 4000);
    this.pokes += 1;
    if (asleep) return this.talk(this.pokeLines.wake, 0, { mood: "durmiendo" });
    if (this.pokes >= pokeLimit) {
      this.pokes = 0;
      return this.talk(this.pokeLines.annoyed, 1);
    }
    this.say(this.line(this.pokeLines.poke), 0, { mood: "apretando" });
    this.moodTimer = setTimeout(() => this.setMood(this.restMood), 500);
  }

  // Como say(), pero devuelve una promesa que se cumple al terminar de escribir (o al ser interrumpido).
  speak(message, options = {}) {
    return new Promise((resolve) => {
      this.say(message, 9, options);
      this.spoken = resolve;
    });
  }

  say(message, priority = 0, { stare = false, mood = "normal" } = {}) {
    if (this.preferences?.mascot === false) return;
    if (this.timer && priority < this.priority) return;
    this.reset(priority);
    this.setMood(mood);
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
    this.resolveSpoken();
    clearTimeout(this.timer);
    clearTimeout(this.lingerTimer);
    this.stopIdle();
    this.root.classList.add("is-active");
    this.root.classList.remove("is-staring");
    this.priority = priority;
  }

  stopIdle() {
    clearTimeout(this.idleTimer);
    clearTimeout(this.sleepTimer);
    this.root.classList.remove(...idleClasses);
  }

  startIdle() {
    const activity = pick(Object.keys(activities).filter((name) => name !== this.lastActivity));
    this.lastActivity = activity;
    this.root.classList.add(`is-${activity}`);
    this.setMood(activities[activity]);
    this.sleepTimer = setTimeout(() => {
      this.root.classList.remove(`is-${activity}`);
      this.root.classList.add("is-sleeping");
      this.setMood("durmiendo");
    }, sleepDelay);
  }

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
      if (/[\p{L}\p{N}]/u.test(character) && index % 2) this.audio?.play(this.voice);
      this.timer = setTimeout(step, letterDelay + (punctuationPause[character] ?? 0));
    };
    step();
  }

  resolveSpoken() {
    const spoken = this.spoken;
    this.spoken = null;
    spoken?.();
  }

  silence() {
    this.resolveSpoken();
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
    this.resolveSpoken();
    this.timer = null;
    this.priority = 0;
    this.root.classList.remove("is-talking");
    this.stopIdle();
    if (this.restClass) this.root.classList.add(this.restClass);
    this.moodTimer = setTimeout(() => this.setMood(this.restMood), moodDelay);
    if (this.idle) this.idleTimer = setTimeout(() => this.startIdle(), idleDelay);
    this.lingerTimer = setTimeout(() => this.root.classList.remove("is-active"), lingerDelay);
  }
}
