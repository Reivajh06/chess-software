package reivajh06.model.pieces;

import reivajh06.model.Board;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveBuilder;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

	private boolean startingMove;

	public Pawn(PieceColor color) {
		super(color);
		name = "Pawn";
		startingMove = true;
	}

	public void finishStartMove() {
		startingMove = false;
	}

	@Override
	public List<Move> moveset() {
		List<Move> moveset = new ArrayList<>();

		moveset.add(new MoveBuilder().forward().repetitions(startingMove ? 2 : 1).build());
		moveset.add(new MoveBuilder().forwardLeft().repetitions(1).build());
		moveset.add(new MoveBuilder().forwardRight().repetitions(1).build());

		return moveset;
	}
}
