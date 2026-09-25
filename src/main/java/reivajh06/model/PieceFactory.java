package reivajh06.model;

import reivajh06.gamelogic.MovesValidator;
import reivajh06.model.movement.MoveBuilder;
import reivajh06.model.movement.MoveEffect;

import java.util.ArrayList;
import java.util.List;

public class PieceFactory {

	public static Piece createPawn(PieceColor color) {
		return new Piece(PieceType.PAWN, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().build(),
				new MoveBuilder().forward().forward().conditions(MovesValidator.START_TWO_FORWARD).build(),
				new MoveBuilder().forwardLeft().conditions(MovesValidator.DIAGONAL_CAPTURE, MovesValidator.EN_PASSANT).effect(MoveEffect.PAWN_DIAGONAL_CAPTURE).build(),
				new MoveBuilder().forwardRight().conditions(MovesValidator.DIAGONAL_CAPTURE, MovesValidator.EN_PASSANT).effect(MoveEffect.PAWN_DIAGONAL_CAPTURE).build()
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
				new MoveBuilder().forward().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().left().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().right().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().backward().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().forwardLeft().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().forwardRight().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().backwardLeft().conditions(MovesValidator.SAFE_MOVE).build(),
				new MoveBuilder().backwardRight().conditions(MovesValidator.SAFE_MOVE).build()
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
}
