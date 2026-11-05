package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.api.ApiException;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.api.dtos.AccountRequestDto;
import reivajh06.database.Player;
import reivajh06.datalake.events.LoginEvent;
import reivajh06.services.Json;

public class PostLogin implements EndpointOperator<AccountRequestDto, Void> {

	@Override
	public Void run(EndpointContext ctx, AccountRequestDto input) {
		Player player = new Player(input.username(), input.password());

		if(!Archchess.instance().database().playerExists(player)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Player account does not exist or is not valid");
		}

		ctx.sessionAttribute("username", player.username());
		Archchess.instance().publish(new LoginEvent(player.username()));

		return null;
	}

	@Override
	public AccountRequestDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, AccountRequestDto.class);
	}
}
