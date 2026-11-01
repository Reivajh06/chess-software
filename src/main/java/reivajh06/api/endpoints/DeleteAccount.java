package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Archchess;
import reivajh06.Json;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.AccountRequestDto;
import reivajh06.database.Player;

import java.util.Map;

public class DeleteAccount extends EndpointHandler<AccountRequestDto, Void>{

	@Override
	protected Void run(Context ctx, Map<String, String> pathParamMap, AccountRequestDto input) {
		Player player = new Player(input.username(), input.password());

		if(!Archchess.instance().database().playerExists(player)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Player does not exists");
		}

		Archchess.instance().database().deletePlayer(player);
		ctx.req().getSession().invalidate();

		return null;
	}

	@Override
	protected AccountRequestDto deserializeBody(Context ctx) {
		return Json.fromJson(ctx.body(), AccountRequestDto.class);
	}
}
