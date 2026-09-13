package reivajh06.model.pieces;

import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

	public Rook(PieceColor color) {
		super(color);
		name = "Rook";
	}

	@Override
	public List<Move> moveset() {
		List<Move> moveset = new ArrayList<>();

		return moveset;
	}
}
