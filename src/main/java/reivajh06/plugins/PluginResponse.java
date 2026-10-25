package reivajh06.plugins;

import reivajh06.Json;
import reivajh06.chess.model.PieceType;
import reivajh06.chess.model.Tile;

import java.util.Map;

public record PluginResponse(Tile from, Tile to, PieceType newType) {

	public PluginResponse(String readResult) {
		Map<String, String> response = Json.fromJson(readResult, Map.class);

		Tile from = new Tile(response.get("from").toLowerCase().strip());
		Tile to = new Tile(response.get("to").toLowerCase().strip());
		PieceType newType = PieceType.valueOf(response.get("promote_to").toUpperCase().strip());

		this(from, to, newType);
	}
}
