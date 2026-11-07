package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.datalake.events.LoginEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.AccountRequestDto;

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
