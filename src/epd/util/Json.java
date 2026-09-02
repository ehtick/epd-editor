package epd.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class Json {

	private Json() {
	}

	public static void write(Object obj, File file) {
		try (var fos = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				 var buffer = new BufferedWriter(writer)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String string = gson.toJson(obj);
			buffer.write(string);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Json.class);
			log.error("failed to write {} to {}", obj, file, e);
		}
	}

	public static <T> T read(File file, Class<T> clazz) {
		try (FileInputStream fis = new FileInputStream(file)) {
			return read(fis, clazz);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read {} from file {}", clazz, file, e);
			return null;
		}
	}

	public static <T> T read(InputStream stream, Class<T> clazz) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			Gson gson = new Gson();
			return gson.fromJson(reader, clazz);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read {}", clazz, e);
			return null;
		}
	}

	public static JsonObject getObject(JsonObject obj, String property) {
		var entry = getElement(obj, property);
		return entry != null && entry.isJsonObject()
			? entry.getAsJsonObject()
			: null;
	}

	public static JsonArray getArray(JsonObject obj, String property) {
		var entry = getElement(obj, property);
		return entry != null && entry.isJsonArray()
			? entry.getAsJsonArray()
			: null;
	}

	public static String getString(JsonObject obj, String property) {
		var prim = getPrimitive(obj, property);
		if (prim == null)
			return null;
		return prim.isString()
			? prim.getAsString()
			: null;
	}

	public static long getLong(JsonObject obj, String property, long defaultVal) {
		var prim = getPrimitive(obj, property);
		if (prim == null)
			return defaultVal;
		return prim.isNumber()
			? prim.getAsLong()
			: defaultVal;
	}

	private static JsonPrimitive getPrimitive(JsonObject obj, String property) {
		var entry = getElement(obj, property);
		return entry != null && entry.isJsonPrimitive()
			? entry.getAsJsonPrimitive()
			: null;
	}

	private static JsonElement getElement(JsonObject obj, String property) {
		return obj != null && property != null
			? obj.get(property)
			: null;
	}
}
