package reivajh06.model.pieces;

import reivajh06.model.Board;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveBuilder;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

	public Bishop(PieceColor color) {
		super(color);
		name = "Bishop";
	}

	@Override
	public List<Move> moveset() {
		List<Move> moveset = new ArrayList<>();

		moveset.add(new MoveBuilder().forwardLeft().allRepetitions().build());
		moveset.add(new MoveBuilder().forwardRight().allRepetitions().build());
		moveset.add(new MoveBuilder().backwardLeft().allRepetitions().build());
		moveset.add(new MoveBuilder().backwardRight().allRepetitions().build());

		return moveset;
	}
}
