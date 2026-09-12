package reivajh06.model.pieces;

import reivajh06.model.Tile;

import java.util.List;

public class Knight extends Piece {

	public Knight(PieceColor color) {
		super(color);
		name = "Knight";
	}

	@Override
	public String name() {
		return name.substring(0, 2);
	}

	@Override
	public List<Tile> possibleMoves(Tile currentTile) {
		return List.of();
	}

	@Override
	public String getCode() {
		return color.name().charAt(0) + name();
	}
}
