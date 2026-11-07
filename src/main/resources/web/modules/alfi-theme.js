function fileTrack({ url, bpm, introBars, bars, chorus }) {
	const bar = (4 * 60) / bpm;
	return {
		kind: "file",
		url,
		bpm,
		offset: 0,
		loopStart: introBars * 16,
		loopFrom: introBars * bar,
		loopTo: bars * bar,
		buffer: null,
		loading: null,
		failed: false,

		isChorus(step) {
			const at = step / 16;
			return chorus.some(([from, to]) => at >= from && at < to);
		},

		load(context) {
			this.loading ??= fetch(this.url)
				.then((response) => {
					if(!response.ok) throw new Error(`HTTP ${response.status}`);
					return response.arrayBuffer();
				})
				.then((data) => context.decodeAudioData(data))
				.then((buffer) => {
					this.buffer = buffer;
					return this;
				})
				.catch(() => {
					this.failed = true;
					return this;
				});
			return this.loading;
		},
	};
}

const theme = (name, bpm, chorus = [[12, 36], [40, 48]]) => fileTrack({ url: `./assets/music/alfi-${name}.mp3`, bpm, introBars: 4, bars: 48, chorus });

const badMove = theme("mal-movimiento", 120);
const allIn = theme("a-por-todas", 132);
const lastBreath = theme("ultimo-aliento", 108);
const asleep = theme("zzz", 96);
const checkDance = theme("jaque-y-baile", 144, [[12, 20], [28, 36], [40, 48]]);

const byLevel = { fresh: badMove, tired: allIn, exhausted: lastBreath, asleep };

export const alfiTracks = [badMove, allIn, lastBreath, asleep, checkDance];

export function alfiTrack(level = "fresh") {
	return byLevel[level] ?? badMove;
}
