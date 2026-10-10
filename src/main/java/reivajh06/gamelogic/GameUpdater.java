package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEvent;
import reivajh06.model.movement.MoveRules;

import java.util.*;

public class GameUpdater {

	private final GameSession session;

	public GameUpdater(GameSession session) {
		this.session = session;
	}

	public Map<Move, List<Tile>> pieceMoves(Tile location) {
		Piece pieceToMove = session.gameState().board().pieceAt(location);

		if(pieceToMove == null || pieceToMove.color() != session.gameState().turnColor()) return new HashMap<>();

		return GameController.legalPieceMoves(session.gameState(), location);
	}

	public void movePieceAndUpdate(Move move, Tile from, Tile to) {
		if(session.gameState().status() != GameStatus.ACTIVE) return;

		Piece movedPiece = session.gameState().board().pieceAt(from);

		if(movedPiece == null) return;
		if(movedPiece.color() != session.gameState().turnColor()) return;

		Piece capturedPiece = GameController.movePiece(session.gameState(), move, from, to);

		if(movedPiece.type() == PieceType.KING) updateKingTile(movedPiece.color(), to);
		else if(movedPiece.type() == PieceType.ROOK) updateRookTile(movedPiece.color(), from, to);

		session.gameState(updateGameState(move, movedPiece, capturedPiece, from, to));
	}

	private void updateRookTile(PieceColor color, Tile previousLocation, Tile newLocation) {
		Board board = session.gameState().board();

		if(color == PieceColor.BLACK) {
			if(board.rookTile(color, Castling.KINGSIDE) == previousLocation) board.blackKingSideRook(newLocation);

			else board.blackQueenSideRook(newLocation);

		} else {
			if(board.rookTile(color, Castling.KINGSIDE) == previousLocation) board.whiteKingSideRook(newLocation);

			else board.whiteQueenSideRook(newLocation);
		}
	}

	private void updateKingTile(PieceColor color, Tile newLocation) {
		Board board = session.gameState().board();

		if(color == PieceColor.BLACK) board.blackKingTile(newLocation);

		else board.whiteKingTile(newLocation);
	}

	private GameState updateGameState(Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		GameState currentState = session.gameState();
		Board updatedBoard = currentState.board().copy();

		GameStateBuilder stateBuilder = new GameStateBuilder()
				.turnColor(PieceColor.rivalColor(movedPiece.color()))
				.board(updatedBoard)
				.movesHistory(updateMovesHistory(movedPiece, move, from, to, capturedPiece, session.gameState().toFEN()))
				.eatenPieces(updateEatenPieces(capturedPiece))
				.checkedColor(updateCheckedColor(movedPiece))
				.status(updateGameStatus(movedPiece))
				.enPassantDestination(updateEnPassantTile(move, to))
				.halfMoveClock(updateHalfMoveClock(movedPiece, capturedPiece))
				.fullMoves(updateFullMoves(movedPiece));

		if(movedPiece.color() == PieceColor.WHITE) {
			stateBuilder.whiteCastling(updateCastlingRights(movedPiece));
			stateBuilder.blackCastling(new ArrayList<>(currentState.blackCastling()));
		} else {
			stateBuilder.whiteCastling(new ArrayList<>(currentState.whiteCastling()));
			stateBuilder.blackCastling(updateCastlingRights(movedPiece));
		}

		return stateBuilder.build();
	}

	private PieceColor updateCheckedColor(Piece movedPiece) {
		PieceColor kingColor = PieceColor.rivalColor(movedPiece.color());

		return GameConditions.isKingChecked(session.gameState(), kingColor) ?
				kingColor
				: null;
	}

	private Map<PieceColor, List<Piece>> updateEatenPieces(Piece capturedPiece) {
		Map<PieceColor, List<Piece>> eatenPieces = new HashMap<>(session.gameState().eatenPieces());

		if(capturedPiece != null) eatenPieces.get(capturedPiece.color()).add(capturedPiece.copy());

		return eatenPieces;
	}

	private Deque<MoveEvent> updateMovesHistory(Piece movedPiece, Move move, Tile from, Tile to, Piece capturedPiece, String fen) {
		Deque<MoveEvent> movesHistory = session.gameState().movesHistory();

		movesHistory.addFirst(new MoveEvent(movedPiece, move, from, to, capturedPiece, fen));

		return movesHistory;
	}

	private GameStatus updateGameStatus(Piece movedPiece) {
		PieceColor rivalColor = PieceColor.rivalColor(movedPiece.color());

		if(GameConditions.isCheckMate(session.gameState(), rivalColor)) return movedPiece.color() == PieceColor.BLACK ?
				GameStatus.BLACK_WIN
				: GameStatus.WHITE_WIN;

		return GameConditions.onDraw(session.gameState()) ?
				GameStatus.DRAW
				: GameStatus.ACTIVE;
	}

	private int updateFullMoves(Piece movedPiece) {
		int fullMoves = session.gameState().fullMoves();

		return movedPiece.color() == PieceColor.BLACK ?
				fullMoves + 1 :
				fullMoves;
	}

	private int updateHalfMoveClock(Piece movedPiece, Piece capturedPiece) {
		if(movedPiece.type() == PieceType.PAWN) return 0;

		if(capturedPiece != null) return 0;

		return session.gameState().halfMoveClock() + 1;
	}

	private Tile updateEnPassantTile(Move move, Tile location) {
		if(!move.conditions().contains(MoveRules.START_TWO_FORWARD)) return session.gameState().enPassantDestination();

		Board board = session.gameState().board();

		File leftTileFile = File.previous(location.file());

		if(leftTileFile == null || board.pieceAt(new Tile(leftTileFile, location.rank())) == null) {
			File rightTileFile = File.next(location.file());

			if(rightTileFile == null || board.pieceAt(new Tile(rightTileFile, location.rank())) == null) return null;

			return new Tile(rightTileFile, location.rank());
		}

		return new Tile(leftTileFile, location.rank());
	}

	private List<Castling> updateCastlingRights(Piece movedPiece) {
		List<Castling> castlingRights = new ArrayList<>(
				movedPiece.color() == PieceColor.BLACK ?
						session.gameState().blackCastling()
						: session.gameState().whiteCastling()
		);

		if(castlingRights.getFirst() != null) {
			if(!canCastlingTo(movedPiece, Castling.KINGSIDE)) castlingRights.set(0, null);

		} else if(castlingRights.getLast() != null) {
			if(!canCastlingTo(movedPiece, Castling.QUEENSIDE)) castlingRights.set(1, null);
		}

		return castlingRights;
	}

	private boolean canCastlingTo(Piece movedPiece, Castling castlingSide) {
		PieceColor color = movedPiece.color();
		GameState currentState = session.gameState();
		Board updatedBoard = currentState.board();

		if(color == PieceColor.BLACK) {
			if(currentState.blackCastling().getFirst() == null) return false;
		} else {
			if(currentState.whiteCastling().getFirst() == null) return false;
		}

		if(movedPiece.type() == PieceType.KING) return false;

		Tile rookTile = updatedBoard.rookTile(movedPiece.color(), castlingSide);

		if(rookTile == null) return false;

		return movedPiece.type() != PieceType.ROOK || !updatedBoard.pieceAt(rookTile).equals(movedPiece);
	}
}
