// Where other machines can reach this server. The server reports the IP of
// its network interface with the default route; it depends on the machine
// hosting it, so it is asked every time and never stored.
import { loopbackHost } from "./preferences.js";

// { address, port } or null when the server cannot tell (the user types it).
export async function serverAddress(api) {
  try {
    const { address, port } = await api.serverInfo();
    if (typeof address !== "string" || !address || loopbackHost(address)) return null;
    return { address, port: port != null ? String(port) : location.port };
  } catch {
    return null;
  }
}
