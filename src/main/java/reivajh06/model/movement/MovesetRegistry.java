package reivajh06.model.movement;

import reivajh06.model.PieceType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MovesetRegistry {

	private static final Set<Move> PAWN = Set.of(
			new MoveBuilder().forward().build(),
			new MoveBuilder().forwardLeft().build(),
			new MoveBuilder().forwardRight().build()
	);

	private static final Set<Move> ROOK = Set.of(
			new MoveBuilder().forward().allRepetitions().build(),
			new MoveBuilder().backward().allRepetitions().build(),
			new MoveBuilder().left().allRepetitions().build(),
			new MoveBuilder().right().allRepetitions().build()
	);

	private static final Set<Move> KING = Set.of(
			new MoveBuilder().forward().build(),
			new MoveBuilder().backward().build(),
			new MoveBuilder().left().build(),
			new MoveBuilder().right().build(),
			new MoveBuilder().forwardLeft().build(),
			new MoveBuilder().forwardRight().build(),
			new MoveBuilder().backwardLeft().build(),
			new MoveBuilder().backwardRight().build()
	);

	private static final Set<Move> QUEEN = Set.of(
			new MoveBuilder().forward().allRepetitions().build(),
			new MoveBuilder().backward().allRepetitions().build(),
			new MoveBuilder().left().allRepetitions().build(),
			new MoveBuilder().right().allRepetitions().build(),
			new MoveBuilder().forwardLeft().allRepetitions().build(),
			new MoveBuilder().forwardRight().allRepetitions().build(),
			new MoveBuilder().backwardLeft().allRepetitions().build(),
			new MoveBuilder().backwardRight().allRepetitions().build()
	);

	private static final Set<Move> BISHOP = Set.of(
			new MoveBuilder().forwardLeft().allRepetitions().build(),
			new MoveBuilder().forwardRight().allRepetitions().build(),
			new MoveBuilder().backwardLeft().allRepetitions().build(),
			new MoveBuilder().backwardRight().allRepetitions().build()
	);

	private static final Set<Move> KNIGHT = Set.of(
			new MoveBuilder().forward().forward().left().build(),
			new MoveBuilder().forward().forward().right().build(),
			new MoveBuilder().backward().backward().left().build(),
			new MoveBuilder().backward().backward().right().build(),
			new MoveBuilder().forward().left().left().build(),
			new MoveBuilder().forward().right().right().build(),
			new MoveBuilder().backward().left().left().build(),
			new MoveBuilder().backward().right().right().build()
	);

	private final Map<String, Set<Move>> movesets;

	public MovesetRegistry() {
		movesets = new HashMap<>();
		addPredefinedMoveset(PieceType.PAWN, PAWN);
		addPredefinedMoveset(PieceType.QUEEN, QUEEN);
		addPredefinedMoveset(PieceType.BISHOP, BISHOP);
		addPredefinedMoveset(PieceType.KNIGHT, KNIGHT);
		addPredefinedMoveset(PieceType.ROOK, ROOK);
		addPredefinedMoveset(PieceType.KING, KING);
	}

	public boolean addPredefinedMoveset(PieceType classicType, Set<Move> moveset) {
		return addCustomMoveset(classicType.name(), moveset);
	}

	public boolean addCustomMoveset(String typeName, Set<Move> moveset) {
		if(movesets.containsKey(typeName)) return false;

		movesets.put(typeName, moveset);

		return true;
	}

	public void updateMoveset(String typeName, Set<Move> moves) {
		Set<Move> moveset = movesets.get(typeName);

		if(moveset == null) return;

		moveset.addAll(moves);
	}

	public Set<Move> removeMoveset(String typeName) {
		if(Arrays.stream(PieceType.values()).map(PieceType::name).toList().contains(typeName)) return null;

		return movesets.remove(typeName);
	}

	public Set<Move> getMoveset(String typeName) {
		return movesets.get(typeName);
	}
}
