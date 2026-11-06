package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.web.ApiException;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.HttpStatus;
import reivajh06.infrastructure.adapters.web.dtos.AccountRequestDto;
import reivajh06.architecture.domain.model.Player;
import reivajh06.infrastructure.adapters.datalake.events.DeleteAccountEvent;
import reivajh06.infrastructure.adapters.Json;

public class DeleteAccount implements EndpointOperator<AccountRequestDto, Void> {

	@Override
	public Void run(EndpointContext ctx, AccountRequestDto input) {
		Player player = new Player(ctx.sessionPlayer().username(), input.password());

		if(!Archchess.instance().database().playerExists(player)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Player does not exists");
		}

		Archchess.instance().database().deletePlayer(player);
		ctx.invalidateSession();
		Archchess.instance().publish(new DeleteAccountEvent(player.username()));
		return null;
	}

	@Override
	public AccountRequestDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, AccountRequestDto.class);
	}
}
