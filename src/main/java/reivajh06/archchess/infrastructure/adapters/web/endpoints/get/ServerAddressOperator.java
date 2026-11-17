package reivajh06.archchess.infrastructure.adapters.web.endpoints.get;

import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.ServerAddress;
import reivajh06.archchess.infrastructure.adapters.web.endpoints.EndpointOperator;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerAddressOperator implements EndpointOperator<Void, ServerAddress> {

	private final int port;

	public ServerAddressOperator(int port) {
		this.port = port;
	}

	@Override
	public ServerAddress run(EndpointContext ctx, Void input) {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 53);
			String address = socket.getLocalAddress().getHostAddress();

			return new ServerAddress(address, port);

		} catch(Exception e) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Error while fetching server address");
		}
	}

	@Override
	public Void deserializeBody(String body) {
		return null;
	}
}
