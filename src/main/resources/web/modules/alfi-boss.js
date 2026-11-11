import { byId, wait, element } from "./ui.js";
import { Mascot } from "./mascot.js";
import { pieceImage, setPieceSkin } from "./assets.js";
import { createFace, drawFace, eraseCross } from "./alfi-face.js";
import { alfiTrack, alfiTracks } from "./alfi-theme.js";
import { calmTrack } from "./music.js";
import { setColorSwap } from "./colors.js";
import { line } from "./mascot.js";

const rules = { theme: "classic", mascot: true, muted: false, music: true };
const minimumVolume = 20;

function alfiKing() {
	const body = pieceImage("BISHOP", "WHITE", "classic");
	const face = createFace(body.querySelector(".piece-art"));
	eraseCross(body.querySelector("img"));
	drawFace(face, { crowned: true });
	body.classList.add("alfi-king");
	body.append(face);
	return body;
}
const glyphs = "#%&@$?!<>/\\|=+*01";

export class AlfiBoss {
	constructor(audio, music, preferences) {
		this.audio = audio;
		this.music = music;
		this.preferences = preferences;
		this.lastProtest = 0;
		this.track = alfiTrack();
		this.side = "BLACK";
		const always = { mascot: true, get theme() { return preferences.theme; } };
		this.intro = new Mascot(byId("alfi-intro"), audio, always, { idle: false, voice: "alfiTalk" });
		this.seat = new Mascot(byId("alfi-seat"), audio, always, {
			idle: false,
			voice: "alfiTalk",
			pokes: { poke: "bossPoke", annoyed: "bossPoked", wake: "bossAsleep" },
		});
		this.stage = byId("alfi-stage");
		this.crowds = [...document.querySelectorAll(".alfi-dancers")];
		for(const crowd of this.crowds) {
			const first = crowd.dataset.first ?? "BLACK";
			const other = first === "BLACK" ? "WHITE" : "BLACK";
			crowd.replaceChildren(...Array.from({ length: Number(crowd.dataset.count) }, (_, index) => {
				const dancer = pieceImage("BISHOP", index % 2 ? other : first, "classic");
				dancer.classList.add("alfi-dancer");
				return dancer;
			}));
		}
	}

	get active() {
		return document.body.dataset.alfi === "on";
	}

	apply(on) {
		this.enforce(on);
		this.rebrand(on);
		this.dress(this.preferences.theme);
		this.onRulesChanged?.();
		if(!on) for(const crowd of this.crowds) delete crowd.dataset.pose;
		document.body.dataset.alfi = on ? "on" : "off";
		byId("alfi-seat").hidden = !on;
		if(!on) this.seat.silence();
		for(const element of document.querySelectorAll("[data-alfi-breaks]")) element.classList.toggle("alfi-broken", on);
		if(on && this.audio.context) for(const track of alfiTracks) track.load(this.audio.context);
		this.music.play(on ? this.track : calmTrack);
	}

	followGame(state, waiting = false) {
		// Keep the full soundtrack without stamina: tension follows the board and pauses.
		const phase = waiting ? "asleep" : state.checkedColor ? "check"
			: state.pieces.size <= 10 ? "exhausted" : state.moves.length >= 20 ? "tired" : "fresh";
		this.track = alfiTrack(phase);
		if(this.active && !this.music.silenced) this.music.play(this.track);
	}

	enforce(on) {
		const preferences = this.preferences;
		if(on && !preferences.held) {
			preferences.held = Object.fromEntries(Object.keys(rules).map((key) => [key, preferences[key]]));
			Object.assign(preferences, rules);
			preferences.volume = Math.max(preferences.volume, minimumVolume);
			preferences.musicVolume = Math.max(preferences.musicVolume, minimumVolume);
		} else if(!on && preferences.held) {
			Object.assign(preferences, preferences.held);
			delete preferences.held;
		}
	}

	beat({ step, track }) {
		if(!this.active) return;
		const beat = step / 4;
		const intro = step < track.loopStart;
		const chorus = track.isChorus?.(step) ?? false;
		const pose = intro
			? (beat % 2 ? "dip" : "rest")
			: chorus
				? ["left", "hop", "right", "spin"][beat % 4]
				: (beat % 2 ? "left" : "right");
		for(const crowd of this.crowds) crowd.dataset.pose = pose;
	}

