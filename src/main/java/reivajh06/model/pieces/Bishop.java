package reivajh06.model.pieces;

import java.util.List;

public class Bishop extends Piece {

	public Bishop(PieceColor color) {
		super(color);
		name = "Bishop";
	}

	@Override
	public List<String> possibleMoves(String currentTile) {
		return List.of();
	}
}
