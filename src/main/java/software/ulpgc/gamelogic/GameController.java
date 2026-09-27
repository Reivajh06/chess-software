package software.ulpgc.gamelogic;

import software.ulpgc.model.*;
import software.ulpgc.model.movement.Move;
import software.ulpgc.model.movement.MoveData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameController {

	protected static final Rank WHITE_PAWN_START_RANK = Rank.TWO;
	protected static final Rank WHITE_KING_START_RANK = Rank.ONE;
	protected static final Rank BLACK_PAWN_START_RANK = Rank.SEVEN;
	protected static final Rank BLACK_KING_START_RANK = Rank.EIGHT;

	private final Board board;
	private Tile whiteKingTile;
	private Tile blackKingTile;


	public GameController(Board board) {
		this.board = board;
	}

	public void setupBoard() {
		allocatePieces(board, PieceColor.BLACK);
		allocatePieces(board, PieceColor.WHITE);

		blackKingTile = new Tile(File.E, BLACK_KING_START_RANK);
		whiteKingTile = new Tile(File.E, WHITE_KING_START_RANK);
	}

	private void allocatePieces(Board board, PieceColor color) {
		Rank pawnRank = WHITE_PAWN_START_RANK;
		Rank kingRank = WHITE_KING_START_RANK;

		if(color == PieceColor.BLACK) {
			pawnRank = BLACK_PAWN_START_RANK;
			kingRank = BLACK_KING_START_RANK;
		}

		for(File file = File.A; file != null && file.compareTo(File.H) <= 0; file = File.next(file)) {
			board.addPiece(new Tile(file, pawnRank), PieceFactory.createPawn(color));
		}

		board.addPiece(new Tile(File.A, kingRank), PieceFactory.createRook(color));
		board.addPiece(new Tile(File.B, kingRank), PieceFactory.createKnight(color));
		board.addPiece(new Tile(File.C, kingRank), PieceFactory.createBishop(color));
		board.addPiece(new Tile(File.D, kingRank), PieceFactory.createQueen(color));
		board.addPiece(new Tile(File.E, kingRank), PieceFactory.createKing(color) );
		board.addPiece(new Tile(File.F, kingRank), PieceFactory.createBishop(color));
		board.addPiece(new Tile(File.G, kingRank), PieceFactory.createKnight(color));
		board.addPiece(new Tile(File.H, kingRank), PieceFactory.createRook(color));

	}

	public Piece movePiece(Move move, Tile from, Tile to) {
		Piece p = board.pieceAt(from);

		if(p.type() == PieceType.KING) {
			if(p.color() == PieceColor.BLACK) blackKingTile = to;
			else whiteKingTile = to;
		}

		return move.effect().apply(board, from, to);
	}

	public void addPiece(Piece piece, Tile tile) {
		board.addPiece(tile, piece);
	}

	public Piece removePieceAt(Tile tile) {
		return board.removePieceIn(tile);
	}

	public void undoMove(MoveData moveData) {
		Piece movedPiece = board.removePieceIn(moveData.to());
		addPiece(movedPiece, moveData.from());
		addPiece(moveData.pieceEaten(), moveData.to());
	}

	public List<Tile> calculateMoveTiles(PieceColor color, Tile start, Move move) {
		List<Tile> tiles = new ArrayList<>();
		Tile t = start;
		int movementFactor = color == PieceColor.BLACK ? -1 : 1;

		for(int i = 0; i < move.repetitions(); i++) {
			int fileIndex = t.file().code + move.x() - File.A.code;
			int rankIndex = t.rank().number + move.y() * movementFactor - Rank.ONE.number;

			if(outOfBounds(fileIndex, rankIndex)) break;

			t = new Tile(
					File.values()[fileIndex],
					Rank.values()[rankIndex]
			);

			tiles.add(t);

			if(board.pieceAt(t) != null) break;
		}

		return tiles;
	}

	public List<Tile> piecesTilesByColor(PieceColor color) {
		return board.asStream()
				.filter(t -> board.pieceAt(t) != null && board.pieceAt(t).color() == color)
				.toList();
	}

	public List<Tile> tilesByRank(Rank rank) {
		return board.asStream()
				.filter(t -> t.rank().equals(rank))
				.sorted(Comparator.comparing(Tile::file))
				.toList();
	}

	public Tile blackKingTile() {
		return blackKingTile;
	}

	public Tile whiteKingTile() {
		return whiteKingTile;
	}

	public Tile kingTile(PieceColor kingColor) {
		return kingColor == PieceColor.BLACK ? blackKingTile : whiteKingTile;
	}

	private boolean outOfBounds(int fileIndex, int rankIndex) {
		return (fileIndex < 0 || fileIndex >= File.values().length) ||
				(rankIndex < 0 || rankIndex >= Rank.values().length);
	}
}
