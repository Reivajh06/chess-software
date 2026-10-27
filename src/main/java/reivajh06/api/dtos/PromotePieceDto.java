package reivajh06.api.dtos;

import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;

import java.util.UUID;

public record PromotePieceDto(Tile tile, PieceType newType, UUID playerId) {
}
