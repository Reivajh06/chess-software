package reivajh06.gamelogic;

import reivajh06.FenParser;
import reivajh06.gamelogic.updaters.*;
import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveEvent;

import java.util.*;

public class GameUpdater {

	private final GameSession session;
	private final List<Updater> updaters;

	public GameUpdater(GameSession session) {
		this.session = session;

		updaters = List.of(
				new EnPassantUpdater(),
				new HalfMoveClockUpdater(),
				new FullMovesUpdater(),
				new TurnColorUpdater(),
				new EatenPiecesUpdater(),
				new CastlingRightsUpdater()
		);
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

	private void updateKingTile(Color color, Tile newLocation) {
		Board board = session.gameState().board();

		if(color == Color.BLACK) board.blackKingTile(newLocation);

		else board.whiteKingTile(newLocation);
	}

	public void promotePiece(Tile location, PieceType newType) {
		Piece piece = session.gameState().board().pieceAt(location);
		Piece promotedPiece;

		if(piece == null || piece.type() != PieceType.PAWN) return;

		if(piece.color() == Color.BLACK) {
			if(location.rank() != Board.WHITE_KING_START_RANK) return;

			promotedPiece = PieceFactory.createPieceByFENCode(String.valueOf(newType.firstLetter).toLowerCase().charAt(0));

		} else {
			if(location.rank() != Board.BLACK_KING_START_RANK) return;

			promotedPiece = PieceFactory.createPieceByFENCode(String.valueOf(newType.firstLetter).charAt(0));
		}

		session.gameState().board().removePieceIn(location);
		session.gameState().board().addPiece(location, promotedPiece);
	}

	private GameState updateGameState(Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		GameStateBuilder builder = new GameStateBuilder();

		builder.board(session.gameState().board().copy());
		updaters.forEach(u -> u.update(builder, session.gameState(), move, movedPiece, capturedPiece, from, to));

		GameState updatedState = builder.build();

		String fen = FenParser.gameStateToFen(updatedState);

		updatedState.movesHistory().addFirst(updateMovesHistory(
				movedPiece,
				move,
				from,
				to,
				capturedPiece,
				fen
		));

		return updatedState;
	}

	private Color updateCheckedColor(Piece movedPiece) {
		Color kingColor = Color.rivalColor(movedPiece.color());

		return GameConditions.isKingChecked(session.gameState(), kingColor) ?
				kingColor
				: null;
	}

	private MoveEvent updateMovesHistory(Piece movedPiece, Move move, Tile from, Tile to, Piece capturedPiece, String fen) {
		return new MoveEvent(movedPiece, move, from, to, capturedPiece, fen);
	}

	private GameStatus updateGameStatus(Piece movedPiece) {
		Color rivalColor = Color.rivalColor(movedPiece.color());

		if(GameConditions.isCheckMate(session.gameState(), rivalColor)) return movedPiece.color() == Color.BLACK ?
				GameStatus.BLACK_WIN
				: GameStatus.WHITE_WIN;

		return GameConditions.onDraw(session.gameState()) ?
				GameStatus.DRAW
				: GameStatus.ACTIVE;
	}
}
