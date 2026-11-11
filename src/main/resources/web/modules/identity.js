// Identity colours: the colour that tells one participant of a game from another (board cards, thinking slabs,
// account chip). Plugins and guests get a random colour for each game; a registered player gets a random colour
// when the account is created and keeps it from then on.
const neutral = "#8f9bb3";
export const palette = ["#62c7e8", "#e8a95e", "#b68cf0", "#6fd68a", "#ec7f8e", "#e2d45f", "#e58fd0", "#8fa6ff"];
const accountTonesKey = "archchess.account-tones";

let game = new Map(); // identity -> colour, for the current game

function pick(taken) {
	const free = palette.filter((tone) => !taken.has(tone));
	const pool = free.length ? free : palette;
	return pool[Math.floor(Math.random() * pool.length)];
}

/** Forgets the colours of the previous game: the next one draws new ones. */
export function newGame() {
	game = new Map();
}

/** The colour of a participant in this game; random the first time it is asked, and different from the others. */
export function toneFor(identity) {
	if(!identity) return neutral;
	if(!game.has(identity)) game.set(identity, pick(new Set(game.values())));
	return game.get(identity);
}

/** Fixes the colour of a participant (a registered player) for this game. */
export function reserveTone(identity, tone) {
	if(identity && tone) game.set(identity, tone);
}

export const pluginIdentity = (name) => (name ? `plugin:${String(name).replace(/\.jar$/i, "").trim().toLowerCase()}` : null);
export const accountIdentity = (username) => (username ? `account:${username}` : null);

function savedTones() {
	try {
		const saved = JSON.parse(localStorage.getItem(accountTonesKey) || "{}");
		return saved && typeof saved === "object" && !Array.isArray(saved) ? saved : {};
	} catch {
		return {};
	}
}

/**
 * The colour of a registered player: drawn at random the first time the account is seen (when it is created)
 * and the same ever after in this browser, also after logging out and in.
 */
export function accountTone(username) {
	const saved = savedTones();
	if(palette.includes(saved[username])) return saved[username];
	const tone = pick(new Set());
	try {
		localStorage.setItem(accountTonesKey, JSON.stringify({ ...saved, [username]: tone }));
	} catch {}
	return tone;
}
