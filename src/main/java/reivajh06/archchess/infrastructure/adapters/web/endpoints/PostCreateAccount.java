package reivajh06.archchess.infrastructure.adapters.web.endpoints;

import reivajh06.archchess.architecture.domain.model.Player;
import reivajh06.archchess.infrastructure.Archchess;
import reivajh06.archchess.infrastructure.adapters.Json;
import reivajh06.archchess.infrastructure.adapters.datalake.events.CreateAccountEvent;
import reivajh06.archchess.infrastructure.adapters.web.ApiException;
import reivajh06.archchess.infrastructure.adapters.web.EndpointContext;
import reivajh06.archchess.infrastructure.adapters.web.HttpStatus;
import reivajh06.archchess.infrastructure.adapters.web.dtos.AccountRequest;

import java.util.regex.Pattern;

public class PostCreateAccount implements EndpointOperator<AccountRequest, Void> {

	@Override
	public Void run(EndpointContext ctx, AccountRequest request) {
		if(!isPlayerValid(request)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid username/password");

		Player player = new Player(request.username(), request.password());

		if(Archchess.instance().database().usernameExists(player.username())) {
			throw new ApiException(HttpStatus.CONFLICT, "Account already exists");
		}

		Archchess.instance().database().registerPlayer(player);
		ctx.sessionAttribute("username", player.username());
		Archchess.instance().publish(new CreateAccountEvent(player.username()));

		return null;
	}

	@Override
	public AccountRequest deserializeBody(String body) {
		return Json.fromJsonPretty(body, AccountRequest.class);
	}

	private boolean isPlayerValid(AccountRequest request) {
		if(request.username().contains(request.password())) return false;

		Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
		Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

		if(!usernamePattern.matcher(request.username()).matches()) return false;

		return passwordPattern.matcher(request.password()).matches();
	}
}
