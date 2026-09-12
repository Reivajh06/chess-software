package reivajh06.model.pieces;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

	private boolean startingMove;

	public Pawn(PieceColor color) {
		super(color);
		name = "Pawn";
		startingMove = true;
	}

	@Override
	public List<String> possibleMoves(String currentTile) {
		List<String> moveset = new ArrayList<>();
		return moveset;

	}
}
