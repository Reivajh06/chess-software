package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.api.ChessAPI;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class GetServerAddress implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 53);
			String address = socket.getLocalAddress().getHostAddress();

			ctx.result(Json.toJson(Map.of(
					"address", address,
					"port", ChessAPI.PORT
			)));

		} catch(Exception e) {
			ctx.status(503);
		}
	}
}
