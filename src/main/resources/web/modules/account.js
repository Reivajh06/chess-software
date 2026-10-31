import { ApiError, HttpStatus } from "./api.js";

const byId = (id) => document.getElementById(id);
const accountKey = "archchess.account";

// Cuenta con la que este equipo ha iniciado sesión (la sesión real la guarda el servidor).
export function loadAccount() {
  try {
    const saved = JSON.parse(localStorage.getItem(accountKey) || "null");
    return typeof saved?.username === "string" && saved.username ? saved : null;
  } catch {
    return null;
  }
}

export function saveAccount(account) {
  try {
    if (account) localStorage.setItem(accountKey, JSON.stringify(account));
    else localStorage.removeItem(accountKey);
  } catch {}
}

// Reglas de la cuenta. Son las mismas que debería aplicar el backend; aquí solo avisan antes de enviar.
export const accountRules = Object.freeze({
  usernameMin: 3,
  usernameMax: 20,
  usernamePattern: /^[A-Za-z0-9_-]+$/,
  passwordMin: 8,
  passwordMax: 64,
});

export function usernameProblem(username) {
  const field = "account-username";
  if (!username) return { field, message: "Escribe un nombre de usuario." };
  if (username.length < accountRules.usernameMin || username.length > accountRules.usernameMax)
    return { field, message: `El usuario debe tener entre ${accountRules.usernameMin} y ${accountRules.usernameMax} caracteres.` };
  if (!accountRules.usernamePattern.test(username))
    return { field, message: "El usuario solo puede tener letras sin tilde, números, «_» o «-»." };
  return null;
}

export function passwordProblem(password, username = "") {
  const field = "account-password";
  if (!password) return { field, message: "Escribe una contraseña." };
  if (password.length < accountRules.passwordMin)
    return { field, message: `La contraseña debe tener al menos ${accountRules.passwordMin} caracteres.` };
  if (password.length > accountRules.passwordMax)
    return { field, message: `La contraseña no puede pasar de ${accountRules.passwordMax} caracteres.` };
  if (/\s/.test(password)) return { field, message: "La contraseña no puede tener espacios." };
  if (!/\p{L}/u.test(password) || !/\d/.test(password))
    return { field, message: "La contraseña necesita al menos una letra y un número." };
  if (username && password.toLowerCase().includes(username.toLowerCase()))
    return { field, message: "La contraseña no puede contener tu nombre de usuario." };
  return null;
}

const modes = {
  login: { label: "Entrar", busy: "Entrando...", password: "current-password" },
  signup: { label: "Crear cuenta", busy: "Creando...", password: "new-password" },
};

// Pantalla de cuenta: iniciar sesión (POST /api/login) o crear cuenta (POST /api/accounts).
// En los dos casos el servidor deja la sesión iniciada; aquí solo se recuerda el usuario en este equipo.
export class AccountScreen {
  constructor(api, { onLeave, onLoggedIn }) {
    this.api = api;
    this.onLoggedIn = onLoggedIn;
    this.mode = "login";
    this.busy = false;

    byId("account-form").addEventListener("submit", (event) => {
      event.preventDefault();
      this.submit();
    });
    byId("account-tab-login").addEventListener("click", () => this.setMode("login"));
    byId("account-tab-signup").addEventListener("click", () => this.setMode("signup"));
    byId("account-anonymous").addEventListener("click", () => onLeave());
  }

  open(mode = "login") {
    byId("account-form").reset();
    this.setMode(mode);
  }

  setMode(mode) {
    if (this.busy) return;
    this.mode = mode;
    byId("account-form").dataset.mode = mode;
    byId("account-tab-login").setAttribute("aria-selected", String(mode === "login"));
    byId("account-tab-signup").setAttribute("aria-selected", String(mode === "signup"));
    byId("account-password").autocomplete = modes[mode].password;
    byId("account-submit").textContent = modes[mode].label;
    this.setMessage("", "account-username");
  }

  async submit() {
    if (this.busy) return;
    const username = byId("account-username").value.trim();
    const password = byId("account-password").value;

    if (this.mode === "signup") {
      const problem = usernameProblem(username) ?? passwordProblem(password, username);
      if (problem) return this.setMessage(problem.message, problem.field);
      if (password !== byId("account-repeat").value)
        return this.setMessage("Las contraseñas no coinciden.", "account-repeat");
    } else {
      if (!username) return this.setMessage("Escribe tu nombre de usuario.", "account-username");
      if (!password) return this.setMessage("Escribe tu contraseña.", "account-password");
    }

    this.setBusy(true);
    this.setMessage();
    try {
      if (this.mode === "signup") await this.api.createAccount(username, password);
      else await this.api.login(username, password);
      byId("account-form").reset();
      saveAccount({ username });
      this.onLoggedIn({ username });
    } catch (error) {
      if (error.name === "AbortError") return;
      this.showError(error);
    } finally {
      this.setBusy(false);
    }
  }

  showError(error) {
    const status = error instanceof ApiError ? error.status : 0;
    if (this.mode === "signup" && status === HttpStatus.CONFLICT)
      return this.setMessage("Ese nombre de usuario ya existe.", "account-username");
    if (this.mode === "login" && [HttpStatus.UNAUTHORIZED, HttpStatus.NOT_FOUND].includes(status))
      return this.setMessage("Usuario o contraseña incorrectos.", "account-password");
    this.setMessage(error.message);
  }

  setBusy(busy) {
    this.busy = busy;
    const submit = byId("account-submit");
    submit.disabled = busy;
    submit.textContent = busy ? modes[this.mode].busy : modes[this.mode].label;
  }

  setMessage(error = "", focusId = null) {
    byId("account-error").textContent = error;
    byId("account-error").hidden = !error;
    if (focusId) byId(focusId).focus();
  }
}
