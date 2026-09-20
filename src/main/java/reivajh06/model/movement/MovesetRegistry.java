package reivajh06.model.movement;

import reivajh06.model.PieceType;

import java.util.*;

public class MovesetRegistry {

	private static final List<Move> PAWN = List.of(
			new MoveBuilder().forward().build()
	);

	private static final List<Move> ROOK = List.of(
			new MoveBuilder().forward().allRepetitions().build(),
			new MoveBuilder().backward().allRepetitions().build(),
			new MoveBuilder().left().allRepetitions().build(),
			new MoveBuilder().right().allRepetitions().build()
	);

	private static final List<Move> KING = List.of(
			new MoveBuilder().forward().build(),
			new MoveBuilder().backward().build(),
			new MoveBuilder().left().build(),
			new MoveBuilder().right().build(),
			new MoveBuilder().forwardLeft().build(),
			new MoveBuilder().forwardRight().build(),
			new MoveBuilder().backwardLeft().build(),
			new MoveBuilder().backwardRight().build()
	);

	private static final List<Move> QUEEN = List.of(
			new MoveBuilder().forward().allRepetitions().build(),
			new MoveBuilder().backward().allRepetitions().build(),
			new MoveBuilder().left().allRepetitions().build(),
			new MoveBuilder().right().allRepetitions().build(),
			new MoveBuilder().forwardLeft().allRepetitions().build(),
			new MoveBuilder().forwardRight().allRepetitions().build(),
			new MoveBuilder().backwardLeft().allRepetitions().build(),
			new MoveBuilder().backwardRight().allRepetitions().build()
	);

	private static final List<Move> BISHOP = List.of(
			new MoveBuilder().forwardLeft().allRepetitions().build(),
			new MoveBuilder().forwardRight().allRepetitions().build(),
			new MoveBuilder().backwardLeft().allRepetitions().build(),
			new MoveBuilder().backwardRight().allRepetitions().build()
	);

	private static final List<Move> KNIGHT = List.of(
			new MoveBuilder().forward().forward().left().build(),
			new MoveBuilder().forward().forward().right().build(),
			new MoveBuilder().backward().backward().left().build(),
			new MoveBuilder().backward().backward().right().build(),
			new MoveBuilder().forward().left().left().build(),
			new MoveBuilder().forward().right().right().build(),
			new MoveBuilder().backward().left().left().build(),
			new MoveBuilder().backward().right().right().build()
	);

	private final Map<String, List<Move>> movesets;

	public MovesetRegistry() {
		movesets = new HashMap<>();
		addPredefinedMoveset(PieceType.PAWN, PAWN);
		addPredefinedMoveset(PieceType.QUEEN, QUEEN);
		addPredefinedMoveset(PieceType.BISHOP, BISHOP);
		addPredefinedMoveset(PieceType.KNIGHT, KNIGHT);
		addPredefinedMoveset(PieceType.ROOK, ROOK);
		addPredefinedMoveset(PieceType.KING, KING);
	}

	public boolean addPredefinedMoveset(PieceType classicType, List<Move> moveset) {
		return addCustomMoveset(classicType.name(), moveset);
	}

	public boolean addCustomMoveset(String typeName, List<Move> moveset) {
		if(movesets.containsKey(typeName)) return false;

		movesets.put(typeName, new ArrayList<>(moveset));

		return true;
	}

	public void updateMoveset(String typeName, List<Move> moves) {
		List<Move> moveset = movesets.get(typeName);

		if(moveset == null) return;

		moveset.addAll(moves);
	}

	public List<Move> removeMoveset(String typeName) {
		if(Arrays.stream(PieceType.values()).map(PieceType::name).toList().contains(typeName)) return null;

		return movesets.remove(typeName);
	}

	public List<Move> getMoveset(String typeName) {
		return movesets.get(typeName);
	}
}
