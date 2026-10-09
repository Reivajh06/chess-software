package reivajh06.gamelogic;

import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEvent;

import java.util.*;

public class GameUpdater {

	private final GameSession session;

	public GameUpdater(GameSession session) {
		this.session = session;
	}

	public Map<Move, List<Tile>> pieceMoves(Tile location) {
		return GameController.legalPieceMoves(session.gameState(), location);
	}

	public void movePieceAndUpdate(Move move, Tile from, Tile to) {
		if(session.gameState().status() != GameStatus.ACTIVE) return;

		Piece movedPiece = session.gameState().board().pieceAt(from);

		if(movedPiece == null) return;

		Piece capturedPiece = GameController.movePiece(session.gameState(), move, from, to);

		session.gameState(updateGameState(move, movedPiece, capturedPiece, from, to));
	}

	private GameState updateGameState(Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		GameState currentState = session.gameState();
		Board updatedBoard = currentState.board().copy();

		GameStateBuilder stateBuilder = new GameStateBuilder()
				.turnColor(PieceColor.rivalColor(movedPiece.color()))
				.board(updatedBoard)
				.movesHistory(updateMovesHistory(movedPiece, move, from, to, capturedPiece, updatedBoard.toFEN()))
				.eatenPieces(updateEatenPieces(capturedPiece))
				.checkedColor(updateCheckedColor(movedPiece))
				.status(updateGameStatus())
				.enPassantDestination(updateEnPassantTile())
				.halfMoveClock(updateHalfMoveClock(movedPiece, capturedPiece))
				.fullMoves(updateFullMove(movedPiece));

		if(movedPiece.color() == PieceColor.WHITE) {
			stateBuilder.whiteCastling(updateCastlingRights(move, movedPiece));
			stateBuilder.blackCastling(new ArrayList<>(currentState.blackCastling()));
		} else {
			stateBuilder.whiteCastling(new ArrayList<>(currentState.blackCastling()));
			stateBuilder.blackCastling(updateCastlingRights(move, movedPiece));
		}

		return stateBuilder.build();
	}

	private PieceColor updateCheckedColor(Piece movedPiece) {
		return null;
	}

	private Map<PieceColor, List<Piece>> updateEatenPieces(Piece capturedPiece) {
		return null;
	}

	private Deque<MoveEvent> updateMovesHistory(Piece movedPiece, Move move, Tile from, Tile to, Piece capturedPiece, String fen) {
		return null;
	}

	private GameStatus updateGameStatus() {
		return null;
	}

	private int updateFullMove(Piece movedPiece) {
		return 0;
	}

	private int updateHalfMoveClock(Piece movedPiece, Piece capturedPiece) {
		return 0;
	}

	private Tile updateEnPassantTile() {
		return null;
	}

	private List<Castling> updateCastlingRights(Move move, Piece movedPiece) {
		return null;
	}
}
