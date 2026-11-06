package reivajh06.architecture.domain.services;

import reivajh06.architecture.domain.model.*;
import reivajh06.architecture.domain.model.game.CastlingRights;
import reivajh06.architecture.domain.model.game.GameState;

public class FenParser {

	public static String boardToFen(Board board) {
		StringBuilder fenBoard = new StringBuilder();

		for(Rank rank = Rank.R8; rank != null && rank.number >= Rank.R1.number; rank = Rank.previous(rank)) {
			int emptyTiles = 0;
			StringBuilder rankFEN = new StringBuilder();

			for(File file = File.A; file != null && file.code <= File.H.code; file = File.next(file)) {
				Piece piece = board.pieceAt(new Tile(file, rank));

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

	public static String gameStateToFen(GameState gameState) {
		return new StringBuilder()
				.append(FenParser.boardToFen(gameState.board()))
				.append(" ")
				.append(gameState.turnColor().name().toLowerCase().charAt(0))
				.append(" ")
				.append(castlingRightsToFen(gameState.castlingRights()))
				.append(" ")
				.append(gameState.enPassantDestination() == null ? "-" : gameState.enPassantDestination().name().toLowerCase())
				.append(" ")
				.append(gameState.halfMoveClock())
				.append(" ")
				.append(gameState.fullMoves())
				.toString();
	}

	public static String gameStateToFen(Board board, Color turnColor, CastlingRights castlingRights, Tile enPassantDestination, int halfMoveClock, int fullMoves) {
		return new StringBuilder()
				.append(FenParser.boardToFen(board))
				.append(" ")
				.append(turnColor.name().toLowerCase().charAt(0))
				.append(" ")
				.append(castlingRightsToFen(castlingRights))
				.append(" ")
				.append(enPassantDestination == null ? "-" : enPassantDestination.name().toLowerCase())
				.append(" ")
				.append(halfMoveClock)
				.append(" ")
				.append(fullMoves)
				.toString();
	}

	public static Board fenToBoard(String fenBoard) {
		Board board = new Board();

		String[] fenRanks = fenBoard.split("/");
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

		return board;
	}

	public static String castlingRightsToFen(CastlingRights castlingRights) {
		String whiteKingside = castlingRights.hasRight(Color.WHITE, Castling.KINGSIDE) ? "K" : "";
		String whiteQueenside = castlingRights.hasRight(Color.WHITE, Castling.QUEENSIDE) ? "Q" : "";
		String blackKingside = castlingRights.hasRight(Color.BLACK, Castling.KINGSIDE) ? "k" : "";
		String blackQueenside = castlingRights.hasRight(Color.BLACK, Castling.QUEENSIDE) ? "q" : "";

		String whiteCastlingRights = whiteKingside + whiteQueenside;
		String blackCastlingRights = blackKingside + blackQueenside;

		String castlingRightsFen = whiteCastlingRights + blackCastlingRights;

		return castlingRightsFen.isEmpty() ? "-" : castlingRightsFen;
	}
}
