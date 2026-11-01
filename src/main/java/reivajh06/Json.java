package reivajh06;

import com.google.gson.*;
import reivajh06.chess.model.Tile;

import java.lang.reflect.Type;

public class Json {

	private static final Gson GSON_PRETTY = new GsonBuilder()
			.setPrettyPrinting()
			.serializeNulls()
			.registerTypeAdapter(Tile.class, (JsonDeserializer<Tile>) (json, _, _) -> new Tile(json.getAsString()))
			.registerTypeAdapter(Tile.class, (JsonSerializer<Tile>) (tile, _, _) -> new JsonPrimitive(tile.name().toLowerCase()))
			.create();

	private static final Gson GSON = new GsonBuilder()
			.serializeNulls()
			.registerTypeAdapter(Tile.class, (JsonDeserializer<Tile>) (json, _, _) -> new Tile(json.getAsString()))
			.registerTypeAdapter(Tile.class, (JsonSerializer<Tile>) (tile, _, _) -> new JsonPrimitive(tile.name().toLowerCase()))
			.create();

	public static <T> T fromJson(String json, Type type) {
		return GSON.fromJson(json, type);
	}

	public static String toJson(Object object) {
		return GSON.toJson(object);
	}

	public static <T> T fromJsonPretty(String json, Type type) {
		return GSON_PRETTY.fromJson(json, type);
	}

	public static String toJsonPretty(Object object) {
		return GSON_PRETTY.toJson(object);
	}
}
