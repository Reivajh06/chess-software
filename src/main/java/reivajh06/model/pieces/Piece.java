package reivajh06.model.pieces;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Piece {

	protected PieceColor color;
	protected String name;

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

	public abstract List<String> possibleMoves(String currentTile);

	@NotNull
	@Override
	public String toString() {
		return getCode();
	}
}
