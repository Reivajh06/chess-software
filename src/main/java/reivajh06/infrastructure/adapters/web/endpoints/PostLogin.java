package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.web.ApiException;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.HttpStatus;
import reivajh06.infrastructure.adapters.web.dtos.AccountRequestDto;
import reivajh06.architecture.domain.model.Player;
import reivajh06.infrastructure.adapters.datalake.events.LoginEvent;
import reivajh06.infrastructure.adapters.Json;

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
