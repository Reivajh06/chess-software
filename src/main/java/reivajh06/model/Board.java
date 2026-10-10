package reivajh06.model;

import reivajh06.FenParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Board {

	public static final Rank WHITE_PAWN_START_RANK = Rank.R2;
	public static final Rank WHITE_KING_START_RANK = Rank.R1;
	public static final Rank BLACK_PAWN_START_RANK = Rank.R7;
	public static final Rank BLACK_KING_START_RANK = Rank.R8;

	private final Map<Tile, Piece> tiles;
	private Tile blackKingTile;
	private Tile whiteKingTile;

	public static Board startingBoard() {
		Board board = new Board();

		board.whiteKingTile = new Tile(File.E, Rank.R1);
		board.blackKingTile = new Tile(File.E, Rank.R8);

		board.allocatePieces(PieceColor.BLACK);
		board.allocatePieces(PieceColor.WHITE);

		return board;
	}

	public Board() {
		tiles = new HashMap<>();

		for(Rank rank : Rank.values()) {
			for(File file : File.values()) {
				tiles.put(new Tile(file, rank), null);
			}
		}
	}

	private void allocatePieces(PieceColor color) {
		Rank pawnRank = WHITE_PAWN_START_RANK;
		Rank kingRank = WHITE_KING_START_RANK;

		if(color == PieceColor.BLACK) {
			pawnRank = BLACK_PAWN_START_RANK;
			kingRank = BLACK_KING_START_RANK;
		}

		for(File file = File.A; file != null && file.compareTo(File.H) <= 0; file = File.next(file)) {
			addPiece(new Tile(file, pawnRank), PieceFactory.createPawn(color));
		}

		addPiece(new Tile(File.A, kingRank), PieceFactory.createRook(color));
		addPiece(new Tile(File.B, kingRank), PieceFactory.createKnight(color));
		addPiece(new Tile(File.C, kingRank), PieceFactory.createBishop(color));
		addPiece(new Tile(File.D, kingRank), PieceFactory.createQueen(color));
		addPiece(new Tile(File.E, kingRank), PieceFactory.createKing(color) );
		addPiece(new Tile(File.F, kingRank), PieceFactory.createBishop(color));
		addPiece(new Tile(File.G, kingRank), PieceFactory.createKnight(color));
		addPiece(new Tile(File.H, kingRank), PieceFactory.createRook(color));

	}

	public Piece removePieceIn(Tile t) {
		Piece p = tiles.get(t);
		replace(t, null);
		return p;
	}

	public Piece pieceAt(Tile tile) {
		return tiles.get(tile);
	}

	public void addPiece(Tile tile, Piece piece) {
		tiles.put(tile, piece);
	}

	public Piece replace(Tile tile, Piece piece) {
		return tiles.replace(tile, piece);
	}

	public Stream<Tile> asStream() {
		return tiles.keySet().stream();
	}

	public List<Tile> piecesTilesByColor(PieceColor color) {
		return asStream()
				.filter(t -> pieceAt(t) != null && pieceAt(t).color() == color)
				.toList();
	}

	public List<Tile> tilesByRank(Rank rank) {
		return asStream()
				.filter(t -> t.rank().equals(rank))
				.sorted(Comparator.comparing(Tile::file))
				.toList();
	}

	public boolean piecesRemaining(PieceColor color, PieceType... pieceTypes) {
		List<Tile> remainingPiecesTiles = piecesTilesByColor(color);

		if(remainingPiecesTiles.size() != pieceTypes.length) return false;

		return remainingPiecesTiles.stream()
				.map(t -> pieceAt(t).type())
				.collect(Collectors.toSet())
				.containsAll(List.of(pieceTypes));
	}

	public void printBoard() {
		System.out.println("  " + Arrays.stream(File.values()).map(File::name).collect(Collectors.joining("   ")));

		for(int i = 7; i >= 0; i--) {
			StringBuilder row = new StringBuilder();
			row.append(Rank.values()[i].number).append(" ");

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(File.values()[j], Rank.values()[i]));
				row.append(p == null ? ". " : p.toString());
				row.append("  ");
			}

			System.out.println(row);
		}
	}

	public Board copy() {
		return FenParser.fenToBoard(FenParser.boardToFen(this));
	}

	public Tile kingTile(PieceColor kingColor) {
		return kingColor == PieceColor.BLACK ? blackKingTile : whiteKingTile;
	}

	public Board blackKingTile(Tile blackKingTile) {
		this.blackKingTile = blackKingTile;
		return this;
	}

	public Board whiteKingTile(Tile whiteKingTile) {
		this.whiteKingTile = whiteKingTile;
		return this;
	}
}
