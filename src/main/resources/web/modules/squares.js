export function square(value) {
	if(typeof value === "string") {
		return /^[a-h][1-8]$/i.test(value) ? value.toLowerCase() : null;
	}

	if(!value) return null;

	const file = String(value.file?.name ?? value.file ?? "")
			.replace(/^File\./i, "")
			.toLowerCase();

	const rank = String(
			value.rank?.number ?? value.rank?.name ?? value.rank ?? ""
	).replace(/^R/i, "");

	return square(file + rank);
}
