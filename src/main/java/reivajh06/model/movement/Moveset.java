package reivajh06.model.movement;

import reivajh06.model.PieceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Moveset {

	public static final List<Move> PAWN = List.of(
			new MoveBuilder().forward().build(),
			new MoveBuilder().forwardLeft().build(),
			new MoveBuilder().forwardRight().build()
	);

	public static final List<Move> ROOK = List.of(
			new MoveBuilder().forward().allRepetitions().build(),
			new MoveBuilder().backward().allRepetitions().build(),
			new MoveBuilder().left().allRepetitions().build(),
			new MoveBuilder().right().allRepetitions().build()
	);

	public static final List<Move> KING = List.of(
			new MoveBuilder().forward().build(),
			new MoveBuilder().backward().build(),
			new MoveBuilder().left().build(),
			new MoveBuilder().right().build(),
			new MoveBuilder().forwardLeft().build(),
			new MoveBuilder().forwardRight().build(),
			new MoveBuilder().backwardLeft().build(),
			new MoveBuilder().backwardRight().build()
	);

	public static final List<Move> QUEEN = List.of(
			new MoveBuilder().forward().allRepetitions().build(),
			new MoveBuilder().backward().allRepetitions().build(),
			new MoveBuilder().left().allRepetitions().build(),
			new MoveBuilder().right().allRepetitions().build(),
			new MoveBuilder().forwardLeft().allRepetitions().build(),
			new MoveBuilder().forwardRight().allRepetitions().build(),
			new MoveBuilder().backwardLeft().allRepetitions().build(),
			new MoveBuilder().backwardRight().allRepetitions().build()
	);

	public static final List<Move> BISHOP = List.of(
			new MoveBuilder().forwardLeft().allRepetitions().build(),
			new MoveBuilder().forwardRight().allRepetitions().build(),
			new MoveBuilder().backwardLeft().allRepetitions().build(),
			new MoveBuilder().backwardRight().allRepetitions().build()
	);

	public static final List<Move> KNIGHT = List.of(
			new MoveBuilder().forward().forward().left().build(),
			new MoveBuilder().forward().forward().right().build(),
			new MoveBuilder().backward().backward().left().build(),
			new MoveBuilder().backward().backward().right().build(),
			new MoveBuilder().forward().left().left().build(),
			new MoveBuilder().forward().right().right().build(),
			new MoveBuilder().backward().left().left().build(),
			new MoveBuilder().backward().right().right().build()
	);

	private static final Map<String, List<Move>> CUSTOM_MOVESETS = new HashMap<>();
	private static final Map<PieceType, List<Move>> CLASSIC_MOVESETS = new HashMap<>(Map.of(
			PieceType.PAWN, PAWN,
			PieceType.ROOK, ROOK,
			PieceType.BISHOP, BISHOP,
			PieceType.KNIGHT, KNIGHT,
			PieceType.QUEEN, QUEEN,
			PieceType.KING, KING
			));

	public static List<Move> customMoveset(String name) {
		return CUSTOM_MOVESETS.get(name);
	}

	public static void addCustomMoveset(String name, List<Move> moveset) {
		CUSTOM_MOVESETS.put(name, moveset);
	}

	public static List<Move> removeMoveset(String name) {
		return CUSTOM_MOVESETS.remove(name);
	}

	public static List<Move> classicMoveset(PieceType type) {
		return CLASSIC_MOVESETS.get(type);
	}
}
