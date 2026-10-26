package reivajh06.api.endpoints;

import reivajh06.api.ports.EndpointOperator;
import reivajh06.api.ports.InputDeserializer;
import reivajh06.api.ports.OutputSerializer;
import reivajh06.chess.gamelogic.GameSession;

import java.util.UUID;

public class GetReadCurrentState extends EndpointHandler<UUID, GameSession> {

	public GetReadCurrentState(InputDeserializer<UUID> inputDeserializer, OutputSerializer<GameSession> outputSerializer, EndpointOperator<UUID, GameSession> endpointOperator) {
		super(inputDeserializer, outputSerializer, endpointOperator);
	}
}
