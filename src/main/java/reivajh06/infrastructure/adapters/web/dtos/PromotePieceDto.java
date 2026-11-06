package reivajh06.infrastructure.adapters.web.dtos;

import reivajh06.architecture.domain.model.PieceType;
import reivajh06.architecture.domain.model.Tile;

import java.util.UUID;

public record PromotePieceDto(Tile tile, PieceType newType, UUID playerId) {
}
