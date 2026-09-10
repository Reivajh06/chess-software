package reivajh06.model;

import java.util.HashMap;
import java.util.Map;

public class Board {

	//Tile -> null if does not contain any piece. Could be changed to a Tiles enum
	private Map<String, Piece> tiles;


	public Board(PieceColor userColor) {
		tiles = new HashMap<>();

		allocatePieces(userColor);
	}

	private void allocatePieces(PieceColor userColor) {
		allocatePieces(true, userColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
		allocatePieces(false, userColor);
	}

	private void allocatePieces(boolean placedFront, PieceColor color) {
		if(placedFront) {
			for(int i = 1; i <= 8; i++) {
				for(int j = 0; j < 8; j++) {						//Unicode based
					tiles.put(Character.toString(0x61 + j) + i, new Piece(PieceType.PAWN, color));
				}
			}

			tiles.put("a8", new Piece(PieceType.ROOK, color));
			tiles.put("b8", new Piece(PieceType.HORSE, color));
			tiles.put("c8", new Piece(PieceType.BISHOP, color));
			tiles.put("d8", new Piece(PieceType.QUEEN, color));
			tiles.put("e8", new Piece(PieceType.KING, color));
			tiles.put("f8", new Piece(PieceType.BISHOP, color));
			tiles.put("g8", new Piece(PieceType.HORSE, color));
			tiles.put("h8", new Piece(PieceType.ROOK, color));
		}


	}
}
