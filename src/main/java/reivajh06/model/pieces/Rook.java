package reivajh06.model.pieces;

import reivajh06.model.Board;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveBuilder;

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

		moveset.add(new MoveBuilder().forward().allRepetitions().build());
		moveset.add(new MoveBuilder().backward().allRepetitions().build());
		moveset.add(new MoveBuilder().left().allRepetitions().build());
		moveset.add(new MoveBuilder().right().allRepetitions().build());

		return moveset;
	}
}
