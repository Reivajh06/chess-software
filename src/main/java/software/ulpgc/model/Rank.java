package software.ulpgc.model;

public enum Rank {

	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8);

	public final int number;

	Rank(int number) {
		this.number = number;
	}

	public static Rank next(Rank currentFile) {
		if(currentFile.equals(Rank.EIGHT)) return null;
		return Rank.values()[currentFile.number - Rank.EIGHT.number + 1];
	}

	public static Rank previous(Rank currentFile) {
		if(currentFile.equals(Rank.ONE)) return null;
		return Rank.values()[currentFile.number - Rank.ONE.number - 1];
	}
}