	resist(key, value) {
		if(!this.active) return false;
		let kind = null;
		if(key in rules && value !== rules[key]) kind = { theme: "bossTheme", mascot: "bossHide", muted: "bossMute", music: "bossMute" }[key];
		else if((key === "volume" || key === "musicVolume") && value < minimumVolume) kind = "bossVolume";
		if(!kind) return false;
		if(key in rules) this.preferences[key] = rules[key];
		else this.preferences[key] = minimumVolume;
		const now = performance.now();
		if(now - this.lastProtest > 1500) {
			this.lastProtest = now;
			this.audio.play("alfiLaugh");
			this.seat.talk(kind, 4, { mood: kind === "bossVolume" ? "sonrisa" : "vacio" });
		}
		return true;
	}

	crown() {
		setPieceSkin(this.side, "KING", alfiKing);
	}

	rebrand(on = true) {
		setPieceSkin("WHITE", "KING", null);
		setPieceSkin("BLACK", "KING", null);
		if(on) this.crown();
		setColorSwap(on && this.side === "BLACK");
	}

	cheat(kind) {
		this.audio.play("alfiEye");
		const frame = document.querySelector(".board-frame");
		frame.classList.remove("alfi-cheat");
		void frame.offsetWidth;
		frame.classList.add("alfi-cheat");
		const kinds = { double: "bossDouble", dodge: "bossDodge", rewind: "bossRewind", flip: "bossFlip" };
		this.seat.speak(line(kinds[kind]), { mood: "brillo" });
	}

	dress(theme) {
		this.intro.dress(theme);
		this.seat.dress(theme);
	}

	abort() {
		this.aborted = true;
		this.stage.hidden = true;
		document.body.classList.remove("alfi-glitch", "alfi-hush");
		this.intro.silence();
	}

	async ending(outcome, alive = () => true) {
		const scenes = {
			win: [
				["...well. there goes my quiet afternoon.", "agotado"],
				["you won. even with all my little adjustments.", "agotado"],
				["i bent the rules. you kept finding the gaps.", "cansado"],
				["that's good play. don't make me say it twice.", "normal"],
				["enjoy the win. i'll be reviewing the fine print.", "guino"],
			],
			loss: [
				["hm. quiet board all of a sudden.", "sonrisa"],
				["your king ran out of safe squares. awkward place to end a plan.", "sonrisa"],
				["you can try again. i do admire an unfinished argument.", "vacio"],
				["the board will remember where we left off.", "brillo"],
			],
			draw: [
				["a draw. nobody wins. clever way to deny me the satisfaction.", "aburrido"],
				["i'll call it mercy. you can file a different report.", "guino"],
			],
		};
		this.music.cut();
		this.seat.silence();
		this.stage.classList.remove("is-leaving", "is-watching");
		this.stage.classList.toggle("is-dark", outcome === "loss");
		this.intro.bubble.hidden = true;
		this.intro.setMood(scenes[outcome][0][1]);
		this.stage.hidden = false;
		this.audio.play("alfiAppear");
		await wait(900);
		for(const [text, mood] of scenes[outcome]) {
			if(!alive()) break;
			await this.intro.speak(text, { mood });
			await wait(500);
		}
		if(outcome === "loss" && alive()) {
			this.audio.play("alfiLaugh");
			await wait(500);
			this.audio.play("alfiLaugh");
			await wait(700);
		}
		this.intro.silence();
		this.stage.hidden = true;
		this.stage.classList.remove("is-dark");
	}

