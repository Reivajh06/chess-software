package reivajh06.api.endpoints;

import reivajh06.Archchess;
import reivajh06.Sessions;
import reivajh06.api.ApiException;
import reivajh06.api.EndpointContext;
import reivajh06.api.HttpStatus;
import reivajh06.api.dtos.PromotePieceDto;
import reivajh06.chess.gamelogic.GameDirector;
import reivajh06.chess.gamelogic.GameSession;
import reivajh06.datalake.events.PromotePieceEvent;
import reivajh06.services.FenParser;
import reivajh06.services.Json;

import java.util.UUID;

public class PostPromotePiece implements EndpointOperator<PromotePieceDto, Void> {

    @Override
    public Void run(EndpointContext ctx, PromotePieceDto request) {
        GameSession session = Sessions.fetchSession(UUID.fromString(ctx.pathParams().get("sessionId")));

        if(session == null) throw new ApiException(HttpStatus.NOT_FOUND, "Session not found");

        GameDirector director = new GameDirector(session);

        if(!director.promotePiece(request.tile(), request.newType())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot promote piece to %s".formatted(request.newType()));
        }

        Archchess.instance().publish(new PromotePieceEvent(
                ctx.sessionAttribute("username") == null ? ctx.sessionPlayer().username() : ctx.sessionAttribute("username"),
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
    public PromotePieceDto deserializeBody(String body) {
        return Json.fromJsonPretty(body, PromotePieceDto.class);
    }
}
