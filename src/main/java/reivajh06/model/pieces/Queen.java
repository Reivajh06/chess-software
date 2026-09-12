package reivajh06.model.pieces;

import reivajh06.model.Tile;

import java.util.List;

public class Queen extends Piece {

	public Queen(PieceColor color) {
		super(color);
		name = "Queen";
	}

	@Override
	public List<Tile> possibleMoves(Tile currentTile) {
		return List.of();
	}
}
