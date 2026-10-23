import { pieceBounds } from "./piece-layouts.js";

const P = 10;
const crop = { width: 196, height: 322 };
const svgNS = "http://www.w3.org/2000/svg";

const palette = {
  k: "#15111c",
  w: "#ffffff",
  c: "#f1e9da",
  p: "#ec8f9c",
  g: "#cfe6ff",
  o: "#e8a33a",
  y: "#ffe14d",
  r: "#e0455a",
  R: "#a82a3c",
  b: "#3b6fd8",
  B: "#9cc3ff",
  m: "#5b3480",
  n: "#241a30",
  h: "#9aa3ad",
  H: "#5d6670",
  a: "#e07a2f",
  A: "#b85a1c",
  u: "#f4f0e8",
  e: "#6b3e26",
  s: "#d8d2c8",
  U: "#2f55c4",
  V: "#1b2f73",
  G: "#d9b25a",
  x: "#b3262f",
  q: "#efe2c0",
};

const at = (cx, cy, map) => {
  const rows = map.split("/");
  return { x: cx - (rows[0].length * P) / 2, y: cy - (rows.length * P) / 2, rows };
};
const from = (x, y, map) => ({ x, y, rows: map.split("/") });
const eyes = (left, right = left, y = 116) => [at(78, y, left), at(122, y, right)];
const blush = [at(62, 134, "p"), at(138, 134, "p")];

const forehead = [at(100, 80, ".k./kkk/.k.")];
const cross = { left: 76, right: 124, top: 64, bottom: 128 };
const glasses = [at(74, 118, ".kk./kggk/kggk/.kk."), at(126, 118, ".kk./kggk/kggk/.kk."), at(100, 112, "kk")];

export const moods = {
  normal: eyes("wk/kk/kk"),
  contento: eyes(".k./k.k", ".k./k.k", 112),
  sorpresa: eyes("kkk/kwk/kkk"),
  durmiendo: eyes("k.k/.k.", "k.k/.k.", 120),
  aburrido: eyes("kkk", "kkk", 118),
  guino: [at(78, 116, "wk/kk/kk"), at(122, 112, ".k./k.k")],
  enfadado: [at(78, 118, "k../kk./kkk"), at(122, 118, "..k/.kk/kkk")],
  triste: eyes("kkk/.k./.b.", "kkk/.k./.b.", 120),
  ko: eyes("k.k/.k./k.k"),
  estrellas: eyes(".o./oyo/.o."),
  corazones: [...eyes("r.r/rrr/.r."), ...blush],
  apretando: [at(78, 116, "k../.k./k.."), at(122, 116, "..k/.k./..k"), ...blush],
  sospecha: eyes("kkk/wkk", "kkk/kkw", 120),
  pensando: [at(82, 112, "kw/kk"), at(126, 112, "kw/kk")],
  leyendo: [...glasses, at(74, 124, "kk"), at(126, 124, "kk")],
  // Aguante del Alfi rival: párpados que caen y gotas de sudor; sonrisa ancha cuando se burla.
  cansado: [...eyes("kkk/.k.", "kkk/.k.", 118), at(152, 92, ".B./BBB/.b.")],
  agotado: [...eyes("kkk/.../.k.", "kkk/.../.k.", 120), at(152, 92, ".B./BBB/.b."), at(46, 100, ".B./BBB/.b.")],
  sonrisa: [...eyes(".k./k.k", ".k./k.k", 112), at(100, 146, "k.....k/.kkkkk.")],
  // Cuando va en serio: cuencas sin brillo, y el ojo izquierdo encendido.
  vacio: eyes("kkk/kkk/kkk"),
  brillo: [at(78, 116, ".B./BwB/.B."), at(122, 116, "kkk/kkk/kkk")],
};

