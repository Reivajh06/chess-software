package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Tile;

import java.util.Map;

public record RequestMovePiece(
		String pluginName,
		int moveId,
		Tile from,
		Tile to,
		PieceType promoteTo,
		Map<String, String> details
) {
}
