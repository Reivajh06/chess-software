package reivajh06.model;

import org.jetbrains.annotations.NotNull;
import reivajh06.model.movement.Move;

import java.util.List;

public class Piece {

	private PieceColor color;
	private final String typeName;
	private final String name;
	private List<Move> moveset;
	private int timesMoved;

	public Piece(String typeName, PieceColor color, List<Move> moveset) {
		this.typeName = typeName.trim().toUpperCase();
		this.color = color;
		name = color.name().charAt(0) + typeName.substring(0, 1);
		this.moveset = moveset;
	}

	public Piece(PieceType type, PieceColor color, List<Move> moveset) {
		this(type.name().toLowerCase(), color, moveset);
	}

	public String getCode() {
		return color.name().charAt(0) + typeName.substring(0, 2);
	}

	public PieceColor color() {
		return color;
	}

	public String name() {
		return name;
	}

	public String typeName() {
		return typeName;
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

	@NotNull
	@Override
	public String toString() {
		return getCode();
	}
}
