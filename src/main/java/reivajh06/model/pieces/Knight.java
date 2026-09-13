package reivajh06.model.pieces;

import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveBuilder;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

	public Knight(PieceColor color) {
		super(color);
		name = "Knight";
	}

	@Override
	public String name() {
		return name.substring(0, 2);
	}

	@Override
	public String getCode() {
		return color.name().charAt(0) + name();
	}

	@Override
	public List<Move> moveset() {
		List<Move> moveset = new ArrayList<>();

		moveset.add(new MoveBuilder().forward().forward().left().repetitions(1).build());
		moveset.add(new MoveBuilder().forward().forward().right().repetitions(1).build());
		moveset.add(new MoveBuilder().forward().left().left().repetitions(1).build());
		moveset.add(new MoveBuilder().forward().right().right().repetitions(1).build());
		moveset.add(new MoveBuilder().backward().backward().left().repetitions(1).build());
		moveset.add(new MoveBuilder().backward().backward().right().repetitions(1).build());
		moveset.add(new MoveBuilder().backward().left().left().repetitions(1).build());
		moveset.add(new MoveBuilder().backward().right().right().repetitions(1).build());

		return moveset;
	}
}
