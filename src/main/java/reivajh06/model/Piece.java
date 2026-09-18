package reivajh06.model;

import org.jetbrains.annotations.NotNull;

public class Piece {

	private PieceColor color;
	private final String typeName;
	private final String name;

	public Piece(String typeName, PieceColor color) {
		this.typeName = typeName.trim().toUpperCase();
		this.color = color;
		name = color.name().charAt(0) + typeName.substring(0, 1);
	}

	public Piece(PieceType type, PieceColor color) {
		this(type.name().toLowerCase(), color);
	}

	public String getCode() {
		return color.name().charAt(0) + typeName.substring(0, 2);
	}

	public PieceColor color() {
		return color;
	}

	public String name() {
		return name;
	}

	public String typeName() {
		return typeName;
	}

	@NotNull
	@Override
	public String toString() {
		return getCode();
	}
}
