import { loopbackHost } from "./preferences.js";

export async function serverAddress(api) {
  try {
    const { address, port } = await api.serverInfo();
    if (typeof address !== "string" || !address || loopbackHost(address)) return null;
    // Un puerto 0 o ausente no sirve para abrir un enlace: la página ya se sirve desde el puerto del servidor.
    return { address, port: port > 0 ? String(port) : location.port };
  } catch {
    return null;
  }
}
