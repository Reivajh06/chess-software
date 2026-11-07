const frame = 1024;
const hop = 512;
const latency = 0.75 * frame;

function onsetEnvelope(samples, sampleRate) {
	const count = Math.max(0, Math.floor((samples.length - frame) / hop));
	const energy = new Float32Array(count);
	for(let block = 0; block < count; block++) {
		let sum = 0;
		const start = block * hop;
		for(let index = start + 1; index < start + frame; index++) {
			const edge = samples[index] - samples[index - 1];
			sum += edge * edge;
		}
		energy[block] = Math.log(1e-6 + sum);
	}
	const onsets = new Float32Array(count);
	for(let block = 1; block < count; block++) onsets[block] = Math.max(0, energy[block] - energy[block - 1]);
	return { onsets, rate: sampleRate / hop };
}

function detectTempo(onsets, rate, { min = 70, max = 190 } = {}) {
	const mean = onsets.reduce((sum, value) => sum + value, 0) / (onsets.length || 1);
	const centered = onsets.map((value) => value - mean);
	const score = (lag) => {
		let sum = 0;
		for(let index = lag; index < centered.length; index++) sum += centered[index] * centered[index - lag];
		return sum / (centered.length - lag);
	};
	const shortest = Math.floor((rate * 60) / max);
	const longest = Math.ceil((rate * 60) / min);
	let best = shortest;
	let bestScore = -Infinity;
	const scores = new Map();
	for(let lag = shortest; lag <= longest; lag++) {
		const raw = score(lag);
		scores.set(lag, raw);
		const bpm = (rate * 60) / lag;
		const weighted = raw * Math.exp(-0.5 * (Math.log2(bpm / 120) / 0.9) ** 2);
		if(weighted > bestScore) {
			bestScore = weighted;
			best = lag;
		}
	}
	const [left, middle, right] = [scores.get(best - 1), scores.get(best), scores.get(best + 1)];
	let lag = best;
	if(left !== undefined && right !== undefined) {
		const curve = left - 2 * middle + right;
		if(curve < 0) lag += (0.5 * (left - right)) / curve;
	}
	return (rate * 60) / lag;
}

function alignGrid(onsets, rate, bpm) {
	const period = (rate * 60) / bpm;
	let best = 0;
	let bestScore = -Infinity;
	for(let start = 0; start < period; start += 0.5) {
		let sum = 0;
		for(let position = start; position < onsets.length; position += period) sum += onsets[Math.round(position)] ?? 0;
		if(sum > bestScore) {
			bestScore = sum;
			best = start;
		}
	}
	return { start: best, score: bestScore };
}

function refineGrid(onsets, rate, bpm) {
	let best = { bpm, ...alignGrid(onsets, rate, bpm) };
	for(let candidate = bpm * 0.985; candidate <= bpm * 1.015; candidate += bpm * 0.0005) {
		const grid = alignGrid(onsets, rate, candidate);
		if(grid.score > best.score) best = { bpm: candidate, ...grid };
	}
	const period = 60 / best.bpm;
	return { bpm: best.bpm, offset: (best.start / rate + latency / (rate * hop)) % period };
}

function intenseBeats(samples, sampleRate, bpm, offset) {
	const length = Math.round((sampleRate * 60) / bpm);
	const start = Math.round(offset * sampleRate);
	const levels = [];
	for(let from = start; from + length <= samples.length; from += length) {
		let sum = 0;
		for(let index = from; index < from + length; index += 4) sum += samples[index] * samples[index];
		levels.push(Math.sqrt(sum / (length / 4)));
	}
	const median = [...levels].sort((a, b) => a - b)[Math.floor(levels.length / 2)] ?? 0;
	return levels.map((level) => level > median * 1.12);
}

export function analyzeBeat(samples, sampleRate) {
	const { onsets, rate } = onsetEnvelope(samples, sampleRate);
	const { bpm, offset } = refineGrid(onsets, rate, detectTempo(onsets, rate));
	return { bpm, offset, intense: intenseBeats(samples, sampleRate, bpm, offset) };
}

function mixToMono(buffer) {
	const mono = new Float32Array(buffer.length);
	for(let channel = 0; channel < buffer.numberOfChannels; channel++) {
		const data = buffer.getChannelData(channel);
		for(let index = 0; index < data.length; index++) mono[index] += data[index] / buffer.numberOfChannels;
	}
	return mono;
}
