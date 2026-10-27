package reivajh06.api.dtos;

import reivajh06.chess.model.Tile;

import java.util.UUID;

public record PieceMovesDto(Tile from, UUID playerId) {
}
