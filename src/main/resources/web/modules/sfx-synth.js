const note = (midi) => 440 * 2 ** ((midi - 69) / 12);

function tone(context, output, { type = "square", from, to = from, start, length, volume = 0.2, attack = 0.004 }) {
	const oscillator = context.createOscillator();
	const gain = context.createGain();
	oscillator.type = type;
	oscillator.frequency.setValueAtTime(from, start);
	if(to !== from) oscillator.frequency.exponentialRampToValueAtTime(to, start + length);
	gain.gain.setValueAtTime(0.0001, start);
	gain.gain.exponentialRampToValueAtTime(volume, start + attack);
	gain.gain.exponentialRampToValueAtTime(0.0001, start + length);
	oscillator.connect(gain).connect(output);
	oscillator.start(start);
	oscillator.stop(start + length + 0.02);
	return oscillator;
}

let noiseBuffer = null;
function noise(context, output, { start, length, volume = 0.2, frequency = 2000, to = frequency, q = 1.2 }) {
	if(!noiseBuffer || noiseBuffer.sampleRate !== context.sampleRate) {
		noiseBuffer = context.createBuffer(1, context.sampleRate * 0.5, context.sampleRate);
		const data = noiseBuffer.getChannelData(0);
		let value = 0;
		for(let index = 0; index < data.length; index++) {
			if(index % 6 === 0) value = Math.random() * 2 - 1;
			data[index] = value;
		}
	}
	const source = context.createBufferSource();
	const filter = context.createBiquadFilter();
	const gain = context.createGain();
	source.buffer = noiseBuffer;
	filter.type = "bandpass";
	filter.Q.value = q;
	filter.frequency.setValueAtTime(frequency, start);
	if(to !== frequency) filter.frequency.exponentialRampToValueAtTime(to, start + length);
	gain.gain.setValueAtTime(volume, start);
	gain.gain.exponentialRampToValueAtTime(0.0001, start + length);
	source.connect(filter).connect(gain).connect(output);
	source.start(start);
	source.stop(start + length + 0.02);
	return source;
}

const vary = (value, amount = 0.04) => value * (1 + (Math.random() * 2 - 1) * amount);

function pieceTap(context, output, start, pitch) {
	const from = vary(pitch, 0.06);
	return [
		tone(context, output, { type: "triangle", from, to: from * 0.35, start, length: 0.045, volume: 0.22 }),
		noise(context, output, { start, length: 0.015, volume: 0.08, frequency: 3200, q: 1.2 }),
	];
}

export const synthCues = {
	select(context, output, start) {
		return [
			tone(context, output, { from: note(84), start, length: 0.035, volume: 0.1 }),
			tone(context, output, { from: note(91), start: start + 0.035, length: 0.05, volume: 0.09 }),
		];
	},
	move(context, output, start) {
		const pitch = vary(520);
		return [
			tone(context, output, { type: "triangle", from: pitch, to: pitch * 0.34, start, length: 0.09, volume: 0.38 }),
			noise(context, output, { start, length: 0.03, volume: 0.12, frequency: 3200 }),
		];
	},
	king_move(context, output, start) {
		return [
			tone(context, output, { type: "triangle", from: note(55), start, length: 0.12, volume: 0.4 }),
			tone(context, output, { from: note(55), start, length: 0.1, volume: 0.06 }),
			tone(context, output, { type: "triangle", from: note(62), start: start + 0.11, length: 0.2, volume: 0.4 }),
			tone(context, output, { from: note(67), start: start + 0.11, length: 0.18, volume: 0.05 }),
			noise(context, output, { start, length: 0.03, volume: 0.1, frequency: 2600 }),
		];
	},
	capture(context, output, start) {
		return [
			noise(context, output, { start, length: 0.16, volume: 0.32, frequency: 2600, to: 500, q: 0.8 }),
			tone(context, output, { from: vary(880, 0.03), to: 90, start, length: 0.16, volume: 0.14 }),
			tone(context, output, { type: "triangle", from: 180, to: 60, start, length: 0.12, volume: 0.4 }),
		];
	},
	check(context, output, start) {
		return [0, 1, 2, 3].map((index) =>
			tone(context, output, {
				from: note(index % 2 ? 76 : 83),
				start: start + index * 0.075,
				length: 0.07,
				volume: 0.22,
				attack: 0.002,
			}));
	},
	promotion(context, output, start) {
		return [72, 76, 79, 84, 88].flatMap((midi, index) => [
			tone(context, output, { from: note(midi), start: start + index * 0.06, length: 0.14, volume: 0.09 }),
			tone(context, output, { type: "triangle", from: note(midi - 12), start: start + index * 0.06, length: 0.14, volume: 0.22 }),
		]);
	},
};

