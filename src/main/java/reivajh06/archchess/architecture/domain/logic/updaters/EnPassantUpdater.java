package reivajh06.archchess.architecture.domain.logic.updaters;

import reivajh06.archchess.architecture.domain.model.*;
import reivajh06.archchess.architecture.domain.model.game.GameState;
import reivajh06.archchess.architecture.domain.model.game.GameStateBuilder;
import reivajh06.archchess.architecture.domain.model.movement.Move;
import reivajh06.archchess.architecture.domain.model.movement.MoveRules;

public class EnPassantUpdater implements Updater {

	@Override
	public void update(GameStateBuilder gameStateBuilder, GameState previousState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		if(!move.condition().equals(MoveRules.START_TWO_FORWARD)) {
			gameStateBuilder.enPassantDestination(null);
			return;
		}

		Board board = previousState.board();

		if(board.pieceAt(to).color() == Color.WHITE) {
			gameStateBuilder.enPassantDestination(new Tile(to.file(), Rank.previous(to.rank())));
			return;
		}

		gameStateBuilder.enPassantDestination(board.pieceAt(to).color() == Color.WHITE ?
				new Tile(to.file(), Rank.previous(to.rank()))
				: new Tile(to.file(), Rank.next(to.rank())));
	}
}
