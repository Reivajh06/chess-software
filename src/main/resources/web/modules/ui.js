export const byId = (id) => document.getElementById(id);
export const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export function element(tag, className, text) {
	const node = document.createElement(tag);
	if(className) node.className = className;
	if(text !== undefined) node.textContent = text;
	if(tag === "button") node.type = "button";
	return node;
}

export function optionButton(label, detail, icon, action) {
	const button = element("button", "title-option");
	const text = element("span");
	text.append(element("strong", "", label));
	if(detail) text.append(element("small", "", detail));
	if(icon) {
		const image = element("img", "title-option-art");
		image.src = `./assets/ui/${icon}.png`;
		image.alt = "";
		image.setAttribute("aria-hidden", "true");
		button.append(image);
	}
	button.append(text);
	button.addEventListener("click", action);
	// The pawn cursor follows focus, so pointing at a row moves it there (as the arrow keys do).
	button.addEventListener("pointerenter", () => button.disabled || button.focus({ preventScroll: true }));
	return button;
}

export function dialog(id, open = true, focus) {
	const node = byId(id);
	if(open && !node.open) node.showModal();
	else if(!open) node.close();
	if(open && focus) node.querySelector(focus)?.focus({ preventScroll: true });
	return node;
}

export function bindDialogs() {
	document.addEventListener("click", (event) => {
		const button = event.target.closest?.("[data-close], [data-answer]");
		if(button) (byId(button.dataset.close) ?? button.closest("dialog"))?.close(button.dataset.answer ?? "");
	});
}

export function confirmDialog(id) {
	const node = dialog(id);
	node.returnValue = "";
	return new Promise((resolve) => node.addEventListener("close", () => resolve(node.returnValue === "confirm"), { once: true }));
}

export function tabGroup(tabs, onSelect, initial = Object.keys(tabs)[0]) {
	function select(name, focus = false) {
		for(const [key, tab] of Object.entries(tabs)) {
			tab.setAttribute("aria-selected", String(key === name));
			tab.tabIndex = key === name ? 0 : -1;
		}
		onSelect(name);
		if(focus) tabs[name].focus();
	}
	for(const [name, tab] of Object.entries(tabs)) {
		tab.addEventListener("click", () => select(name));
		tab.addEventListener("keydown", (event) => {
			const names = Object.keys(tabs).filter((key) => !tabs[key].hidden);
			const step = { ArrowRight: 1, ArrowLeft: -1 }[event.key];
			if(names.length < 2 || (!step && !["Home", "End"].includes(event.key))) return;
			event.preventDefault();
			event.stopPropagation();
			select(event.key === "Home" ? names[0] : event.key === "End" ? names.at(-1)
				: names[(names.indexOf(name) + step + names.length) % names.length], true);
		});
	}
	select(initial);
	return select;
}
