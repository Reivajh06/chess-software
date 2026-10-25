package reivajh06.chess.gamelogic;

import java.util.Objects;

public class Position {

	private final String boardFen;
	private final String turnFen;
	private final String castlingRightsFen;
	private final String enPassantDestinationFen;

	public Position(String fen) {
		String[] fenSplit = fen.split(" ", 5);

		boardFen = fenSplit[0];
		turnFen = fenSplit[1];
		castlingRightsFen = fenSplit[2];
		enPassantDestinationFen = fenSplit[3];
	}

	@Override
	public String toString() {
		return "Position{" +
				"boardFen='" + boardFen + '\'' +
				", turnFen='" + turnFen + '\'' +
				", castlingRightsFen='" + castlingRightsFen + '\'' +
				", enPassantDestinationFen='" + enPassantDestinationFen + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Position position = (Position) o;
		return Objects.equals(boardFen, position.boardFen) && Objects.equals(turnFen, position.turnFen) && Objects.equals(castlingRightsFen, position.castlingRightsFen) && Objects.equals(enPassantDestinationFen, position.enPassantDestinationFen);
	}

	@Override
	public int hashCode() {
		return Objects.hash(boardFen, turnFen, castlingRightsFen, enPassantDestinationFen);
	}
}
