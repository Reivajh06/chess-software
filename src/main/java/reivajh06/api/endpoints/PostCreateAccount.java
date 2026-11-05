package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.api.ApiException;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.api.dtos.AccountRequestDto;
import reivajh06.database.Player;
import reivajh06.datalake.events.CreateAccountEvent;
import reivajh06.services.Json;

import java.util.regex.Pattern;

public class PostCreateAccount implements EndpointOperator<AccountRequestDto, Void> {

	@Override
	public Void run(EndpointContext ctx, AccountRequestDto request) {
		if(!isPlayerValid(request)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid username/password");

		Player player = new Player(request.username(), request.password());

		if(Archchess.instance().database().playerExists(player)) {
			throw new ApiException(HttpStatus.CONFLICT, "Account already exists");
		}

		Archchess.instance().database().registerPlayer(player);
		ctx.sessionAttribute("username", player.username());
		Archchess.instance().publish(new CreateAccountEvent(player.username()));

		return null;
	}

	@Override
	public AccountRequestDto deserializeBody(String body) {
		return Json.fromJsonPretty(body, AccountRequestDto.class);
	}

	private boolean isPlayerValid(AccountRequestDto request) {
		if(request.username().contains(request.password())) return false;

		Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
		Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

		if(!usernamePattern.matcher(request.username()).matches()) return false;

		return passwordPattern.matcher(request.password()).matches();
	}
}
