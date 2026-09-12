package reivajh06.model.pieces;

import org.jetbrains.annotations.NotNull;
import reivajh06.model.Tile;

import java.util.List;

public abstract class Piece {

	protected PieceColor color;
	protected String name;
	protected int movementFactor;

	public Piece(PieceColor color) {
		this.color = color;
		movementFactor = color == PieceColor.BLACK ? -1 : 1;
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

	public abstract List<Tile> possibleMoves(Tile currentTile);

	@NotNull
	@Override
	public String toString() {
		return getCode();
	}
}
