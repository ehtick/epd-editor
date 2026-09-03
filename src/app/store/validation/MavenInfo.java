package app.store.validation;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.openlca.commons.Res;
import org.openlca.commons.Strings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import epd.util.Json;

@NullMarked
public record MavenInfo(String name, String version, long timestamp) {

	private static final String g = "com.okworx.ilcd.validation.profiles";

	public static Res<List<MavenInfo>> fetchAll() {
		var url = "https://search.maven.org/solrsearch/select?q=g:" + g + "&wt=json";
		var client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		try (client) {
			var req = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Accept", "application/json")
				.GET()
				.build();
			var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
			var json = resp.body();
			return parse(json);
		} catch (Exception e) {
			return Res.error("Request for validation profiles failed", e);
		}
	}

	private static Res<List<MavenInfo>> parse(String json) {
		try {
			var data = new Gson().fromJson(json, JsonElement.class);
			if (!data.isJsonObject())
				return Res.error("Unexpected response format; not an object");
			var root = data.getAsJsonObject();
			var resp = Json.getObject(root, "response");
			if (resp == null)
				return Res.error("Unexpected response format; missing 'response' object");
			var docs = resp.getAsJsonArray("docs");
			if (docs == null)
				return Res.error("Unexpected response format; missing 'docs' array");

			var list = new ArrayList<MavenInfo>();
			for (var elem : docs) {
				if (!elem.isJsonObject())
					continue;
				var obj = elem.getAsJsonObject();
				var pom = Json.getString(obj, "p");
				if (!"jar".equals(pom))
					continue;

				var name = Json.getString(obj, "a");
				var version = Json.getString(obj, "latestVersion");
				var timestamp = Json.getLong(obj, "timestamp", 0);
				if (Strings.isNotBlank(name)
					&& Strings.isNotBlank(version)
					&& timestamp > 0) {
					list.add(new MavenInfo(name, version, timestamp));
				}
			}
			return Res.ok(list);
		} catch (Exception e) {
			return Res.error("Failed to read response", e);
		}
	}

	public String fileName() {
		return name + "-" + version + ".jar";
	}

	/// Returns `true` when this is a validation profile for EPDs. This is probably
	/// the case when the name of the profile starts with `EPD-`.
	public boolean isForEpd() {
		return name.startsWith("EPD-");
	}

	public Res<File> downloadTo(File targetDir) {
		var url = "https://repo1.maven.org/maven2/"
			+ g.replace('.', '/') + "/" + name + "/" + version + "/" + fileName();
		var client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		try (client) {
			var req = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Accept", "application/java-archive")
				.GET()
				.build();
			var handler = HttpResponse.BodyHandlers.ofFile(
				targetDir.toPath().resolve(fileName()));
			var resp = client.send(req, handler);
			return resp.statusCode() == 200
				? Res.ok(resp.body().toFile())
				: Res.error("Failed to download validation profile: "
				+ resp.statusCode());
		} catch (Exception e) {
			return Res.error("Failed to download validation profile from: " + url, e);
		}
	}
}
