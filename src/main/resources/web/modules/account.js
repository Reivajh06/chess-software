import { byId, dialog } from "./ui.js";
import { ApiError, HttpStatus } from "./api.js";
import { accountTone } from "./identity.js";

const accountKey = "archchess.account";

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
		if(account) localStorage.setItem(accountKey, JSON.stringify(account));
		else localStorage.removeItem(accountKey);
	} catch {}
}

/**
 * The login lives in the server's session, which a restart or an expiry forgets, while the account saved in this
 * browser stays. Asks the server (the history is only served to registered players) before trusting it.
 * @returns false when the server no longer knows the account, which is then forgotten; true otherwise
 *          (also when the server cannot be reached: then there is no way to tell).
 */
export async function verifySavedAccount(api) {
	const saved = loadAccount();
	if(!saved) return false;
	try {
		await api.history();
		return true;
	} catch (error) {
		if(error?.status !== HttpStatus.FORBIDDEN) return true;
		if(loadAccount()?.username === saved.username) saveAccount(null);
		return false;
	}
}

const accountRules = Object.freeze({
	usernameMin: 3,
	usernameMax: 20,
	usernamePattern: /^[A-Za-z0-9_-]+$/,
	passwordMin: 8,
	passwordMax: 64,
});

function usernameProblem(username) {
	const field = "account-username";
	if(!username) return { field, message: "Enter a username." };
	if(username.length < accountRules.usernameMin || username.length > accountRules.usernameMax)
		return { field, message: `Your username must be ${accountRules.usernameMin}–${accountRules.usernameMax} characters long.` };
	if(!accountRules.usernamePattern.test(username))
		return { field, message: "Your username can only contain unaccented letters, numbers, “_” or “-”." };
	return null;
}

function passwordProblem(password, username = "") {
	const field = "account-password";
	if(!password) return { field, message: "Enter a password." };
	if(password.length < accountRules.passwordMin)
		return { field, message: `Your password must be at least ${accountRules.passwordMin} characters long.` };
	if(password.length > accountRules.passwordMax)
		return { field, message: `Your password cannot exceed ${accountRules.passwordMax} characters.` };
	if(/\s/.test(password)) return { field, message: "Your password cannot contain spaces." };
	if(!/\p{L}/u.test(password) || !/\d/.test(password))
		return { field, message: "Your password needs at least one letter and one number." };
	if(username && password.toLowerCase().includes(username.toLowerCase()))
		return { field, message: "Your password cannot contain your username." };
	return null;
}

const modes = {
	login: { label: "Sign in", busy: "Signing in...", password: "current-password" },
	signup: { label: "Create account", busy: "Creating...", password: "new-password" },
};

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
		if(this.busy) return;
		this.mode = mode;
		byId("account-form").dataset.mode = mode;
		byId("account-tab-login").setAttribute("aria-selected", String(mode === "login"));
		byId("account-tab-signup").setAttribute("aria-selected", String(mode === "signup"));
		byId("account-password").autocomplete = modes[mode].password;
		byId("account-submit").textContent = modes[mode].label;
		this.setMessage("", "account-username");
	}

	async submit() {
		if(this.busy) return;
		const username = byId("account-username").value.trim();
		const password = byId("account-password").value;

		if(this.mode === "signup") {
			const problem = usernameProblem(username) ?? passwordProblem(password, username);
			if(problem) return this.setMessage(problem.message, problem.field);
			if(password !== byId("account-repeat").value)
				return this.setMessage("Passwords do not match.", "account-repeat");
		} else {
			if(!username) return this.setMessage("Enter your username.", "account-username");
			if(!password) return this.setMessage("Enter your password.", "account-password");
		}

		this.setBusy(true);
		this.setMessage();
		try {
			if(this.mode === "signup") await this.api.createAccount(username, password);
			else await this.api.login(username, password);
			byId("account-form").reset();
			// The colour is drawn when the account is created (sign-up) and is the same every time after.
			saveAccount({ username, tone: accountTone(username) });
			this.onLoggedIn({ username, tone: accountTone(username) });
		} catch (error) {
			if(error.name === "AbortError") return;
			this.showError(error);
		} finally {
			this.setBusy(false);
		}
	}

	showError(error) {
		const status = error instanceof ApiError ? error.status : 0;
		if(this.mode === "signup" && status === HttpStatus.CONFLICT)
			return this.setMessage("That username is already taken.", "account-username");
		if(this.mode === "login" && [HttpStatus.UNAUTHORIZED, HttpStatus.NOT_FOUND].includes(status))
			return this.setMessage("Incorrect username or password.", "account-password");
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
		if(focusId) byId(focusId).focus();
	}
}

export class AccountDialog {
	constructor(api, { onDeleted }) {
		this.api = api;
		this.onDeleted = onDeleted;
		this.account = null;
		this.busy = false;

		byId("account-delete-form").addEventListener("submit", (event) => {
			event.preventDefault();
			this.submit();
		});
	}

	open(account) {
		this.account = account;
		byId("account-delete-form").reset();
		byId("account-dialog-name").textContent = account.username;
		this.setError();
		dialog("account-dialog");
		byId("account-delete-password").focus();
	}

	async submit() {
		if(this.busy) return;
		const password = byId("account-delete-password").value;
		if(!password) return this.setError("Enter your password.");

		this.setBusy(true);
		this.setError();
		try {
			await this.api.deleteAccount(this.account.username, password);
			byId("account-delete-form").reset();
			dialog("account-dialog", false);
			await this.onDeleted();
		} catch (error) {
			if(error.name === "AbortError") return;
			this.setError(error instanceof ApiError && error.status === HttpStatus.NOT_FOUND
				? "Incorrect password."
				: error.message);
		} finally {
			this.setBusy(false);
		}
	}

	setBusy(busy) {
		this.busy = busy;
		const submit = byId("account-delete-submit");
		submit.disabled = busy;
		submit.textContent = busy ? "Deleting..." : "Delete account";
	}

	setError(error = "") {
		byId("account-delete-error").textContent = error;
		byId("account-delete-error").hidden = !error;
		if(error) byId("account-delete-password").focus();
	}
}
