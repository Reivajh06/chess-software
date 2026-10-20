import { pieceImage } from "./assets.js";
import { pixelArt } from "./alfi-face.js";
import { Mascot, pick } from "./mascot.js";

const byId = (id) => document.getElementById(id);
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const art = {
  broom: "..e../..e../..e../..e../..e../..e../..e../..e../..e../..e../.rrr./.ooo./ooooo/oAoAo/ooooo/o.o.o",
  wand: "hhhhhhhhww/kkkkkkkkww",
  hat: "..kkkkkkkk../..knnnnnnk../..knmnnnnk../..knmnnnnk../..knmnnnnk../..krrrrrrk../..kRRRRRRk../kkkkkkkkkkkk/knnnnnnnnnnk/kkkkkkkkkkkk",
  puff: "..ss.ss../.swwswws./swwwwwwws/swwwwwwws/.swwwwws./..sssss..",
  dust: "s...s../..s...s/s..s...",
  star: "..y../..y../yyyyy/..y../..y..",
};

const acts = {
  sweep: { length: 6000, sound: null },
  tidy: { length: 5200, sound: "move" },
  hat: { length: 4600, sound: "magic", at: 1300 },
  vanish: { length: 4800, sound: "poof", at: 1500 },
  levitate: { length: 4200, sound: "magic", at: 300 },
};
const actLines = { sweep: "stageSweep", tidy: "stageTidy", hat: "stageHat", vanish: "stageVanish", levitate: "stageLevitate" };
const milestones = [[60, "waitingMinute"], [180, "waitingLong"], [300, "waitingVeryLong"]];
const repeatLong = 240;

const invitationText = "¡Te reto a una partida de ajedrez en ArchChess! Pulsa el enlace para unirte:";
const fallbackText = "Si no se abre, prueba con este otro:";

async function copyText(text, urls) {
  try {
    const html = urls.reduce(
      (markup, url) => markup.replace(escapeHtml(url), `<a href="${escapeHtml(url)}">${escapeHtml(url)}</a>`),
      escapeHtml(text),
    ).replaceAll("\n", "<br>");
    await navigator.clipboard.write([new ClipboardItem({
      "text/plain": new Blob([text], { type: "text/plain" }),
      "text/html": new Blob([html], { type: "text/html" }),
    })]);
    return true;
  } catch {}
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    const area = document.createElement("textarea");
    area.value = text;
    area.setAttribute("readonly", "");
    area.style.position = "fixed";
    area.style.opacity = "0";
    document.body.append(area);
    area.select();
    const copied = document.execCommand?.("copy") ?? false;
    area.remove();
    return copied;
  }
}

