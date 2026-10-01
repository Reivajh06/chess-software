package software.ulpgc.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Board {

	private final Map<Tile, Piece> tiles;

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

		File[] files = File.values();
		Rank[] ranks = Rank.values();

		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				tiles.put(new Tile(files[j], ranks[i]), null);
			}
		}
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
}
