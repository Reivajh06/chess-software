package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Archchess;
import reivajh06.services.Json;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.AccountRequestDto;
import reivajh06.database.Player;
import reivajh06.datalake.events.LoginEvent;

import java.util.Map;

public class PostLogin extends EndpointHandler<AccountRequestDto, Void> {

	@Override
	protected Void run(Context ctx, Map<String, String> pathParamMap, AccountRequestDto input) {
		Player player = new Player(input.username(), input.password());

		if(!Archchess.instance().database().playerExists(player)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Player account does not exist or is not valid");
		}

		ctx.sessionAttribute("username", player.username());
		Archchess.instance().publish(new LoginEvent(player.username()));

		return null;
	}

	@Override
	protected AccountRequestDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, AccountRequestDto.class);
	}
}