function jingle(context, output, start, lead, bass, beat = 0.11) {
	const voices = [];
	let time = start;
	for(const [midi, beats] of lead) {
		if(midi) voices.push(tone(context, output, { from: note(midi), start: time, length: beats * beat * 0.95, volume: 0.09 }));
		time += beats * beat;
	}
	time = start;
	for(const [midi, beats] of bass) {
		if(midi) voices.push(tone(context, output, { type: "triangle", from: note(midi), start: time, length: beats * beat * 0.9, volume: 0.26 }));
		time += beats * beat;
	}
	return voices;
}

Object.assign(synthCues, {
	hover(context, output, start) {
		return [tone(context, output, { type: "triangle", from: 1320, to: 1180, start, length: 0.03, volume: 0.05 })];
	},
	confirm(context, output, start) {
		return [
			tone(context, output, { from: note(79), start, length: 0.05, volume: 0.06 }),
			tone(context, output, { from: note(86), start: start + 0.05, length: 0.08, volume: 0.06 }),
		];
	},
	// "Piece tap": Alfi's typing voice, a chess piece set down on the board.
	talk: (context, output, start) => pieceTap(context, output, start, 520),
	victory: (context, output, start) => jingle(context, output, start,
		[[72, 1], [76, 1], [79, 1], [84, 2], [79, 1], [84, 4]],
		[[48, 3], [55, 3], [48, 4]]),
	defeat: (context, output, start) => jingle(context, output, start,
		[[71, 2], [67, 2], [64, 2], [63, 5]],
		[[43, 4], [40, 2], [39, 5]], 0.14),
	draw: (context, output, start) => jingle(context, output, start,
		[[72, 1], [74, 1], [72, 1], [null, 1], [67, 1], [72, 3]],
		[[48, 4], [43, 4]], 0.12),
});

Object.assign(synthCues, {
	alfiAppear(context, output, start) {
		return [
			tone(context, output, { type: "triangle", from: 70, to: 38, start, length: 0.5, volume: 0.5 }),
			noise(context, output, { start, length: 0.35, volume: 0.18, frequency: 400, to: 120, q: 0.7 }),
		];
	},
	alfiEye(context, output, start) {
		return [
			tone(context, output, { from: note(95), to: note(83), start, length: 0.22, volume: 0.07 }),
			tone(context, output, { type: "triangle", from: note(71), start, length: 0.3, volume: 0.16 }),
		];
	},
	scramble(context, output, start) {
		return [tone(context, output, { from: vary(1400, 0.3), start, length: 0.025, volume: 0.05, attack: 0.002 })];
	},
	shatter(context, output, start) {
		return [
			noise(context, output, { start, length: 0.3, volume: 0.3, frequency: 5200, to: 900, q: 0.6 }),
			...[0, 0.05, 0.11, 0.18].map((offset, index) =>
				tone(context, output, { from: vary(2400 - index * 300, 0.1), start: start + offset, length: 0.05, volume: 0.06, attack: 0.002 })),
		];
	},
	glitch(context, output, start) {
		return [0, 0.07, 0.16, 0.22, 0.34, 0.41].flatMap((offset, index) => [
			noise(context, output, { start: start + offset, length: 0.05, volume: 0.16, frequency: index % 2 ? 900 : 3800, q: 2 }),
			tone(context, output, { from: vary(index % 2 ? 110 : 1760, 0.2), start: start + offset, length: 0.04, volume: 0.06, attack: 0.002 }),
		]);
	},
});

Object.assign(synthCues, {
	alfiLaugh(context, output, start) {
		return [0, 0.13, 0.26].flatMap((offset, index) => [
			tone(context, output, { from: note(55 - index * 2) * 1.08, to: note(55 - index * 2), start: start + offset, length: 0.09, volume: 0.14 }),
			tone(context, output, { type: "triangle", from: note(43 - index * 2), start: start + offset, length: 0.08, volume: 0.22 }),
		]);
	},
});

Object.assign(synthCues, {
	// Boss Alfi taps the same piece, a few tones lower.
	alfiTalk: (context, output, start) => pieceTap(context, output, start, 360),
});
