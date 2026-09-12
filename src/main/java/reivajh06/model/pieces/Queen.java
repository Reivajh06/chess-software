package reivajh06.model.pieces;

import java.util.List;

public class Queen extends Piece {

	public Queen(PieceColor color) {
		super(color);
		name = "Queen";
	}

	@Override
	public List<String> possibleMoves(String currentTile) {
		return List.of();
	}
}
