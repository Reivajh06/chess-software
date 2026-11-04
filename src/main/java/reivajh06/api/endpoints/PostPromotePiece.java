package reivajh06.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import reivajh06.Archchess;
import reivajh06.FenParser;
import reivajh06.Json;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.dtos.PromotePieceDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.datalake.events.PromotePieceEvent;

import java.util.Map;
import java.util.UUID;

public class PostPromotePiece extends EndpointHandler<PromotePieceDto, Void> {

    @Override
    protected Void run(Context ctx, Map<String, String> pathParamMap, PromotePieceDto request) {
        GameSession session = Sessions.fetchSession(UUID.fromString(pathParamMap.get("sessionId")));

        if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

        GameDirector director = new GameDirector(session);
        director.promotePiece(request.tile(), request.newType());

        Archchess.instance().publish(new PromotePieceEvent(
                ctx.sessionAttribute("username") == null ? ctx.ip() : ctx.sessionAttribute("username"),
                session.gameState().board().pieceAt(request.tile()).type(),
                session.gameState().board().pieceAt(request.tile()).color(),
                request.newType(),
                session.id(),
                session.gameState().status(),
                FenParser.gameStateToFen(session.gameState())
        ));

        return null;
    }

    @Override
    protected PromotePieceDto deserializeBody(String body) {
        return Json.fromJsonPretty(body, PromotePieceDto.class);
    }
}
