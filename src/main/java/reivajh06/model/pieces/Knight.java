package reivajh06.model.pieces;

import java.util.List;

public class Knight extends Piece {

	public Knight(PieceColor color) {
		super(color);
		name = "Knight";
	}

	@Override
	public String name() {
		return name.substring(0, 2);
	}

	@Override
	public String getCode() {
		return color.name().charAt(0) + name();
	}

	@Override
	public List<String> possibleMoves(String currentTile) {
		return List.of();
	}
}
