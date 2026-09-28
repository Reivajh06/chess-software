package software.ulpgc.model;

import software.ulpgc.model.movement.MoveBuilder;
import software.ulpgc.model.movement.MoveEffect;
import software.ulpgc.model.movement.MoveRules;

import java.util.ArrayList;
import java.util.List;

public class PieceFactory {

	public static Piece createPawn(PieceColor color) {
		return new Piece(PieceType.PAWN, color, new ArrayList<>(List.of(
				new MoveBuilder().forward().conditions(MoveRules.DESTINATION_EMPTY).build(),
				new MoveBuilder().forward().forward().conditions(MoveRules.START_TWO_FORWARD).build(),
				new MoveBuilder().forwardLeft().conditions(MoveRules.DIAGONAL_CAPTURE).effect(MoveEffect.DIRECT_CAPTURE).build(),
				new MoveBuilder().forwardLeft().conditions(MoveRules.EN_PASSANT).effect(MoveEffect.EN_PASSANT_CAPTURE).build(),
				new MoveBuilder().forwardRight().conditions(MoveRules.DIAGONAL_CAPTURE).effect(MoveEffect.DIRECT_CAPTURE).build(),
				new MoveBuilder().forwardRight().conditions(MoveRules.EN_PASSANT).effect(MoveEffect.EN_PASSANT_CAPTURE).build()
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
				new MoveBuilder().forward().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().left().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().right().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().backward().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().forwardLeft().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().forwardRight().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().backwardLeft().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().backwardRight().conditions(MoveRules.SAFE_MOVE).build(),
				new MoveBuilder().left().left().conditions(MoveRules.QUEENSIDE_CASTLING).effect(MoveEffect.QUEENSIDE_CASTLING).build(),
				new MoveBuilder().right().right().conditions(MoveRules.KINGSIDE_CASTLING).effect(MoveEffect.KINGSIDE_CASTLING).build()
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
