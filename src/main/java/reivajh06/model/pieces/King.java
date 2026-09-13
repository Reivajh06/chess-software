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

		moveset.add(new MoveBuilder().forward().repetitions(1).build());
		moveset.add(new MoveBuilder().backward().repetitions(1).build());
		moveset.add(new MoveBuilder().left().repetitions(1).build());
		moveset.add(new MoveBuilder().right().repetitions(1).build());
		moveset.add(new MoveBuilder().forwardLeft().repetitions(1).build());
		moveset.add(new MoveBuilder().forwardRight().repetitions(1).build());
		moveset.add(new MoveBuilder().backwardLeft().repetitions(1).build());
		moveset.add(new MoveBuilder().backwardRight().repetitions(1).build());

		return moveset;
	}
}
