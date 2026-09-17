package reivajh06.model;

import reivajh06.model.movement.Move;
import reivajh06.model.movement.Moveset;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameController {

	private final Board board;

	public GameController gameController;

	public GameController(Board board) {
		this.board = board;
	}

	public void setupBoard() {
		allocatePieces(board, PieceColor.BLACK);
		allocatePieces(board, PieceColor.WHITE);
	}

	private void allocatePieces(Board board, PieceColor color) {
		int pawnRow = 2;
		int kingRow = 1;

		if(color == PieceColor.BLACK) {
			pawnRow = 7;
			kingRow = 8;
		}

		for(int i = 0; i < 8; i++) {
			board.addPiece(new Tile(File.values()[i], Rank.values()[pawnRow - 1]), new Piece(PieceType.PAWN, color));
		}

		board.addPiece(new Tile(File.A, Rank.values()[kingRow - 1]), new Piece(PieceType.ROOK, color));
		board.addPiece(new Tile(File.B, Rank.values()[kingRow - 1]), new Piece(PieceType.KNIGHT, color));
		board.addPiece(new Tile(File.C, Rank.values()[kingRow - 1]), new Piece(PieceType.BISHOP, color));
		board.addPiece(new Tile(File.D, Rank.values()[kingRow - 1]), new Piece(PieceType.QUEEN, color));
		board.addPiece(new Tile(File.E, Rank.values()[kingRow - 1]), new Piece(PieceType.KING, color));
		board.addPiece(new Tile(File.F, Rank.values()[kingRow - 1]), new Piece(PieceType.BISHOP, color));
		board.addPiece(new Tile(File.G, Rank.values()[kingRow - 1]), new Piece(PieceType.KNIGHT, color));
		board.addPiece(new Tile(File.H, Rank.values()[kingRow - 1]), new Piece(PieceType.ROOK, color));
	}

	public boolean movePiece(Tile from, Tile to, BiConsumer<Tile, Tile> onValidMove) {
		Piece p = board.pieceAt(from);
		Piece pieceEaten = board.pieceAt(to);

		List<Tile> validTiles = movesetTiles(from);

		if(!validTiles.contains(to)) return false;

		onValidMove.accept(from, to);
		board.move(from, to);

		return true;
	}

	public List<Tile> movesetTiles(Tile tile) {
		Piece piece = board.pieceAt(tile);
		List<Tile> possibleTiles = new ArrayList<>();
		List<Move> moveset = piece.moveset();
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		for(Move move : moveset) {
			int n = 0;

			int i = (tile.file().code + move.x() * movementFactor) - File.A.code;

			if(i < 0 || i >= 8) continue;

			Tile t = new Tile(
					File.values()[i % 8],
					Rank.values()[tile.rank().number + move.y() * movementFactor]
			);

			if(board.hasPiece(t) && board.pieceAt(t).color() == piece.color()) continue;

			while(n < move.repetitions() && validTile(t)) {
				if(board.hasPiece(t) && board.pieceAt(t).color() == piece.color()) break;

				possibleTiles.add(t);
				n++;

				if(n >= move.repetitions()) break;

				i = ((t.file().code + move.x() * movementFactor) - File.A.code);

				if(i < 0 || i >= 8) break;

				t = new Tile(
						File.values()[i % 8],
						Rank.values()[t.rank().number + move.y() * movementFactor]
				);
			}
		}

		return possibleTiles;
	}

	private boolean isDiagonalMove(Tile from, Tile to) {
		int xOffset = Math.abs(from.rank().number - to.rank().number);
		int yOffset = Math.abs(from.file().code - to.file().code);

		return (xOffset == 1) && (yOffset == 1);
	}

	private boolean validTile(Tile t) {
		return t.file().code <= File.H.code &&
				t.file().code >= File.A.code &&
				t.rank().number <= 8 &&
				t.rank().number > 0;
	}
}
