package software.ulpgc.model;

public enum Rank {

	R1(1),
	R2(2),
	R3(3),
	R4(4),
	R5(5),
	R6(6),
	R7(7),
	R8(8);

	public final int number;

	Rank(int number) {
		this.number = number;
	}

	public static Rank next(Rank currentRank) {
		if(currentRank.equals(Rank.R8)) return null;
		return Rank.values()[currentRank.number];
	}

	public static Rank previous(Rank currentRank) {
		if(currentRank.equals(Rank.R1)) return null;
		return Rank.values()[currentRank.number - Rank.R1.number - 1];
	}
}
