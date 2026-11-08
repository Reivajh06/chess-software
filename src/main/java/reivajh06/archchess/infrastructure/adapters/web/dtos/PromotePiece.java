package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Tile;

import java.util.UUID;

public record PromotePiece(Tile tile, PieceType newType, UUID playerId) {
}
