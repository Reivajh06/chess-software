package reivajh06.model;

import org.jetbrains.annotations.NotNull;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.Moveset;

import java.util.List;

public class Piece {

	private PieceColor color;
	private PieceType type;
	private List<Move> moveset;

	public Piece(PieceType type, PieceColor color) {
		this.type = type;
		this.color = color;
		setMoveset(Moveset.classicMoveset(type));
	}

	public String getCode() {
		return String.valueOf(color.name().charAt(0)) + type.name().substring(0, 2);
	}

	public PieceColor color() {
		return color;
	}

	public String name() {
		return color.name().charAt(0) + type.name().substring(0, 1);
	}

	public PieceType type() {
		return type;
	}

	public List<Move> moveset() {
		return moveset;
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
