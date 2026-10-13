package reivajh06.gamelogic;

import reivajh06.FenParser;
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

	public Map<Integer, List<Tile>> pieceMoves(Tile location) {
		Piece pieceToMove = session.gameState().board().pieceAt(location);

		if(pieceToMove == null) return new HashMap<>();

		if(session.gamemode() != Gamemode.PRACTICE) {
			if(pieceToMove.color() != session.gameState().turnColor()) return new HashMap<>();
		}

		return GameController.legalPieceMoves(session.gameState(), location);
	}

	public void movePieceAndUpdate(int moveIndex, Tile from, Tile to) {
		movePieceAndUpdate(session.gameState().board().pieceAt(from).moveset().get(moveIndex), from, to);
	}

	public void movePieceAndUpdate(Move move, Tile from, Tile to) {
		if(session.gameState().status() != GameStatus.ACTIVE) return;

		Piece movedPiece = session.gameState().board().pieceAt(from);

		if(movedPiece == null) return;

		Piece capturedPiece = GameController.movePiece(session.gameState(), move, from, to);

		if(movedPiece.type() == PieceType.KING) updateKingTile(movedPiece.color(), to);

		session.gameState(updateGameState(move, movedPiece, capturedPiece, from, to));
	}

	private void updateKingTile(PieceColor color, Tile newLocation) {
		Board board = session.gameState().board();

		if(color == PieceColor.BLACK) board.blackKingTile(newLocation);

		else board.whiteKingTile(newLocation);
	}

	private GameState updateGameState(Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		GameState currentState = session.gameState();
		Board updatedBoard = currentState.board();

		PieceColor turnColor = PieceColor.rivalColor(movedPiece.color());
		Map<PieceColor, List<Piece>> eatenPieces = updateEatenPieces(capturedPiece);
		PieceColor checkedColor = updateCheckedColor(movedPiece);
		GameStatus status = updateGameStatus(movedPiece);
		Tile enPassantDestination = updateEnPassantTile(move, to);
		int halfMoveClock = updateHalfMoveClock(movedPiece, capturedPiece);
		int fullMoves = updateFullMoves(movedPiece);

		GameStateBuilder stateBuilder = new GameStateBuilder()
				.turnColor(turnColor)
				.board(updatedBoard)
				.eatenPieces(eatenPieces)
				.checkedColor(checkedColor)
				.status(status)
				.enPassantDestination(enPassantDestination)
				.halfMoveClock(halfMoveClock)
				.fullMoves(fullMoves);

		List<Castling> whiteCastling = new ArrayList<>(currentState.whiteCastling());
		List<Castling> blackCastling = new ArrayList<>(currentState.blackCastling());

		if(movedPiece.color() == PieceColor.WHITE) {
			if(movedPiece.type() == PieceType.KING) {
				whiteCastling.set(0, null);
				whiteCastling.set(1, null);
			}

			else whiteCastling = updateCastlingRights(movedPiece);
		}

		else {
			if(movedPiece.type() == PieceType.KING) {
				blackCastling.set(0, null);
				blackCastling.set(1, null);
			}

			else blackCastling = updateCastlingRights(movedPiece);
		}

		stateBuilder.whiteCastling(whiteCastling).blackCastling(blackCastling);

		String fen = FenParser.gameStateToFen(
				updatedBoard,
				turnColor,
				whiteCastling,
				blackCastling,
				enPassantDestination,
				halfMoveClock,
				fullMoves
		);

		Deque<MoveEvent> movesHistory = updateMovesHistory(movedPiece, move, from, to, capturedPiece, fen);

		stateBuilder.movesHistory(movesHistory);

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
			if(!canCastleKingSide(movedPiece)) castlingRights.set(0, null);

		} else if(castlingRights.getLast() != null) {
			if(!canCastleQueenSide(movedPiece)) castlingRights.set(1, null);
		}

		return castlingRights;
	}

	private boolean canCastleKingSide(Piece movedPiece) {
		PieceColor color = movedPiece.color();
		GameState currentState = session.gameState();
		Board updatedBoard = currentState.board();

		if(movedPiece.type() == PieceType.KING) return false;

		if(color == PieceColor.BLACK) {
			if(currentState.blackCastling().getFirst() == null) return false;

			Tile kingsideRookTile = new Tile(File.H, Board.BLACK_KING_START_RANK);
			Piece piece = updatedBoard.pieceAt(kingsideRookTile);

			if(piece == null) return false;
			if(piece.type() == PieceType.ROOK) return piece.color() == movedPiece.color() && piece.timesMoved() == 0;

		} else {
			if(currentState.whiteCastling().getFirst() == null) return false;

			Tile kingsideRookTile = new Tile(File.H, Board.WHITE_KING_START_RANK);
			Piece piece = updatedBoard.pieceAt(kingsideRookTile);

			if(piece == null) return false;
			if(piece.type() == PieceType.ROOK) return piece.color() == movedPiece.color() && piece.timesMoved() == 0;
		}

		return false;
	}

	private boolean canCastleQueenSide(Piece movedPiece) {
		PieceColor color = movedPiece.color();
		GameState currentState = session.gameState();
		Board updatedBoard = currentState.board();

		if(movedPiece.type() == PieceType.KING) return false;

		if(color == PieceColor.BLACK) {
			if(currentState.blackCastling().getLast() == null) return false;

			Tile queensideRookTile = new Tile(File.A, Board.BLACK_KING_START_RANK);
			Piece piece = updatedBoard.pieceAt(queensideRookTile);

			if(piece == null) return false;
			if(piece.type() == PieceType.ROOK) return piece.timesMoved() == 0;

		} else {
			if(currentState.whiteCastling().getLast() == null) return false;

			Tile queensideRookTile = new Tile(File.A, Board.WHITE_KING_START_RANK);
			Piece piece = updatedBoard.pieceAt(queensideRookTile);

			if(piece == null) return false;
			if(piece.type() == PieceType.ROOK) return piece.timesMoved() == 0;
		}

		return false;
	}
}
