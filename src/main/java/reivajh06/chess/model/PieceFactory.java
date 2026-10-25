package reivajh06.chess.model;

import reivajh06.chess.model.movement.MoveBuilder;
import reivajh06.chess.model.movement.MoveEffect;
import reivajh06.chess.model.movement.MoveRules;

import java.util.ArrayList;
import java.util.List;

public class PieceFactory {

	public static Piece createPawn(Color color) {
		return new Piece(PieceType.PAWN, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().condition(MoveRules.DESTINATION_EMPTY).effect(MoveEffect.NO_CAPTURE).build(),
				new MoveBuilder().forward().forward().condition(MoveRules.START_TWO_FORWARD).effect(MoveEffect.NO_CAPTURE).build(),
				new MoveBuilder().forwardLeft().condition(MoveRules.DIAGONAL_CAPTURE).effect(MoveEffect.DIRECT_CAPTURE).build(),
				new MoveBuilder().forwardLeft().condition(MoveRules.EN_PASSANT).effect(MoveEffect.EN_PASSANT_CAPTURE).build(),
				new MoveBuilder().forwardRight().condition(MoveRules.DIAGONAL_CAPTURE).effect(MoveEffect.DIRECT_CAPTURE).build(),
				new MoveBuilder().forwardRight().condition(MoveRules.EN_PASSANT).effect(MoveEffect.EN_PASSANT_CAPTURE).build()
		)));
	}

	public static Piece createBishop(Color color) {
		return new Piece(PieceType.BISHOP, color, new ArrayList<>(List.of(
				new MoveBuilder().forwardLeft().allRepetitions().build(),
				new MoveBuilder().forwardRight().allRepetitions().build(),
				new MoveBuilder().backwardLeft().allRepetitions().build(),
				new MoveBuilder().backwardRight().allRepetitions().build()
		)));
	}

	public static Piece createRook(Color color) {
		return new Piece(PieceType.ROOK, color, new ArrayList<>(List.of(
				new MoveBuilder().left().allRepetitions().build(),
				new MoveBuilder().right().allRepetitions().build(),
				new MoveBuilder().forward().allRepetitions().build(),
				new MoveBuilder().backward().allRepetitions().build()
		)));
	}

	public static Piece createKing(Color color) {
		return new Piece(PieceType.KING, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().build(),
				new MoveBuilder().left().build(),
				new MoveBuilder().right().build(),
				new MoveBuilder().backward().build(),
				new MoveBuilder().forwardLeft().build(),
				new MoveBuilder().forwardRight().build(),
				new MoveBuilder().backwardLeft().build(),
				new MoveBuilder().backwardRight().build(),
				new MoveBuilder().left().left().condition(MoveRules.QUEENSIDE_CASTLING).effect(MoveEffect.QUEENSIDE_CASTLING).build(),
				new MoveBuilder().right().right().condition(MoveRules.KINGSIDE_CASTLING).effect(MoveEffect.KINGSIDE_CASTLING).build()
		)));
	}

	public static Piece createQueen(Color color) {
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

	public static Piece createKnight(Color color) {
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
			case 'P' -> createPawn(Color.WHITE);
			case 'B' -> createBishop(Color.WHITE);
			case 'R' -> createRook(Color.WHITE);
			case 'K' -> createKing(Color.WHITE);
			case 'Q' -> createQueen(Color.WHITE);
			case 'N' -> createKnight(Color.WHITE);
			case 'p' -> createPawn(Color.BLACK);
			case 'b' -> createBishop(Color.BLACK);
			case 'r' -> createRook(Color.BLACK);
			case 'k' -> createKing(Color.BLACK);
			case 'q' -> createQueen(Color.BLACK);
			case 'n' -> createKnight(Color.BLACK);
			default -> null;
		};
	}
}
