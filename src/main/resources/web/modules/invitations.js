import { byId, dialog } from "./ui.js";
import { loopbackHost } from "./preferences.js";
import { serverAddress } from "./network.js";

export function setupInvitations(api, sessions, store) {
	async function inviteOrigin() {
		if(!loopbackHost()) return location.origin;
		const found = await serverAddress(api);
		if(!found) return null;
		try {
			return new URL(`${location.protocol}//${found.address}${found.port ? `:${found.port}` : ""}`).origin;
		} catch {
			return null;
		}
	}

	byId("invite").addEventListener("click", () => openInvite("play"));
	byId("watch-invite").addEventListener("click", () => openInvite("watch"));

	const inviteTexts = {
		play: {
			title: "Invite an opponent",
			intro: "Send this link to your opponent. The game starts when they open it.",
			copied: "Invitation copied.",
		},
		watch: {
			title: "Invite to watch the game",
			intro: "Send this link to let someone watch the game live. They can watch but cannot move pieces.",
			copied: "Spectator link copied.",
		},
	};
	let inviteKind = "play";

	async function openInvite(kind = "play") {
		if(!store.session) {
			return;
		}

		inviteKind = kind;
		byId("invite-title").textContent = inviteTexts[kind].title;
		byId("invite-intro").textContent = inviteTexts[kind].intro;
		const sessionId = store.session.sessionId;
		byId("invite-link").value = "";
		byId("invite-link").placeholder = "Preparing the link…";
		byId("invite-copy").disabled = true;
		byId("invite-status").textContent = "";
		dialog("invite-dialog");

		const origin = await inviteOrigin();
		if(store.session?.sessionId !== sessionId) return;
		byId("invite-link").value = origin ? sessions.invitation(sessionId, origin, kind) : "";
		byId("invite-link").placeholder = "";
		byId("invite-copy").disabled = !origin;
		byId("invite-status").textContent = origin
				? ""
				: "Couldn't get this device's network address. Try again.";
		if(origin) byId("invite-copy").focus();
	}

	byId("invite-copy").addEventListener("click", async () => {
		const link = byId("invite-link").value;
		if(!link) return;

		let copied = false;
		try {
			await navigator.clipboard.writeText(link);
			copied = true;
		} catch {
			byId("invite-link").select();
			copied = document.execCommand?.("copy") ?? false;
		}

		byId("invite-status").textContent = copied
				? inviteTexts[inviteKind].copied
				: "Couldn't copy the link. Select it and copy it manually.";
	});
	return openInvite;
}
