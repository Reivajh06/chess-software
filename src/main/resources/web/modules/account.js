import { ApiError, HttpStatus } from "./api.js";

const byId = (id) => document.getElementById(id);

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

// Pantalla de crear cuenta: solo registra al jugador (POST /api/accounts con usuario y contraseña).
export class AccountScreen {
  constructor(api, { onLeave }) {
    this.api = api;
    this.busy = false;

    byId("account-form").addEventListener("submit", (event) => {
      event.preventDefault();
      this.submit();
    });
    byId("account-anonymous").addEventListener("click", () => onLeave());
  }

  open() {
    byId("account-form").reset();
    this.setMessage();
    byId("account-username").focus();
  }

  async submit() {
    if (this.busy) return;
    const username = byId("account-username").value.trim();
    const password = byId("account-password").value;
    const repeat = byId("account-repeat").value;

    const problem = usernameProblem(username) ?? passwordProblem(password, username);
    if (problem) return this.setMessage(problem.message, problem.field);
    if (password !== repeat) return this.setMessage("Las contraseñas no coinciden.", "account-repeat");

    this.setBusy(true);
    this.setMessage();
    try {
      await this.api.createAccount(username, password);
      byId("account-form").reset();
      byId("account-done").textContent = `Cuenta «${username}» creada.`;
      byId("account-done").hidden = false;
    } catch (error) {
      if (error.name === "AbortError") return;
      const message = error instanceof ApiError && error.status === HttpStatus.CONFLICT
        ? "Ese nombre de usuario ya existe."
        : error.message;
      this.setMessage(message, error.status === HttpStatus.CONFLICT ? "account-username" : null);
    } finally {
      this.setBusy(false);
    }
  }

  setBusy(busy) {
    this.busy = busy;
    const submit = byId("account-submit");
    submit.disabled = busy;
    submit.textContent = busy ? "Creando..." : "Crear cuenta";
  }

  setMessage(error = "", focusId = null) {
    byId("account-error").textContent = error;
    byId("account-error").hidden = !error;
    byId("account-done").hidden = true;
    if (focusId) byId(focusId).focus();
  }
}
