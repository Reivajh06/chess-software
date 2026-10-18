import { themes } from "./themes.js";

export function loadPreferences() {
  let saved = {};
  try {
    saved = JSON.parse(localStorage.getItem("archchess.preferences") || "{}") ?? {};
  } catch {}
  return {
    theme: saved.theme in themes ? saved.theme : saved.board in themes ? saved.board : "classic",
    volume: percentage(saved.volume, 55),
    muted: saved.muted === true,
    music: saved.music === true,
    musicVolume: percentage(saved.musicVolume, 40),
    mascot: saved.mascot !== false,
  };
}

function percentage(value, fallback) {
  return Number.isFinite(value) ? Math.max(0, Math.min(100, value)) : fallback;
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
export function forgetSession(id) {
  try {
    sessionStorage.removeItem(`archchess.session.${id}`);
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

// Where other machines can reach this server. Only needed when the page was
// opened through a loopback address, which is useless to anybody else.
export function loopbackHost(hostname = location.hostname) {
  return hostname === "localhost" || hostname.endsWith(".localhost") ||
    hostname === "[::1]" || hostname === "::1" || /^127\./.test(hostname);
}
