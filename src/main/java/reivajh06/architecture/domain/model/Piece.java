package reivajh06.architecture.domain.model;

import org.jetbrains.annotations.NotNull;
import reivajh06.architecture.domain.services.Sprites;
import reivajh06.architecture.domain.model.movement.Move;

import java.util.List;

public class Piece {

	private final Color color;
	private final PieceType type;
	private final String name;
	private final String fenCode;
	private final List<Move> moveset;
	private final boolean isPromotable;

	private int timesMoved;

	public Piece(PieceType type, Color color, List<Move> moveset) {
		this.type = type;
		this.color = color;
		name = color.name().charAt(0) + type.firstLetter;
		this.moveset = moveset;
		isPromotable = type == PieceType.PAWN;
		fenCode = color == Color.BLACK ? type.firstLetter.toLowerCase() : type.firstLetter;
	}

	public Piece(PieceType type, Color color, List<Move> moveset, int timesMoved) {
		this(type, color, moveset);
		this.timesMoved = timesMoved;
	}

	public Color color() {
		return color;
	}

	public String name() {
		return name;
	}

	public PieceType type() {
		return type;
	}

	public String fenCode() {
		return fenCode;
	}

	public List<Move> moveset() {
		return moveset;
	}

	public void moved() {
		timesMoved++;
	}

	public void unmoved() {
		timesMoved = Math.max(0, timesMoved - 1);
	}

	public int timesMoved() {
		return timesMoved;
	}

	public boolean isPromotable() {
		return isPromotable;
	}

	@NotNull
	@Override
	public String toString() {
		return Sprites.sprite(name());
	}

	public Piece copy() {
		return new Piece(type, color, moveset, timesMoved);
	}
}
