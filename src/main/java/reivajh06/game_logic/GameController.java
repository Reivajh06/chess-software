package reivajh06.game_logic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.Moveset;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class GameController {

	private final Board board;
	private PieceColor colorTurn = PieceColor.WHITE;

	public GameController(Board board) {
		this.board = board;
	}

	public void setupBoard() {
		allocatePieces(board, PieceColor.BLACK);
		allocatePieces(board, PieceColor.WHITE);
	}

	public PieceColor colorTurn() {
		return colorTurn;
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
		if(board.pieceAt(from).color() != colorTurn) return false;
		if(!board.hasPiece(from)) return false;

		List<Tile> validTiles = calculateMovesetTiles(from);

		if(!validTiles.contains(to)) return false;

		onValidMove.accept(from, to);
		board.removeFromTile(to);
		board.addPiece(to, board.removeFromTile(from));
		colorTurn = colorTurn == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

		return true;
	}

	//TODO remake this function. It has issues when calculating tiles
	public List<Tile> calculateMovesetTiles(Tile start) {
		Piece piece = board.pieceAt(start);
		List<Tile> possibleTiles = new ArrayList<>();
		Set<Move> moveset = Moveset.getMoveset(piece.typeName());
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		for(Move move : moveset) {
			int n = 0;

			int file = (start.file().code + move.x() * movementFactor) - File.A.code;

			if(file < 0 || file >= 8) continue;

			int rank = start.rank().number + move.y() * movementFactor - 1;

			if(rank < 0) continue;

			Tile destination = new Tile(
					File.values()[file % 8],
					Rank.values()[rank]
			);

			if(board.hasPiece(destination) && board.pieceAt(destination).color() == piece.color()) continue;

			while(n < move.repetitions() && validTile(destination)) {
				if(board.hasPiece(destination) && board.pieceAt(destination).color() == piece.color()) break;
				if(PieceType.isClassicType(piece.typeName()) && PieceType.valueOf(piece.typeName()) == PieceType.PAWN && isDiagonalMove(start, destination) && !board.hasPiece(destination)) break;

				possibleTiles.add(destination);
				n++;

				if(n >= move.repetitions()) break;

				file = ((destination.file().code + move.x() * movementFactor) - File.A.code);

				if(file < 0 || file >= 8) break;

				destination = new Tile(
						File.values()[file % 8],
						Rank.values()[destination.rank().number + move.y() * movementFactor]
				);
			}
		}

		return possibleTiles;
	}

	public List<Tile> calculateMovesetTiles2(Tile start) {
		List<Tile> tiles = new ArrayList<>();
		Piece piece = board.pieceAt(start);
		Set<Move> moveset = Moveset.getMoveset(piece.typeName());

		for(Move move : moveset) {
			List<Tile> moveTiles = calculateMoveTiles(start, move);


		}

		return tiles;
	}

	private List<Tile> calculateMoveTiles(Tile start, Move move) {
		List<Tile> tiles = new ArrayList<>();

		return tiles;
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
