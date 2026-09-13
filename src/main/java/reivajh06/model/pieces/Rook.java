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

		moveset.add(new MoveBuilder().forward().repetitions(Board.ALL_TILES).build());
		moveset.add(new MoveBuilder().backward().repetitions(Board.ALL_TILES).build());
		moveset.add(new MoveBuilder().left().repetitions(Board.ALL_TILES).build());
		moveset.add(new MoveBuilder().right().repetitions(Board.ALL_TILES).build());

		return moveset;
	}
}
