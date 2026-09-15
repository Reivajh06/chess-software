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

	public boolean hasPiece(Tile tile) {
		return pieceAt(tile) != null;
	}

	public List<Tile> tilesWithPieces(PieceColor color) {
		return tiles.entrySet().stream()
				.filter(e -> {
					Piece p = e.getValue();

					return p != null && p.color() == color;
				})
				.map(Map.Entry::getKey)
				.toList();
	}

	public List<Tile> possiblePieceMoves(Tile from) {
		//TODO refactor this and add pawn conditions
		Piece piece = pieceAt(from);
		List<Tile> possibleTiles = new ArrayList<>();
		List<Move> moveset = piece.moveset();
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		for(Move move : moveset) {
			int n = 0;

			int i = (from.col().code + move.x() * movementFactor) - File.A.code;

			if(i < 0 || i >= 8) continue;

			Tile to = new Tile(
					File.values()[i % 8],
					from.row() + move.y() * movementFactor
			);

			while(n < move.repetitions() && validTile(to)) {
				Piece p = pieceAt(to);

				if(p != null) {
					if(p.color() == piece.color()) break;

				} else if(piece instanceof Pawn && isDiagonalMove(from, to)) break;

				if(hasPiece(to) && pieceAt(to).color() == piece.color()) break;

				possibleTiles.add(to);
				n++;

				if(n >= move.repetitions()) break;

				i = ((to.col().code + move.x() * movementFactor) - File.A.code);

				if(i < 0 || i >= 8) break;

				to = new Tile(
						File.values()[i % 8],
						to.row() + move.y() * movementFactor
				);
			}
		}

		return possibleTiles;
	}

	private boolean isDiagonalMove(Tile from, Tile to) {
		int xOffset = Math.abs(from.row() - to.row());
		int yOffset = Math.abs(from.col().code - to.col().code);

		return (xOffset == 1) && (yOffset == 1);
	}

	private boolean validTile(Tile t) {
		return t.col().code <= File.H.code &&
				t.col().code >= File.A.code &&
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
			tiles.put(new Tile(File.values()[i], pawnRow), new Pawn(color));
		}

		tiles.put(new Tile(File.A, kingRow), new Rook(color));
		tiles.put(new Tile(File.B, kingRow), new Knight(color));
		tiles.put(new Tile(File.C, kingRow), new Bishop(color));
		tiles.put(new Tile(File.D, kingRow), new Queen(color));
		tiles.put(new Tile(File.E, kingRow), new King(color));
		tiles.put(new Tile(File.F, kingRow), new Bishop(color));
		tiles.put(new Tile(File.G, kingRow), new Knight(color));
		tiles.put(new Tile(File.H, kingRow), new Rook(color));
	}

	private void fillMiddle() {
		for(int i = 3; i <= 6; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Tile(File.values()[j], i), null);
			}
		}
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(File.values()[j], i));
				row.append(p == null ? ".." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
