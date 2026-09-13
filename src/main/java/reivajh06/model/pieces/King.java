package reivajh06.model.pieces;

import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveBuilder;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

	public King(PieceColor color) {
		super(color);
		name = "King";
	}

	@Override
	public List<Move> moveset() {
		List<Move> moveset = new ArrayList<>();

		return moveset;
	}
}
