import { loopbackHost } from "./preferences.js";

export async function serverAddress(api) {
	try {
		const { address, port } = await api.serverInfo();
		if(typeof address !== "string" || !address || loopbackHost(address)) return null;
		return { address, port: port > 0 ? String(port) : location.port };
	} catch {
		return null;
	}
}
