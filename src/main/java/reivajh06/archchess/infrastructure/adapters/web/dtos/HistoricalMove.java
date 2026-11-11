package reivajh06.archchess.infrastructure.adapters.web.dtos;

import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.architecture.domain.model.game.GameStatus;

public record HistoricalMove(
		int ply,
		String player,
		PieceType piece,
		Tile from,
		Tile to,
		PieceType promotion,
		String fen,
		GameStatus status,
		String timestamp
) {
}