	async reveal({ cheat, alive, onRevealed, swapColors, crownKing, encounters = 0 }) {
		this.aborted = false;
		const live = () => !this.aborted && alive();

		await wait(1300);
		if(!live()) return false;
		this.music.cut();
		this.enforce(true);
		this.dress(this.preferences.theme);
		this.onRulesChanged?.();
		document.body.classList.add("alfi-hush");
		await cheat();
		if(!live()) return false;
		await wait(1100);
		if(!live()) return false;

		this.intro.setMood("vacio");
		this.intro.bubble.hidden = true;
		this.stage.hidden = false;
		this.stage.classList.remove("is-leaving", "is-watching");
		this.audio.play("alfiAppear");
		await wait(900);
		if(!live()) return false;

		const opening = encounters === 0
			? [
				["right. let's adjust a few expectations.", "guino"],
				["oh, your turn? how inconvenient for the schedule.", "vacio"],
				["computer needed a break. i offered. very public-spirited of me.", "contento"],
			]
			: [
				["well. you know where the pieces go.", "guino"],
				[line(encounters >= 2 ? "bossAgainMany" : "bossAgain", { veces: String(encounters + 1) }), "sonrisa"],
			];
		for(const [text, mood] of opening) {
			await this.intro.speak(text, { mood });
			if(!live()) return false;
			await wait(450);
			if(!live()) return false;
		}

		onRevealed?.();
		const scramble = this.scramble(byId("opponent-name"), "Alfi");
		for(const element of document.querySelectorAll("[data-alfi-breaks]")) this.shatter(element);
		this.audio.play("alfiEye");
		await this.intro.speak("you get me today. quite the upgrade in problems.", { mood: "brillo" });
		await scramble;
		if(!live()) return false;
		await wait(500);
		if(!live()) return false;

		this.intro.bubble.hidden = true;
		this.stage.classList.add("is-watching");
		this.intro.setMood("brillo");
		await wait(350);
		await swapColors?.(() => this.audio.play("scramble"));
		if(!live()) return false;
		await wait(300);

		await this.intro.speak("one little detail. hardly worth reading the rules for.", { mood: "sorpresa" });
		if(!live()) return false;
		await crownKing?.();
		if(!live()) return false;
		await this.intro.speak("i handle the rules. you handle the consequences.", { mood: "sonrisa" });
		if(!live()) return false;
		await wait(600);
		if(!live()) return false;

		this.intro.silence();
		this.intro.bubble.hidden = true;
		document.body.classList.add("alfi-glitch");
		this.audio.play("glitch");
		document.body.classList.remove("alfi-hush");
		document.body.dataset.alfi = "on";
		byId("alfi-seat").hidden = false;
		await this.fly();
		this.stage.hidden = true;
		document.body.classList.remove("alfi-glitch");
		if(!live()) return false;
		this.apply(true);
		this.seat.setMood("brillo");
		await wait(400);
		this.seat.say("think you've got a winning move? good. i'd hate for this to be dull.", 3, { mood: "brillo" });
		return true;
	}

	async scramble(element, target) {
		const source = element.textContent;
		const frames = 18;
		const glyph = () => glyphs[Math.floor(Math.random() * glyphs.length)];
		for(let frame = 1; frame < frames; frame++) {
			const length = Math.round(source.length + ((target.length - source.length) * frame) / frames);
			const fixed = Math.floor((target.length * frame) / frames);
			element.textContent = target.slice(0, fixed) + Array.from({ length: Math.max(0, length - fixed) }, glyph).join("");
			if(frame % 2) this.audio.play("scramble");
			await wait(55);
		}
		element.textContent = target;
	}

	shatter(element) {
		const rect = element.getBoundingClientRect();
		if(!rect.width || !rect.height) return;
		this.audio.play("shatter");
		const strips = 5;
		const rubble = element("div", "alfi-rubble");
		Object.assign(rubble.style, { left: `${rect.left}px`, top: `${rect.top}px`, width: `${rect.width}px`, height: `${rect.height}px` });
		for(let index = 0; index < strips; index++) {
			const shard = element.cloneNode(true);
			shard.removeAttribute("id");
			shard.setAttribute("aria-hidden", "true");
			shard.classList.add("alfi-shard");
			const left = (index / strips) * 100;
			const right = 100 - ((index + 1) / strips) * 100;
			shard.style.clipPath = `inset(0 ${right}% 0 ${left}% )`;
			shard.style.setProperty("--dx", `${(index - 2) * 14}px`);
			shard.style.setProperty("--spin", `${(index % 2 ? 1 : -1) * (8 + index * 5)}deg`);
			shard.style.animationDelay = `${index * 40}ms`;
			rubble.append(shard);
		}
		document.body.append(rubble);
		element.classList.add("alfi-broken");
		setTimeout(() => rubble.remove(), 1400);
	}

	async fly() {
		const from = this.intro.sprite.getBoundingClientRect();
		const to = this.seat.sprite.getBoundingClientRect();
		this.stage.classList.add("is-leaving");
		if(!from.width || !to.width) return wait(500);
		const dx = to.left + to.width / 2 - (from.left + from.width / 2);
		const dy = to.top + to.height / 2 - (from.top + from.height / 2);
		const animation = this.intro.sprite.animate(
			[{ transform: "none" }, { transform: `translate(${dx}px, ${dy}px) scale(${to.width / from.width})` }],
			{ duration: 650, easing: "steps(7)", fill: "forwards" },
		);
		await animation.finished.catch(() => {});
		animation.cancel();
	}
}
