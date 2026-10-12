export const defaultSoundSet = "8bit";
export const soundSets = {
  "8bit": { name: "8-bit" },
  "16bit": { name: "16-bit" },
};

export const soundFiles = {
  select: "select",
  move: "move",
  king_move: "king_move",
  capture: "capture",
  check: "check",
  victory: "win",
  defeat: "lose",
  draw: "draw",
};

export function selectedSoundSet(preferences) {
  return Object.hasOwn(soundSets, preferences.soundSet) ? preferences.soundSet : defaultSoundSet;
}

export function populateSoundSets(select) {
  for (const [id, set] of Object.entries(soundSets)) {
    const option = document.createElement("option");
    option.value = id;
    option.textContent = set.name;
    select.append(option);
  }
}
