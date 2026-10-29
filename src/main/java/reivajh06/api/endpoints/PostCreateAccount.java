package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Json;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.CreateAccountRequestDto;
import reivajh06.players.Player;
import reivajh06.players.PlayerDatabaseClient;

import java.util.Map;

public class PostCreateAccount extends EndpointHandler<CreateAccountRequestDto, Void> {

	@Override
	protected Void run(Map<String, String> pathParamMap, CreateAccountRequestDto request) {
		PlayerDatabaseClient manager = new PlayerDatabaseClient();

		if(manager.playerExists(request.username())) throw new ApiException(HttpStatus.CONFLICT, "Player with username %s already exists".formatted(request.username()));

		manager.registerPlayer(new Player(request.username(), request.password()));

		return null;
	}

	@Override
	protected CreateAccountRequestDto deserializeBody(Context ctx) {
		return Json.fromJson(ctx.body(), CreateAccountRequestDto.class);
	}
}