const outfits = {
  warm: [from(50, 15, "....cc..../...cccc.../...aaaa.../..aaaaaa../.aaaaaaaa./.AAAAAAAA./aaaaaaaaaa/AaAaAaAaAa")],
  cold: [
    from(40, 25, "...bbbbbb.../..bbbbbbbb../.bbbbbbbbbb./.BBBBBBBBBB./BBBBBBBBBBBB/BB........BB/BB........BB/BB........BB/.B........B."),
    from(50, 135, "bbbbbbbbbb/bBbBbBbBbB/.bb......./.Bb......./.bb......./.BB......."),
  ],
  gothic: [from(55, -15, ".nnnnnnn./.nnnnnnn./.nnnnnnn./.nnnnnnn./.nnnnnnn./.nnnnnnn./.nnnnnnn./.mmmmmmm./.nnnnnnn./nnnnnnnnn")],
  medieval: [from(50, -15, ".....rr.../....rRr.../....rr..../...hhhh.../..hhhhhh../.hhhhhhhh./.hhHhhhhh./.hhHhhhhh./hhhhhhhhhh/hhhhhhhhhh/HHHHHHHHHH")],
  halloween: [from(30, -35, ".........mm.../........mm..../.......mmm..../......mmmm..../.....mmmmm..../.....mmmmmm.../....mmmmmmm.../....mmmmmmmm../...mmmmmmmmm../...ooooooooo../...mmmmmmmmm../mmmmmmmmmmmmmm")],
  christmas: [from(40, -5, "..........ww/........rrww/.......rrr../.....rrrrr../....Rrrrrr../...Rrrrrrr../..Rrrrrrrrr./..Rrrrrrrrr./.wwwwwwwwww./.wwwwwwwwww.")],
  python: [from(50, 25, "...bbbb...../..bbbbbb..../.bbbbbbbb.../.bbbyybbb.../.bbbyybbb.../bbbbbbbbbb../yyyyyyyyyyyy")],
  java: [from(140, 195, "ueeeeu../uuuuuuu./uoooou.u/uuuuuuu./.uuuu...")],
  warhammer: [
    from(30, -45, ".....xxxx...../...xxxxxxxx.../..xxUUUUUUxx../...UUUUUUUU.../..UUUUUUUUUU../..UUUUwUUwUU../..UUUUUwwUUU../..UUUUUUUUUU../.GGGGGGGGGGGG./.VUU......UUV./.VUU......UUV./..VU......UV.."),
    from(8, 148, ".VUUU........UUUV./VUUUUG......GUUUUV/.GGGG........GGGG."),
    from(118, 185, ".xx./xxxx/xxxx/.xx./.qq./.qq./.q../.q.."),
  ],
};
// La corona del rey de Alfi, pintada como parte de la pieza.
const crown = [from(55, -8, "y...y...y/yy.yyy.yy/yyyyyyyyy/yoyryryoy/ooooooooo")];
const hats = new Set(["warm", "cold", "gothic", "medieval", "halloween", "christmas", "python", "warhammer"]);
const extras = {
  steam: [from(150, 170, ".s.s/s.s.")],
  notes: [from(150, 40, ".kk/.k./kk./kk."), from(20, 70, ".kk/.k./kk./kk.")],
};

function paint(group, layers) {
  for (const { x, y, rows } of layers) {
    rows.forEach((row, line) => [...row].forEach((letter, column) => {
      const color = palette[letter];
      if (!color) return;
      const pixel = document.createElementNS(svgNS, "rect");
      pixel.setAttribute("x", x + column * P);
      pixel.setAttribute("y", y + line * P);
      pixel.setAttribute("width", P);
      pixel.setAttribute("height", P);
      pixel.setAttribute("fill", color);
      group.append(pixel);
    }));
  }
}

function group(className, layers = []) {
  const element = document.createElementNS(svgNS, "g");
  element.setAttribute("class", className);
  paint(element, layers);
  return element;
}

export function createFace(artwork) {
  const svg = document.createElementNS(svgNS, "svg");
  svg.setAttribute("class", "mascot-face");
  svg.setAttribute("viewBox", `0 0 ${crop.width} ${crop.height}`);
  svg.setAttribute("aria-hidden", "true");
  svg.style.width = artwork.style.width;
  svg.style.height = artwork.style.height;
  return svg;
}

export function drawFace(svg, { mood = "normal", theme = "classic", crowned = false } = {}) {
  const notes = extras.notes.map((note, index) => group(`mascot-note mascot-note-${index + 1}`, [note]));
  const outfit = group("mascot-outfit", crowned ? crown : outfits[theme] ?? []);
  if (theme === "java") outfit.append(group("mascot-steam", extras.steam));
  svg.replaceChildren(
    group("mascot-forehead", crowned || hats.has(theme) ? [] : forehead),
    group("mascot-eyes", moods[mood] ?? moods.normal),
    outfit,
    ...notes,
  );
}

export function eraseCross(image) {
  const [x, y, width, height] = pieceBounds("classic", "BISHOP", "WHITE");
  const sheet = new Image();
  sheet.src = image.src;
  image.style.visibility = "hidden";
  sheet.decode().then(() => {
    const canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    const context = canvas.getContext("2d");
    context.drawImage(sheet, -x, -y);
    const { left, right, top, bottom } = cross;
    const patch = context.getImageData(left, top, right - left + 1, bottom - top + 1);
    const { width: span, data } = patch;
    for (let row = 0; row < patch.height; row++) {
      const start = row * span * 4;
      const end = start + (span - 1) * 4;
      for (let column = 1; column < span - 1; column++) {
        const weight = Math.min(1, (Math.floor(column / P) * P + P / 2) / (span - 1));
        for (let channel = 0; channel < 4; channel++) {
          data[start + column * 4 + channel] = Math.round(data[start + channel] * (1 - weight) + data[end + channel] * weight);
        }
      }
    }
    context.putImageData(patch, left, top);
    image.addEventListener("load", () => (image.style.visibility = ""), { once: true });
    Object.assign(image.style, { left: "0", top: "0", width: "100%", height: "100%" });
    image.src = canvas.toDataURL();
  }).catch(() => (image.style.visibility = ""));
}
