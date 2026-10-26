package reivajh06.api.dto;

import reivajh06.chess.model.Tile;

import java.util.UUID;

public record PieceMovesDto(Tile from, UUID playerId) {
}
