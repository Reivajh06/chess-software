package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.api.ApiException;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.api.dtos.AccountRequestDto;
import reivajh06.database.Player;
import reivajh06.datalake.events.DeleteAccountEvent;
import reivajh06.services.Json;

public class DeleteAccount implements EndpointOperator<AccountRequestDto, Void> {

	@Override
	public Void run(EndpointContext ctx, AccountRequestDto input) {
		Player player = ctx.sessionPlayer();

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
