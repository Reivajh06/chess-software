package reivajh06.model;

import java.util.HashMap;
import java.util.Map;

public class Board {

	private final Map<Tile, Piece> tiles;

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

	public void move(Tile from, Tile to) {
		tiles.put(to, pieceAt(from));
	}

	public Piece removeFromTile(Tile t) {
		return tiles.remove(t);
	}

	public Piece pieceAt(Tile tile) {
		return tiles.get(tile);
	}

	public boolean hasPiece(Tile tile) {
		return pieceAt(tile) != null;
	}

	public void addPiece(Tile tile, Piece piece) {
		tiles.put(tile, piece);
	}

	public Piece replace(Tile tile, Piece piece) {
		return tiles.replace(tile, piece);
	}

	public void debug() {
		for(int i = 8; i > 0; i--) {
			StringBuilder row = new StringBuilder();

			for(int j = 0; j < 8; j++) {
				Piece p = tiles.get(new Tile(File.values()[j], Rank.values()[i]));
				row.append(p == null ? ".." : p.toString());
				row.append(" ");
			}

			System.out.println(row);
		}
	}


	//TODO migrate methods
	/*public List<Tile> possiblePieceMoves(Tile tile) {
		Piece piece = pieceAt(tile);
		List<Tile> possibleTiles = new ArrayList<>();
		List<Move> moveset = piece.moveset();
		int movementFactor = piece.color() == PieceColor.BLACK ? -1 : 1;

		for(Move move : moveset) {
			int n = 0;

			int i = (tile.col().code + move.x() * movementFactor) - File.A.code;

			if(i < 0 || i >= 8) continue;

			Tile t = new Tile(
					File.values()[i % 8],
					tile.row() + move.y() * movementFactor
			);

			if(hasPiece(t) && pieceAt(t).color() == piece.color()) continue;

			while(n < move.repetitions() && validTile(t)) {
				if(hasPiece(t) && pieceAt(t).color() == piece.color()) break;

				possibleTiles.add(t);
				n++;

				if(n >= move.repetitions()) break;

				i = ((t.col().code + move.x() * movementFactor) - File.A.code);

				if(i < 0 || i >= 8) break;

				t = new Tile(
						File.values()[i % 8],
						t.row() + move.y() * movementFactor
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
*/
}
