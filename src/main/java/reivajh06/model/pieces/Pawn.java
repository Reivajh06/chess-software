package reivajh06.model.pieces;

import reivajh06.model.Tile;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

	private boolean startingMove;

	public Pawn(PieceColor color) {
		super(color);
		name = "Pawn";
		startingMove = true;
	}

	public void finishStartMove() {
		startingMove = false;
	}

	@Override
	public List<Tile> possibleMoves(Tile currentTile) {
		List<Tile> moveset = new ArrayList<>();

		moveset.add(new Tile(currentTile.col(), currentTile.row() + movementFactor));

		if(startingMove) moveset.add(new Tile(currentTile.col(), currentTile.row() + movementFactor * 2));

		return moveset;
	}
}
