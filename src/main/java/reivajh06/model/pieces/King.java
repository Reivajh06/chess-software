package reivajh06.model.pieces;

import java.util.List;

public class King extends Piece {

	public King(PieceColor color) {
		super(color);
		name = "King";
	}

	@Override
	public List<String> possibleMoves(String currentTile) {
		return List.of();
	}
}
