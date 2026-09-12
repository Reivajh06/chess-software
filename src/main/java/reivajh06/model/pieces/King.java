package reivajh06.model.pieces;

import reivajh06.model.Board;
import reivajh06.model.Tile;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

	public King(PieceColor color) {
		super(color);
		name = "King";
	}

	@Override
	public List<Tile> possibleMoves(Tile currentTile) {
		List<Tile> moveset = new ArrayList<>();

		//Forward
		moveset.add(new Tile(currentTile.col(), currentTile.row() + movementFactor));

		//Backward
		moveset.add(new Tile(currentTile.col(), currentTile.row() - movementFactor));

		//Left
		moveset.add(new Tile(Character.toString(Board.columnCode(currentTile.col().charAt(0)) - movementFactor), currentTile.row()));

		//Right
		moveset.add(new Tile(Character.toString(Board.columnCode(currentTile.col().charAt(0)) - movementFactor), currentTile.row()));

		//Left forward
		moveset.add(new Tile(Character.toString(Board.columnCode(currentTile.col().charAt(0)) - movementFactor), currentTile.row() + movementFactor));

		//Right forward
		moveset.add(new Tile(Character.toString(Board.columnCode(currentTile.col().charAt(0)) - movementFactor), currentTile.row() + movementFactor));

		//Left backward
		moveset.add(new Tile(Character.toString(Board.columnCode(currentTile.col().charAt(0)) - movementFactor), currentTile.row() - movementFactor));

		//Right backward
		moveset.add(new Tile(Character.toString(Board.columnCode(currentTile.col().charAt(0)) - movementFactor), currentTile.row() - movementFactor));

		return moveset;
	}
}
