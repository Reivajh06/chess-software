package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Archchess;
import reivajh06.Json;
import reivajh06.ShaEncoder;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.CreateAccountRequestDto;
import reivajh06.database.Player;

import java.util.Map;
import java.util.regex.Pattern;

public class PostCreateAccount extends EndpointHandler<CreateAccountRequestDto, Void> {

	@Override
	protected Void run(Map<String, String> pathParamMap, CreateAccountRequestDto request) {
		if(!isPlayerValid(request)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid username/password");

		Player player = new Player(request.username(), ShaEncoder.encode(request.password()));

		if(Archchess.getInstance().database().playerExists(request.username())) {
			throw new ApiException(HttpStatus.CONFLICT, "Account already exists");
		}

		Archchess.getInstance().database().registerPlayer(player);

		return null;
	}

	@Override
	protected CreateAccountRequestDto deserializeBody(Context ctx) {
		return Json.fromJson(ctx.body(), CreateAccountRequestDto.class);
	}

	private boolean isPlayerValid(CreateAccountRequestDto request) {
		if(request.username().contains(request.password())) return false;

		Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
		Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

		if(!usernamePattern.matcher(request.username()).matches()) return false;

		return passwordPattern.matcher(request.password()).matches();
	}
}
