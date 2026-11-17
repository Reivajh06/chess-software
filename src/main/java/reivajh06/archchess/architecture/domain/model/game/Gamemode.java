package reivajh06.archchess.architecture.domain.model.game;

public enum Gamemode {

	PRACTICE,
	PVP,
	PVM,
	MVM,
	PVALFI;

	public static boolean requiresPlugin(Gamemode gamemode) {
		return gamemode != PRACTICE && gamemode != PVP;
	}
}
