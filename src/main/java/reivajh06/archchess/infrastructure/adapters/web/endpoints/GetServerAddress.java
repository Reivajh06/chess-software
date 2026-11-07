package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.ServerAddressDto;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class GetServerAddress implements EndpointOperator<Void, ServerAddressDto> {

	private final int port;

	public GetServerAddress(int port) {
		this.port = port;
	}

	@Override
	public ServerAddressDto run(EndpointContext ctx, Void input) {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 53);
			String address = socket.getLocalAddress().getHostAddress();

			return new ServerAddressDto(address, port);

		} catch(Exception e) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Error while fetching server address");
		}
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
