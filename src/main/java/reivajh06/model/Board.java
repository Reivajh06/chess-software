package reivajh06.model;

import reivajh06.model.movement.Move;
import reivajh06.model.pieces.*;

import java.util.*;
import java.util.stream.Stream;

public class Board {

	//TODO CHANGE THE CODIFICATION OF COLUMNS INTO AN ENUM.
	//TODO MAKE THE BOARD HAVE THE LOGIC OF GETTING THE TILES A PIECE CAN MOVE LIKE moveTiles(Tile from, Direction[] steps)
	//TODO MAKE AN ENUM DIRECTION ? BASED ON FORWARD BACKWARD LEFT AND RIGHT
	public static final int ALL_TILES = 8;


	private final Map<Tile, Piece> tiles;


	public Board() {
		tiles = new HashMap<>();

		allocatePieces(PieceColor.BLACK);
		fillMiddle();
		allocatePieces(PieceColor.WHITE);
	}

	public Piece move(Tile from, Tile to) {
		if(tiles.get(from) == null) return null;

		return tiles.replace(to, tiles.remove(from));
	}

	public Piece pieceAt(Tile tile) {
		return tiles.get(tile);
	}

	public Tile pieceLocation(Piece piece) {
		return tiles.entrySet().stream()
				.filter(e -> e.getValue().equals(piece))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}

	public List<Tile> possiblePieceMoves(Piece piece) {
		List<Tile> possibleTiles = new ArrayList<>();
		List<Move> moveset = piece.moveset();
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		Tile currentTile = pieceLocation(piece);

		for(Move move : moveset) {
			int n = 0;

			while(n < move.repetitions() && validTile(currentTile)) {

			}
		}

		return possibleTiles;
	}

	private boolean validTile(Tile t) {
		return true;
		/*return BoardColumns.valueOf(t.col().toUpperCase()).code < 8 &&
				BoardColumns.valueOf(t.col().toUpperCase()).code > 0 &&;
	*/}

	private void allocatePieces(PieceColor color) {
		int pawnRow = 2;
		int kingRow = 1;

		if(color == PieceColor.BLACK) {
			pawnRow = 7;
			kingRow = 8;
		}

		for(int i = 0; i < 8; i++) {						//Unicode based
			tiles.put(new Tile(Character.toString(BoardColumns.A.code + i), pawnRow), new Pawn(color));
		}

		tiles.put(new Tile("a", kingRow), new Rook(color));
		tiles.put(new Tile("b", kingRow), new Knight(color));
		tiles.put(new Tile("c", kingRow), new Bishop(color));
		tiles.put(new Tile("d", kingRow), new Queen(color));
		tiles.put(new Tile("e", kingRow), new King(color));
		tiles.put(new Tile("f", kingRow), new Bishop(color));
		tiles.put(new Tile("g", kingRow), new Knight(color));
		tiles.put(new Tile("h", kingRow), new Rook(color));
	}

	private void fillMiddle() {
		for(int i = 3; i <= 6; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Tile(Character.toString(BoardColumns.A.code + j), i), null);
			}
		}
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(Character.toString(BoardColumns.values()[j].code), i));
				row.append(p == null ? ".." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
