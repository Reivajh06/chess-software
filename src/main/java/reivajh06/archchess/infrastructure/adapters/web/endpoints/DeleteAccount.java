package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.datalake.events.DeleteAccountEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.AccountRequest;

public class DeleteAccount implements EndpointOperator<AccountRequest, Void> {

	@Override
	public Void run(EndpointContext ctx, AccountRequest input) {
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
	public AccountRequest deserializeBody(String body) {
		return Json.fromJsonPretty(body, AccountRequest.class);
	}
}
