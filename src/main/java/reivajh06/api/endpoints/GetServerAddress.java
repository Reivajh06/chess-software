package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.ServerAddressDto;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class GetServerAddress extends EndpointHandler<Void, ServerAddressDto> {

	private final int port;

	public GetServerAddress(int port) {
		this.port = port;
	}

	@Override
	protected ServerAddressDto run(Context ctx, Map<String, String> pathParamMap, Void input) {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 53);
			String address = socket.getLocalAddress().getHostAddress();

			return new ServerAddressDto(address, port);

		} catch(Exception e) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Error while fetching server address");
		}
	}

	@Override
	protected Void deserializeBody(Context ctx) {
		return null;
	}
}
