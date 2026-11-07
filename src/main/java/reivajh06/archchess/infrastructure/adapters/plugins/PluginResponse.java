package reivajh06.archchess.infrastructure.adapters.plugins;

import reivajh06.archchess.architecture.domain.model.PieceType;
import reivajh06.archchess.architecture.domain.model.Tile;
import reivajh06.archchess.infrastructure.adapters.Json;

import java.util.Map;
import java.util.Optional;

public record PluginResponse(Tile from, Tile to, Optional<PieceType> newType) {

	public PluginResponse(String readResult) {
		Map<String, String> response = Json.fromJsonPretty(readResult, Map.class);

		Tile from = new Tile(response.get("from").toLowerCase().strip());
		Tile to = new Tile(response.get("to").toLowerCase().strip());
		Optional<PieceType> newType = response.get("promote_to") == null ?
				Optional.empty()
				: Optional.of(PieceType.valueOf(response.get("promote_to").toUpperCase().strip()));

		this(from, to, newType);
	}
}
