// File-name allowlist only: /api/plugins does not expose plugin types. Order is fixed,
// strongest first; unknown plugins must never become Alfi's engine by accident.
export const enginePriority = Object.freeze([
	"stockfish", "berserk", "archon", "ethereal", "komodo", "dragon", "lc0",
	"houdini", "rubichess", "rofchade", "koivisto", "seer", "viridithas",
	"caissa", "arasan", "crafty", "fruit",
]);

const llm = /ollama|llama|qwen|gemma|gpt|chatgpt|claude|deepseek|mistral|phi|llm|openai|anthropic|gemini/i;

export function engineRank(filename) {
	if(typeof filename !== "string" || /[/\\]/.test(filename) || llm.test(filename)) return -1;
	const name = filename.replace(/\.jar$/i, "").toLowerCase();
	return enginePriority.findIndex((engine) =>
		new RegExp(`(^|[^a-z])${engine}(?=$|[^a-z]|plugin)`).test(name));
}

export function strongestEngine(plugins = []) {
	return plugins.filter((name) => engineRank(name) >= 0)
		.sort((a, b) => engineRank(a) - engineRank(b) ||
			a.toLowerCase().localeCompare(b.toLowerCase(), "en") || a.localeCompare(b, "en"))[0] ?? null;
}
