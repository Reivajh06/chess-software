package reivajh06.model;

import org.jetbrains.annotations.NotNull;
import reivajh06.Sprites;
import reivajh06.model.movement.Move;

import java.util.List;

public class Piece {

	private PieceColor color;
	private final PieceType type;
	private final String name;
	private List<Move> moveset;
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

	@NotNull
	@Override
	public String toString() {
		return Sprites.sprite(code());
	}
}
