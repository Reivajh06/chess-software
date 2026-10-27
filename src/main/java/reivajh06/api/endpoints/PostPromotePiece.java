package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.PromotePieceDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;

import java.util.Map;
import java.util.UUID;

public class PostPromotePiece extends EndpointHandler<PromotePieceDto, Void> {

    @Override
    protected Void run(Map<String, String> pathParamMap, PromotePieceDto request) {
        GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

        if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

        GameDirector updater = new GameDirector(session);
        updater.promotePiece(request.tile(), request.newType());

        return null;
    }

    @Override
    protected PromotePieceDto deserializeBody(Context ctx) {
        return null;
    }


}
