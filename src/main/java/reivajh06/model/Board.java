package reivajh06.model;

import reivajh06.model.movement.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

	private final Map<Square, Piece> tiles;

	public Board() {
		tiles = new HashMap<>();

		allocatePieces(PieceColor.BLACK);
		fillMiddle();
		allocatePieces(PieceColor.WHITE);
	}

	public Piece move(Square from, Square to) {
		if(tiles.get(from) == null) return null;

		return tiles.replace(to, tiles.remove(from));
	}

	public Piece pieceAt(Square square) {
		return tiles.get(square);
	}

	public boolean hasPiece(Square square) {
		return pieceAt(square) != null;
	}

	public List<Square> possiblePieceMoves(Square square) {
		//TODO refactor this and add pawn conditions
		Piece piece = pieceAt(square);
		List<Square> possibleSquares = new ArrayList<>();
		List<Move> moveset = piece.moveset();
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		for(Move move : moveset) {
			int n = 0;

			int i = (square.col().code + move.x() * movementFactor) - BoardColumns.A.code;

			if(i < 0 || i >= 8) continue;

			Square t = new Square(
					BoardColumns.values()[i % 8],
					square.row() + move.y() * movementFactor
			);

			if(hasPiece(t) && pieceAt(t).color() == piece.color()) continue;

			while(n < move.repetitions() && validTile(t)) {
				if(hasPiece(t) && pieceAt(t).color() == piece.color()) break;

				possibleSquares.add(t);
				n++;

				if(n >= move.repetitions()) break;

				i = ((t.col().code + move.x() * movementFactor) - BoardColumns.A.code);

				if(i < 0 || i >= 8) break;

				t = new Square(
						BoardColumns.values()[i % 8],
						t.row() + move.y() * movementFactor
				);
			}
		}

		return possibleSquares;
	}

	private boolean isDiagonalMove(Square from, Square to) {
		int xOffset = Math.abs(from.row() - to.row());
		int yOffset = Math.abs(from.col().code - to.col().code);

		return (xOffset == 1) && (yOffset == 1);
	}

	private boolean validTile(Square t) {
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
			tiles.put(new Square(BoardColumns.values()[i], pawnRow), new Pawn(color));
		}

		tiles.put(new Square(BoardColumns.A, kingRow), new Rook(color));
		tiles.put(new Square(BoardColumns.B, kingRow), new Knight(color));
		tiles.put(new Square(BoardColumns.C, kingRow), new Bishop(color));
		tiles.put(new Square(BoardColumns.D, kingRow), new Queen(color));
		tiles.put(new Square(BoardColumns.E, kingRow), new King(color));
		tiles.put(new Square(BoardColumns.F, kingRow), new Bishop(color));
		tiles.put(new Square(BoardColumns.G, kingRow), new Knight(color));
		tiles.put(new Square(BoardColumns.H, kingRow), new Rook(color));
	}

	private void fillMiddle() {
		for(int i = 3; i <= 6; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Square(BoardColumns.values()[j], i), null);
			}
		}
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Square(BoardColumns.values()[j], i));
				row.append(p == null ? ".." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}
}
