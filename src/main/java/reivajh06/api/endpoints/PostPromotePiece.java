package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;

import java.util.Map;
import java.util.UUID;

public class PostPromotePiece extends EndpointHandler {

    @Override
    protected Object run(Context ctx) {
        GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

        if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

        Map<String, String> promotionDetails = Json.fromJson(ctx.body(), Map.class);

        GameDirector updater = new GameDirector(session);
        updater.promotePiece(new Tile(promotionDetails.get("tile")), PieceType.valueOf(promotionDetails.get("newType")));

        return null;
    }
}
