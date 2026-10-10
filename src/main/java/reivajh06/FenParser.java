package reivajh06;

import reivajh06.gamelogic.GameState;
import reivajh06.model.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
		String whiteCastlingRights = gameState.whiteCastling().stream().filter(Objects::nonNull).map(Castling::firstLetter).collect(Collectors.joining());
		String blackCastlingRights = gameState.blackCastling().stream().filter(Objects::nonNull).map(c -> c.firstLetter().toLowerCase()).collect(Collectors.joining());

		String castlingRights = whiteCastlingRights + blackCastlingRights;

		if(castlingRights.isEmpty()) castlingRights = "-";

		return new StringBuilder()
				.append(FenParser.boardToFen(gameState.board()))
				.append(" ")
				.append(gameState.turnColor().name().toLowerCase().charAt(0))
				.append(" ")
				.append(castlingRights)
				.append(" ")
				.append(gameState.enPassantDestination() == null ? "-" : gameState.enPassantDestination().name().toLowerCase())
				.append(" ")
				.append(gameState.halfMoveClock())
				.append(" ")
				.append(gameState.fullMoves())
				.toString();
	}

	public static String gameStateToFen(Board board, PieceColor turnColor, List<Castling> whiteCastling, List<Castling> blackCastling, Tile enPassantDestination, int halfMoveClock, int fullMoves) {
		String whiteCastlingRights = whiteCastling.stream().filter(Objects::nonNull).map(Castling::firstLetter).collect(Collectors.joining());
		String blackCastlingRights = blackCastling.stream().filter(Objects::nonNull).map(c -> c.firstLetter().toLowerCase()).collect(Collectors.joining());

		String castlingRights = whiteCastlingRights + blackCastlingRights;

		if(castlingRights.isEmpty()) castlingRights = "-";

		return new StringBuilder()
				.append(FenParser.boardToFen(board))
				.append(" ")
				.append(turnColor.name().toLowerCase().charAt(0))
				.append(" ")
				.append(castlingRights)
				.append(" ")
				.append(enPassantDestination == null ? "-" : enPassantDestination.name().toLowerCase())
				.append(" ")
				.append(halfMoveClock)
				.append(" ")
				.append(fullMoves)
				.toString();
	}
}
