package reivajh06.model;

import reivajh06.model.movement.MoveBuilder;
import reivajh06.model.movement.MoveEffect;
import reivajh06.model.movement.MoveRules;

import java.util.ArrayList;
import java.util.List;

public class PieceFactory {

	public static Piece createPawn(PieceColor color) {
		return new Piece(PieceType.PAWN, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().condition(MoveRules.DESTINATION_EMPTY).build(),
				new MoveBuilder().forward().forward().condition(MoveRules.START_TWO_FORWARD).build(),
				new MoveBuilder().forwardLeft().condition(MoveRules.DIAGONAL_CAPTURE).effect(MoveEffect.DIRECT_CAPTURE).build(),
				new MoveBuilder().forwardLeft().condition(MoveRules.EN_PASSANT).effect(MoveEffect.EN_PASSANT_CAPTURE).build(),
				new MoveBuilder().forwardRight().condition(MoveRules.DIAGONAL_CAPTURE).effect(MoveEffect.DIRECT_CAPTURE).build(),
				new MoveBuilder().forwardRight().condition(MoveRules.EN_PASSANT).effect(MoveEffect.EN_PASSANT_CAPTURE).build()
		)));
	}

	public static Piece createBishop(PieceColor color) {
		return new Piece(PieceType.BISHOP, color, new ArrayList<>(List.of(
				new MoveBuilder().forwardLeft().allRepetitions().build(),
				new MoveBuilder().forwardRight().allRepetitions().build(),
				new MoveBuilder().backwardLeft().allRepetitions().build(),
				new MoveBuilder().backwardRight().allRepetitions().build()
		)));
	}

	public static Piece createRook(PieceColor color) {
		return new Piece(PieceType.ROOK, color, new ArrayList<>(List.of(
				new MoveBuilder().left().allRepetitions().build(),
				new MoveBuilder().right().allRepetitions().build(),
				new MoveBuilder().forward().allRepetitions().build(),
				new MoveBuilder().backward().allRepetitions().build()
		)));
	}

	public static Piece createKing(PieceColor color) {
		return new Piece(PieceType.KING, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().left().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().right().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().backward().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().forwardLeft().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().forwardRight().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().backwardLeft().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().backwardRight().condition(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().left().left().condition(MoveRules.QUEENSIDE_CASTLING).effect(MoveEffect.QUEENSIDE_CASTLING).build(),
				new MoveBuilder().right().right().condition(MoveRules.KINGSIDE_CASTLING).effect(MoveEffect.KINGSIDE_CASTLING).build()
		)));
	}

	public static Piece createQueen(PieceColor color) {
		return new Piece(PieceType.QUEEN, color, new ArrayList<>(List.of(
				new MoveBuilder().left().allRepetitions().build(),
				new MoveBuilder().right().allRepetitions().build(),
				new MoveBuilder().forward().allRepetitions().build(),
				new MoveBuilder().backward().allRepetitions().build(),
				new MoveBuilder().forwardLeft().allRepetitions().build(),
				new MoveBuilder().forwardRight().allRepetitions().build(),
				new MoveBuilder().backwardLeft().allRepetitions().build(),
				new MoveBuilder().backwardRight().allRepetitions().build()
		)));
	}

	public static Piece createKnight(PieceColor color) {
		return new Piece(PieceType.KNIGHT, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().forward().left().build(),
				new MoveBuilder().forward().forward().right().build(),
				new MoveBuilder().forward().left().left().build(),
				new MoveBuilder().forward().right().right().build(),
				new MoveBuilder().backward().right().right().build(),
				new MoveBuilder().backward().left().left().build(),
				new MoveBuilder().backward().backward().right().build(),
				new MoveBuilder().backward().backward().left().build()
		)));
	}

	public static Piece createPieceByFENCode(char fenCode) {
		return switch(fenCode) {
			case 'P' -> createPawn(PieceColor.WHITE);
			case 'B' -> createBishop(PieceColor.WHITE);
			case 'R' -> createRook(PieceColor.WHITE);
			case 'K' -> createKing(PieceColor.WHITE);
			case 'Q' -> createQueen(PieceColor.WHITE);
			case 'N' -> createKnight(PieceColor.WHITE);
			case 'p' -> createPawn(PieceColor.BLACK);
			case 'b' -> createBishop(PieceColor.BLACK);
			case 'r' -> createRook(PieceColor.BLACK);
			case 'k' -> createKing(PieceColor.BLACK);
			case 'q' -> createQueen(PieceColor.BLACK);
			case 'n' -> createKnight(PieceColor.BLACK);
			default -> null;
		};
	}
}
