package reivajh06.model.pieces;

import reivajh06.model.Tile;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

	public Bishop(PieceColor color) {
		super(color);
		name = "Bishop";
	}

	@Override
	public List<Tile> possibleMoves(Tile currentTile) {
		List<Tile> moveset = new ArrayList<>();

		return moveset;
	}

	private List<Tile> upperLeftDiagonal(Tile currentTile) {
	 return List.of();
	}
}
