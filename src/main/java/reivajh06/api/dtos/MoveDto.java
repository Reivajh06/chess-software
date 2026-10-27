package reivajh06.api.dtos;

import reivajh06.chess.model.Tile;

import java.util.UUID;

public record MoveDto(
		String playerType,
		Tile from,
		Tile to,
		Integer moveId,
		UUID playerID
) {
}
