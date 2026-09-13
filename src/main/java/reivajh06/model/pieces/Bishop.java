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

		moveset.add(new MoveBuilder().forwardLeft().repetitions(Board.ALL_TILES).build());
		moveset.add(new MoveBuilder().forwardRight().repetitions(Board.ALL_TILES).build());
		moveset.add(new MoveBuilder().backwardLeft().repetitions(Board.ALL_TILES).build());
		moveset.add(new MoveBuilder().backwardRight().repetitions(Board.ALL_TILES).build());

		return moveset;
	}
}
