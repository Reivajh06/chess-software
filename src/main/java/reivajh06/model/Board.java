package reivajh06.model;

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
	private Tile whiteKingSideRook;
	private Tile whiteQueenSideRook;
	private Tile blackKingSideRook;
	private Tile blackQueenSideRook;

	public static Board of(String boardAsFEN) {
		Board board = new Board();

		String[] fenRanks = boardAsFEN.split("/");
		Rank rank = Rank.R8;

		for(String fenRank : fenRanks) {
			File file = File.A;
			char[] tilesData = fenRank.toCharArray();

			for(char tileData : tilesData) {
				int i = tileData - '0';

				if(i > 0 && i < 9) {
					int fileIndex = (file.code + i) - File.A.code;

					if(fileIndex == 8) continue;

					file = File.values()[fileIndex];

				} else {
					board.addPiece(new Tile(file, rank), PieceFactory.createPieceByFENCode(tileData));
					file = File.next(file);
				}
			}

			rank = Rank.previous(rank);
		}

		board.printBoard();
		return board;
	}

	public Board() {
		tiles = new HashMap<>();

		whiteKingTile = new Tile(File.A, Rank.R1);
		blackKingTile = new Tile(File.E, Rank.R8);

		whiteKingSideRook = new Tile(File.A, Rank.R1);
		whiteQueenSideRook = new Tile(File.H, Rank.R1);
		blackKingSideRook = new Tile(File.A, Rank.R8);
		blackQueenSideRook = new Tile(File.H, Rank.R8);

		allocatePieces(PieceColor.BLACK);
		allocatePieces(PieceColor.WHITE);
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
		Board board = new Board();

		for(Map.Entry<Tile, Piece> entry : tiles.entrySet()) {
			Tile tile = entry.getKey();
			Piece piece = entry.getValue();

			board.addPiece(new Tile(tile.file(), tile.rank()), piece == null ? null : piece.copy());
		}

		return board;
	}

	public String toFEN() {
		StringBuilder fenBoard = new StringBuilder();

		for(Rank rank = Rank.R8; rank != null && rank.number >= Rank.R1.number; rank = Rank.previous(rank)) {
			int emptyTiles = 0;
			StringBuilder rankFEN = new StringBuilder();

			for(File file = File.A; file != null && file.code <= File.H.code; file = File.next(file)) {
				Piece piece = tiles.get(new Tile(file, rank));

				if(piece == null) emptyTiles++;

				else {
					if(emptyTiles > 0) rankFEN.append(emptyTiles);

					rankFEN.append(piece.fenCode());
					emptyTiles = 0;
				}
			}

			fenBoard.append(rankFEN);
			if(emptyTiles != 0) fenBoard.append(emptyTiles);
			fenBoard.append("/");
		}

		fenBoard.deleteCharAt(fenBoard.length() - 1);
		return fenBoard.toString();
	}

	public Tile kingTile(PieceColor kingColor) {
		return kingColor == PieceColor.BLACK ? blackKingTile : whiteKingTile;
	}

	public Tile rookTile(PieceColor color, Castling castlingSide) {
		if(color == PieceColor.BLACK) return castlingSide == Castling.KINGSIDE ? blackKingSideRook : blackQueenSideRook;

		return castlingSide == Castling.KINGSIDE ? whiteKingSideRook : whiteQueenSideRook;
	}

	public Board blackKingTile(Tile blackKingTile) {
		this.blackKingTile = blackKingTile;
		return this;
	}

	public Board whiteKingTile(Tile whiteKingTile) {
		this.whiteKingTile = whiteKingTile;
		return this;
	}

	public Board whiteKingSideRook(Tile whiteKingSideRook) {
		this.whiteKingSideRook = whiteKingSideRook;
		return this;
	}

	public Board whiteQueenSideRook(Tile whiteQueenSideRook) {
		this.whiteQueenSideRook = whiteQueenSideRook;
		return this;
	}

	public Board blackKingSideRook(Tile blackKingSideRook) {
		this.blackKingSideRook = blackKingSideRook;
		return this;
	}

	public Board blackQueenSideRook(Tile blackQueenSideRook) {
		this.blackQueenSideRook = blackQueenSideRook;
		return this;
	}
}
