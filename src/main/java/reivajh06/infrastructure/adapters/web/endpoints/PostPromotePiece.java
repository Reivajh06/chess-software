package reivajh06.infrastructure.adapters.web.endpoints;

import reivajh06.architecture.domain.logic.GameDirector;
import reivajh06.architecture.domain.model.game.GameSession;
import reivajh06.architecture.domain.services.FenParser;
import reivajh06.infrastructure.Archchess;
import reivajh06.infrastructure.adapters.Json;
import reivajh06.infrastructure.adapters.datalake.events.PromotePieceEvent;
import reivajh06.infrastructure.adapters.web.ApiException;
import reivajh06.infrastructure.adapters.web.EndpointContext;
import reivajh06.infrastructure.adapters.web.HttpStatus;
import reivajh06.infrastructure.adapters.web.dtos.PromotePieceDto;

import java.util.UUID;

public class PostPromotePiece implements EndpointOperator<PromotePieceDto, Void> {

    @Override
    public Void run(EndpointContext ctx, PromotePieceDto request) {
        GameSession session = Archchess.instance().session(UUID.fromString(ctx.pathParams().get("sessionId")));

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
