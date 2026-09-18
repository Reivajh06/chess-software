package reivajh06.model.movement;

import reivajh06.model.PieceType;

import java.util.*;
import java.util.stream.Collectors;

public class Moveset {

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

	private static final Map<String, Set<Move>> MOVESETS;

	static {
		MOVESETS = new HashMap<>();
		addPredefinedMoveset(PieceType.PAWN, PAWN);
		addPredefinedMoveset(PieceType.QUEEN, QUEEN);
		addPredefinedMoveset(PieceType.BISHOP, BISHOP);
		addPredefinedMoveset(PieceType.KNIGHT, KNIGHT);
		addPredefinedMoveset(PieceType.ROOK, ROOK);
		addPredefinedMoveset(PieceType.KING, KING);
	}

	public static boolean addPredefinedMoveset(PieceType classicType, Set<Move> moveset) {
		return addCustomMoveset(classicType.name(), moveset);
	}

	public static boolean addCustomMoveset(String typeName, Set<Move> moveset) {
		if(MOVESETS.containsKey(typeName)) return false;

		MOVESETS.put(typeName.toLowerCase(), moveset);

		return true;
	}

	public static void updateMoveset(String typeName, Set<Move> moves) {
		Set<Move> moveset = MOVESETS.get(typeName);

		if(moveset == null) return;

		moveset.addAll(moves);
	}

	public static Set<Move> removeMoveset(String typeName) {
		if(Arrays.stream(PieceType.values()).map(PieceType::name).toList().contains(typeName)) return null;

		return MOVESETS.remove(typeName);
	}

	public static Set<Move> getMoveset(String typeName) {
		return MOVESETS.get(typeName);
	}
}
