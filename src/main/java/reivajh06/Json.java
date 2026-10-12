package reivajh06;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class Json {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

	public static <T> T fromJson(String json, Type type) {
		return GSON.fromJson(json, type);
	}

	public static String toJson(Object object) {
		return GSON.toJson(object);
	}
}