const escapeHtml = (text) => text.replace(/[&<>"]/g, (character) => `&#${character.charCodeAt(0)};`);

const clock = (seconds) => `${String(Math.floor(seconds / 60)).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`;

export class WaitingRoom {
  constructor({ audio, preferences, link, onLeave }) {
    this.audio = audio;
    this.link = link;
    this.stage = byId("waiting-stage");
    this.dialog = byId("invite-dialog");
    this.mascot = new Mascot(byId("stage-mascot"), audio, preferences, { idle: false, pokeKind: "stagePoke" });
    this.mode = null;
    this.since = null;
    this.ticker = null;
    this.actTimer = null;
    this.cueTimer = null;
    this.lastAct = null;
    this.spoken = 0;
    this.nextLong = null;
    this.revealing = false;
    this.generation = 0;
    this.linkFor = null;
    this.hiddenAt = null;

    this.dress(preferences.theme);
    this.url = null;
    this.panels = [...document.querySelectorAll(".invite-share")];
    this.native = typeof navigator.share === "function";
    for (const panel of this.panels) {
      panel.querySelector(".invite-main").textContent = this.native ? "Compartir invitación" : "Copiar invitación";
      panel.querySelector('[data-share="copy"]').hidden = !this.native;
      panel.addEventListener("click", (event) => {
        const button = event.target.closest("[data-share]");
        if (button && !button.disabled) this.share(button.dataset.share, panel);
      });
    }
    byId("invite-leave").addEventListener("click", () => onLeave());
    this.dialog.addEventListener("cancel", (event) => {
      if (this.mode === "dialog") event.preventDefault();
    });
    document.addEventListener("visibilitychange", () => this.visibility());
  }

  dress(theme) {
    if (theme) this.mascot.dress(theme);
    const actor = this.stage.querySelector(".stage-actor");
    actor.querySelector(".stage-broom").replaceChildren(pixelArt("stage-art", art.broom));
    actor.querySelector(".stage-dust").replaceChildren(pixelArt("stage-art", art.dust));
    actor.querySelector(".stage-wand").replaceChildren(pixelArt("stage-art", art.wand));
    const hat = this.stage.querySelector(".stage-hat");
    hat.querySelector(".stage-hat-pawn").replaceChildren(pieceImage("PAWN", "WHITE", theme));
    hat.querySelector("svg")?.remove();
    hat.append(pixelArt("stage-art", art.hat));
    this.stage.querySelectorAll(".stage-pawns i").forEach((slot, index) =>
      slot.replaceChildren(pieceImage("PAWN", index === 1 ? "BLACK" : "WHITE", theme)));
    this.stage.querySelector(".stage-float").replaceChildren(pieceImage("PAWN", "BLACK", theme));
    this.stage.querySelector(".stage-puff").replaceChildren(pixelArt("stage-art", art.puff));
    this.stage.querySelectorAll(".stage-sparkles i").forEach((slot) => slot.replaceChildren(pixelArt("stage-art", art.star)));
  }

  get onStage() {
    return this.mode === "stage";
  }

  sync({ waiting, visible, mascot, sessionId }) {
    if (this.revealing) return;
    if (!waiting) return this.stop();
    if (this.since === null) {
      this.since = Date.now();
      this.spoken = 0;
      this.nextLong = null;
      this.ticker = setInterval(() => this.tick(), 1000);
      this.tick();
    }
    if (this.linkFor !== sessionId) this.fill(sessionId);
    const mode = !visible ? null : mascot ? "stage" : "dialog";
    if (mode === this.mode) return;
    this.leaveMode();
    this.mode = mode;
    document.body.dataset.waiting = mode ?? "";
    if (mode === "stage") {
      this.stage.hidden = false;
      this.mascot.talk(this.spoken ? "stageWelcomeBack" : "waiting", 1);
      this.scheduleAct(7000);
    } else if (mode === "dialog") {
      if (!this.dialog.open) this.dialog.showModal();
    }
  }

  leaveMode() {
    clearTimeout(this.actTimer);
    clearTimeout(this.cueTimer);
    this.setAct("");
    if (this.mode === "stage") {
      this.stage.hidden = true;
      this.mascot.silence();
    }
    if (this.mode === "dialog" && this.dialog.open) this.dialog.close();
    this.mode = null;
    document.body.dataset.waiting = "";
  }

  stop() {
    this.generation++;
    this.revealing = false;
    this.leaveMode();
    clearInterval(this.ticker);
    this.since = null;
    this.linkFor = null;
    this.stage.classList.remove("is-closing", "is-revealed", "is-opening", "is-celebrating");
  }

  async fill(sessionId) {
    this.linkFor = sessionId;
    this.url = null;
    this.fallback = null;
    this.setPanels(false, "");
    const link = await this.link(sessionId);
    if (this.linkFor !== sessionId) return;
    this.url = link?.url ?? null;
    this.fallback = link?.fallback ?? null;
    this.setPanels(!!this.url, this.url ? "" : "No se pudo obtener la dirección de este equipo en la red.");
  }

  setPanels(ready, status) {
    for (const panel of this.panels) {
      for (const button of panel.querySelectorAll("[data-share]")) button.disabled = !ready;
      const anchor = panel.querySelector(".invite-link");
      anchor.hidden = !ready;
      anchor.textContent = ready ? this.url : "";
      if (ready) anchor.href = this.url;
      else anchor.removeAttribute("href");
      panel.querySelector(".invite-status").textContent = status;
    }
  }

  async share(target, panel) {
    if (!this.url) return;
    const backup = this.fallback ? `\n${fallbackText} ${this.fallback}` : "";
    const message = `${invitationText} ${this.url}${backup}`;
    const status = (text) => (panel.querySelector(".invite-status").textContent = text);
    if (target === "native" && this.native) {
      try {
        await navigator.share({ title: "ArchChess", text: `${invitationText}${backup}`, url: this.url });
        status("¡Invitación enviada! Ahora solo falta que tu rival la abra.");
        this.react("stageShared");
      } catch (error) {
        if (error.name !== "AbortError") return this.share("copy", panel);
      }
      return;
    }
    if (target === "native" || target === "copy") {
      const copied = await copyText(message, [this.url, this.fallback].filter(Boolean));
      status(copied
        ? "¡Invitación copiada! Pégala en el chat con tu rival."
        : "No se pudo copiar. Prueba con WhatsApp, Telegram o Correo.");
      if (copied) this.react("stageCopied");
      return;
    }
    const address = {
      whatsapp: `https://wa.me/?text=${encodeURIComponent(message)}`,
      telegram: `https://t.me/share/url?url=${encodeURIComponent(this.url)}&text=${encodeURIComponent(`${invitationText}${backup}`)}`,
      email: `mailto:?subject=${encodeURIComponent("Te reto a una partida de ArchChess")}&body=${encodeURIComponent(message)}`,
    }[target];
    if (!address) return;
    window.open(address, "_blank", "noopener");
    status("Se ha abierto la aplicación para enviar la invitación.");
    this.react("stageShared");
  }

  react(kind) {
    this.audio.play("confirm");
    if (this.onStage) this.mascot.talk(kind, 2);
  }

  tick() {
    const seconds = Math.floor((Date.now() - this.since) / 1000);
    byId("stage-timer").textContent = clock(seconds);
    byId("invite-timer").textContent = clock(seconds);
    if (!this.onStage) return;
    const due = milestones[this.spoken];
    if (due && seconds >= due[0]) {
      this.spoken++;
      if (seconds < due[0] + 5) this.mascot.talk(due[1], 1);
      if (this.spoken === milestones.length) this.nextLong = seconds + repeatLong;
    } else if (this.nextLong !== null && seconds >= this.nextLong) {
      this.nextLong = seconds + repeatLong;
      this.mascot.talk("waitingVeryLong", 1);
    }
  }

  visibility() {
    if (document.hidden) {
      this.hiddenAt = Date.now();
      return;
    }
    const away = this.hiddenAt ? Date.now() - this.hiddenAt : 0;
    this.hiddenAt = null;
    if (this.onStage && away > 5000) this.mascot.talk("stageWelcomeBack", 1);
  }

  setAct(name) {
    const root = byId("stage-mascot");
    if (root.dataset.act === name) return;
    root.dataset.act = name;
  }

  scheduleAct(delay) {
    clearTimeout(this.actTimer);
    this.actTimer = setTimeout(() => this.perform(), delay);
  }

  perform() {
    if (!this.onStage) return;
    const name = pick(Object.keys(acts).filter((act) => act !== this.lastAct));
    const { length, sound, at = 0 } = acts[name];
    this.lastAct = name;
    this.setAct("");
    void byId("stage-mascot").offsetWidth;
    this.setAct(name);
    this.mascot.talk(actLines[name]);
    clearTimeout(this.cueTimer);
    if (sound) this.cueTimer = setTimeout(() => this.onStage && this.audio.play(sound), at);
    this.actTimer = setTimeout(() => {
      this.setAct("");
      this.scheduleAct(5000 + Math.random() * 3000);
    }, length);
  }

  async reveal() {
    const generation = ++this.generation;
    const live = () => generation === this.generation;
    clearInterval(this.ticker);
    if (this.mode !== "stage") {
      this.stop();
      this.audio.play("confirm");
      return;
    }
    this.revealing = true;
    clearTimeout(this.actTimer);
    clearTimeout(this.cueTimer);
    this.setAct("");
    this.stage.classList.add("is-celebrating");
    this.audio.play("confirm");
    this.mascot.talk("opponentJoined", 3);
    await wait(2600);
    if (!live()) return;
    this.stage.classList.add("is-closing");
    await wait(700);
    if (!live()) return;
    this.mascot.silence();
    this.stage.classList.add("is-revealed");
    this.audio.play("tada");
    await wait(120);
    if (!live()) return;
    this.stage.classList.add("is-opening");
    await wait(1000);
    if (!live()) return;
    this.stop();
  }
}
