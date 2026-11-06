package reivajh06.infrastructure.adapters.web.dtos;

import reivajh06.architecture.domain.model.Tile;

import java.util.UUID;

public record MovePieceDto(
		String playerType,
		Tile from,
		Tile to,
		Integer moveId,
		UUID playerId
) {
}
