package reivajh06.infrastructure.adapters.web.dtos;

import reivajh06.architecture.domain.model.Tile;

import java.util.UUID;

public record PieceMovesRequestDto(Tile from, UUID playerId) {
}
