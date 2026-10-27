package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.api.ApiException;
import reivajh06.api.ChessAPI;
import reivajh06.api.dtos.ServerAddressDto;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class GetServerAddress extends EndpointHandler<ServerAddressDto> {

	@Override
	protected ServerAddressDto run(Context ctx) throws ApiException {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 53);
			String address = socket.getLocalAddress().getHostAddress();

			return new ServerAddressDto(address, ChessAPI.PORT);

		} catch(Exception e) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Error while fetching server address");
		}
	}
}
