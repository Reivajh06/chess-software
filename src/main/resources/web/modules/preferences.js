import { themes } from "./themes.js";
import { selectedSoundSet } from "./soundsets.js";

export function loadPreferences() {
  let saved = {};
  try {
    saved = JSON.parse(localStorage.getItem("archchess.preferences") || "{}") ?? {};
  } catch {}
  return {
    theme: saved.theme in themes ? saved.theme : saved.board in themes ? saved.board : "classic",
    view: ["both", "history", "chat", "info", "analysis"].includes(saved.view)
      ? saved.view
      : "history",
    volume: Number.isFinite(saved.volume) ? Math.max(0, Math.min(100, saved.volume)) : 55,
    muted: saved.muted === true,
    soundSet: selectedSoundSet(saved),
  };
}
export function savePreferences(preferences) {
  try {
    localStorage.setItem("archchess.preferences", JSON.stringify(preferences));
  } catch {}
}
export function saveSession(session) {
  try {
    sessionStorage.setItem(`archchess.session.${session.sessionId}`, JSON.stringify(session));
  } catch {}
}
export function readSession(id) {
  try {
    const session = JSON.parse(sessionStorage.getItem(`archchess.session.${id}`) || "null");
    return session?.sessionId === id ? session : null;
  } catch {
    return null;
  }
}
export function parseInvitation(value) {
  const text = value.trim();
  if (!text) return null;
  if (text.includes("://")) {
    try {
      return new URL(text).searchParams.get("session");
    } catch {
      return null;
    }
  }
  return /^[a-zA-Z0-9_-]{1,128}$/.test(text) ? text : null;
}

export function invitationPlayer(value) {
  try {
    const url = new URL(value);
    const fragment = new URLSearchParams(url.hash.slice(1));
    const playerId = fragment.get("playerId");
    const playerColor = fragment.get("side");
    return { playerId, playerColor: playerColor === "BLACK" ? "BLACK" : "WHITE" };
  } catch {
    return { playerId: null, playerColor: "WHITE" };
  }
}
