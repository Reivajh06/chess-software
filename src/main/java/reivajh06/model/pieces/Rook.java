package reivajh06.model.pieces;

import reivajh06.model.Board;
import reivajh06.model.Tile;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

	public Rook(PieceColor color) {
		super(color);
		name = "Rook";
	}

	@Override
	public List<Tile> possibleMoves(Tile currentTile) {
		List<Tile> moveset = new ArrayList<>();

		//Forward
		for(int row = currentTile.row(); row <= 8 && row > 0; row += movementFactor) {
			moveset.add(new Tile(currentTile.col(), row));
		}

		//Backward
		for(int row = currentTile.row(); row <= 8 && row > 0; row -= movementFactor) {
			moveset.add(new Tile(currentTile.col(), row));
		}

		//Left
		for(int col = Board.columnCode(currentTile.col().charAt(0)); col >= Board.columnCode('a') && col <= Board.columnCode('h'); col -= movementFactor) {
			moveset.add(new Tile(Character.toString(col), currentTile.row()));
		}

		//Right
		for(int col = Board.columnCode(currentTile.col().charAt(0)); col >= Board.columnCode('a') && col <= Board.columnCode('h'); col += movementFactor) {
			moveset.add(new Tile(Character.toString(col), currentTile.row()));
		}

		return moveset;
	}
}
