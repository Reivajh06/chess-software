package reivajh06.model.pieces;

import java.util.List;

public class Rook extends Piece {

	public Rook(PieceColor color) {
		super(color);
		name = "Rook";
	}

	@Override
	public List<String> possibleMoves(String currentTile) {
		return List.of();
	}
}
