package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.gamelogic.GameSession;
import reivajh06.gamelogic.GameUpdater;
import reivajh06.model.PieceType;
import reivajh06.model.Tile;

import java.util.Map;
import java.util.UUID;

public class PostPromotePiece implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParam("sessionId")));

        if(session == null) {
            ctx.status(404);
            return;
        }

        Map<String, String> promotionDetails = Json.fromJson(ctx.body(), Map.class);

        GameUpdater updater = new GameUpdater(session);
        updater.promotePiece(new Tile(promotionDetails.get("tile")), PieceType.valueOf(promotionDetails.get("newType")));

        ctx.status(200);
    }
}
