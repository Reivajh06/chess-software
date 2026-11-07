package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.Tile;

import java.util.UUID;

public record PieceMovesRequestDto(Tile from, UUID playerId) {
}
