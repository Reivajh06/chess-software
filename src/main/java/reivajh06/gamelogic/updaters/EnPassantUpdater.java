package reivajh06.gamelogic.updaters;

import reivajh06.gamelogic.GameState;
import reivajh06.model.*;
import reivajh06.model.movement.Move;
import reivajh06.model.movement.MoveRules;

public class EnPassantUpdater implements Updater<Tile> {

	@Override
	public Tile update(GameState gameState, Move move, Piece movedPiece, Piece capturedPiece, Tile from, Tile to) {
		if(!move.condition().equals(MoveRules.START_TWO_FORWARD)) return null;

		Board board = gameState.board();

		if(board.pieceAt(to).color() == Color.WHITE) return new Tile(to.file(), Rank.previous(to.rank()));

		return board.pieceAt(to).color() == Color.WHITE ?
				new Tile(to.file(), Rank.previous(to.rank()))
				: new Tile(to.file(), Rank.next(to.rank()));
	}
}
