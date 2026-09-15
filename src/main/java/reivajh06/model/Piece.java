package reivajh06.model;

import org.jetbrains.annotations.NotNull;
import reivajh06.model.movement.Move;

import java.util.List;

public class Piece {

	private PieceColor color;
	private PieceType type;
	private String name;
	private List<Move> moveset;

	public Piece(PieceColor color) {
		this.color = color;
	}

	public String getCode() {
		return String.valueOf(color.name().charAt(0)) + name().charAt(0);
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

	public void setMoveset(List<Move> moveset) {
		this.moveset = moveset;
	}

	@NotNull
	@Override
	public String toString() {
		return getCode();
	}
}
