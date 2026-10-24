package reivajh06.gamelogic;

import reivajh06.gamelogic.evaluators.*;
import reivajh06.gamelogic.updaters.*;
import reivajh06.model.*;
import reivajh06.model.movement.Move;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDirector {

	private final GameSession session;
	private final List<Updater> updaters;
	private final List<Evaluator> evaluators;

	public GameDirector(GameSession session) {
		this.session = session;

		updaters = List.of(
				new EnPassantUpdater(),
				new HalfMoveClockUpdater(),
				new FullMovesUpdater(),
				new TurnColorUpdater(),
				new EatenPiecesUpdater(),
				new CastlingRightsUpdater()
		);

		evaluators = List.of(
				new CheckedColorEvaluator(),
				new MovesHistoryEvaluator(),
				new PositionsEvaluator(),
				new GameStatusEvaluator()
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
		GameStateBuilder updatedGameStateBuilder = new GameStateBuilder();

		updatedGameStateBuilder.board(session.gameState().board().copy());
		updatedGameStateBuilder.movesHistory(new ArrayDeque<>(session.gameState().movesHistory()));
		updaters.forEach(u -> u.update(updatedGameStateBuilder, session.gameState(), move, movedPiece, capturedPiece, from, to));
		updatedGameStateBuilder.positions(session.gameState().positions());

		GameState transitiveState = updatedGameStateBuilder.build();

		updatedGameStateBuilder.movesHistory(transitiveState.movesHistory());
		updatedGameStateBuilder.positions(new HashMap<>(session.gameState().positions()));

		evaluators.forEach(e -> e.evaluate(updatedGameStateBuilder, transitiveState, move, movedPiece, capturedPiece, from, to));

		return updatedGameStateBuilder.build();
	}
}
