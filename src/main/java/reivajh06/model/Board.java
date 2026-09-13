package reivajh06.model;

import reivajh06.model.movement.Move;
import reivajh06.model.pieces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

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

	public List<Tile> possiblePieceMoves(Tile tile) {
		//TODO refactor this method so it gets way cleaner than this
		Piece piece = pieceAt(tile);
		List<Tile> possibleTiles = new ArrayList<>();
		List<Move> moveset = piece.moveset();
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		for(Move move : moveset) {
			int n = 0;

			int i = (tile.col().code + move.x() * movementFactor) - BoardColumns.A.code;

			if(i < 0 || i >= 8) continue;

			Tile t = new Tile(
					BoardColumns.values()[i % 8],
					tile.row() + move.y() * movementFactor
			);

			while(n < move.repetitions() && validTile(t)) {
				possibleTiles.add(t);
				n++;

				if(n >= move.repetitions()) break;

				i = ((t.col().code + move.x() * movementFactor) - BoardColumns.A.code);

				if(i < 0 || i >= 8) break;

				t = new Tile(
						BoardColumns.values()[i % 8],
						t.row() + move.y() * movementFactor
				);
			}
		}

		return possibleTiles;
	}

	private boolean validTile(Tile t) {
		return t.col().code <= BoardColumns.H.code &&
				t.col().code >= BoardColumns.A.code &&
				t.row() <= 8 &&
				t.row() > 0;
	}

	private void allocatePieces(PieceColor color) {
		int pawnRow = 2;
		int kingRow = 1;

		if(color == PieceColor.BLACK) {
			pawnRow = 7;
			kingRow = 8;
		}

		for(int i = 0; i < 8; i++) {						//Unicode based
			tiles.put(new Tile(BoardColumns.values()[i], pawnRow), new Pawn(color));
		}

		tiles.put(new Tile(BoardColumns.A, kingRow), new Rook(color));
		tiles.put(new Tile(BoardColumns.B, kingRow), new Knight(color));
		tiles.put(new Tile(BoardColumns.C, kingRow), new Bishop(color));
		tiles.put(new Tile(BoardColumns.D, kingRow), new Queen(color));
		tiles.put(new Tile(BoardColumns.E, kingRow), new King(color));
		tiles.put(new Tile(BoardColumns.F, kingRow), new Bishop(color));
		tiles.put(new Tile(BoardColumns.G, kingRow), new Knight(color));
		tiles.put(new Tile(BoardColumns.H, kingRow), new Rook(color));
	}

	private void fillMiddle() {
		for(int i = 3; i <= 6; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Tile(BoardColumns.values()[j], i), null);
			}
		}
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(BoardColumns.values()[j], i));
				row.append(p == null ? ".." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
