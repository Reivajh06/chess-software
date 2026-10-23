// En el modo Alfi las piezas se ven con los colores cambiados: las suyas blancas (como él) y las del
// jugador negras. La partida no cambia; solo cómo se pintan y cómo se nombran los bandos.
let swapped = false;

export function setColorSwap(on) {
  swapped = on;
}

export function colorSwapped() {
  return swapped;
}

export function shownColor(color) {
  if (!swapped || !color) return color;
  return color === "WHITE" ? "BLACK" : "WHITE";
}
