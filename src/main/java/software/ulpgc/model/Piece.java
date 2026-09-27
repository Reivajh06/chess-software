package software.ulpgc.model;

import org.jetbrains.annotations.NotNull;
import software.ulpgc.SpritesRepository;
import software.ulpgc.model.movement.Move;

import java.util.List;

public class Piece {

	private final PieceColor color;
	private PieceType type;
	private final String name;
	private final List<Move> moveset;
	private boolean isPromotable = false;

	private int timesMoved;

	public Piece(PieceType type, PieceColor color, List<Move> moveset) {
		this.type = type;
		this.color = color;
		name = color.name().charAt(0) + type.name().substring(0, 1);
		this.moveset = moveset;
	}

	public String code() {
		return color.name().charAt(0) + type.name().substring(0, 2);
	}

	public PieceColor color() {
		return color;
	}

	public String name() {
		return name;
	}

	public PieceType type() {
		return type;
	}

	public List<Move> moveset() {
		return moveset;
	}

	public void moved() {
		timesMoved++;
	}

	public int timesMoved() {
		return timesMoved;
	}

	public boolean isPromotable() {
		return isPromotable;
	}

	public void enablePromotion() {
		isPromotable = true;
	}

	public void promote(PieceType newType) {
		if(!isPromotable || newType == PieceType.KING) return;

		type = newType;
		isPromotable = false;
	}

	@NotNull
	@Override
	public String toString() {
		return SpritesRepository.sprite(code());
	}
}
