// Temas de Alfi: cuatro pistas propias en re menor, una por fase de su aguante.
// Se conocen tempo y estructura, así que los alfiles bailan sin analizar el audio.
// loopStart va en semicorcheas (como en las pistas sintetizadas); loopFrom/loopTo, en segundos del archivo.
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
          if (!response.ok) throw new Error(`HTTP ${response.status}`);
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

// Todas comparten el estilo de "Mal movimiento": 4 compases de intro y 44 en bucle
// (gancho, gancho con armonía, puente, solo, respiro burlón y vuelta final).
const theme = (name, bpm) => fileTrack({ url: `./assets/music/alfi-${name}.mp3`, bpm, introBars: 4, bars: 48, chorus: [[12, 36], [40, 48]] });

// En forma: juega tranquilo y seguro de sí mismo.
const badMove = theme("mal-movimiento", 120);
// Cansado: se lo toma en serio y acelera.
const allIn = theme("a-por-todas", 132);
// Agotado: el mismo descaro, pero arrastrando los pies.
const lastBreath = theme("ultimo-aliento", 108);
// Dormido: una nana con el reloj de fondo.
const asleep = theme("zzz", 96);

const byLevel = { fresh: badMove, tired: allIn, exhausted: lastBreath, asleep };

export const alfiTracks = [badMove, allIn, lastBreath, asleep];

export function alfiTrack(level = "fresh") {
  return byLevel[level] ?? badMove;
}
