package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameUpdater;
import reivajh06.model.Tile;
import reivajh06.model.movement.Move;

import java.util.*;

public class PostLegalMoves implements Handler {

	@Override
	public void handle(@NotNull Context ctx) {
	}
}
