package me.thrusmyster.tevobungee.Util;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import me.thrusmyster.tevobungee.TevoBungee;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UUIDFetcher {
	
	private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
	private static final JsonParser parser = new JsonParser();
	private static HashMap<String, UUID> uuid_Cache = new HashMap<String, UUID>();
	
	
	public static UUID getUUID(final String playername)
	{
		UUID uuid = null;
		if (uuid_Cache.containsKey(playername.toLowerCase()))
		{
			return uuid_Cache.get(playername.toLowerCase());
		}
		try
		{
			HttpURLConnection connection = createConnection();
			String body = new Gson().toJson(Arrays.asList(playername));
			writeBody(connection, body);
			JsonArray array = (JsonArray)parser.parse(new InputStreamReader(connection.getInputStream()));
			for (Object profile : array)
			{
				JsonObject jsonProfile = (JsonObject) profile;
				String id = jsonProfile.get("id").toString();
				id = id.replaceAll("\"", "");
				uuid = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
			}
		}
		catch(Exception e)
		{
			TevoBungee.getInstance().getUtilLogger().warning("UUIDFetcher> Failed to get UUID! The username was probably invalid!");
			e.printStackTrace();
		}
		if (uuid != null)
		{
			uuid_Cache.put(playername.toLowerCase(), uuid);	
		}
		return uuid;
	}
	
	public static void removefromCache(String playername)
	{
		uuid_Cache.remove(playername.toLowerCase());
	}
	
	
	private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }
	
	private static HttpURLConnection createConnection() throws Exception {
	       URL url = new URL(PROFILE_URL);
	       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	       connection.setRequestMethod("POST");
	       connection.setRequestProperty("Content-Type", "application/json");
	       connection.setUseCaches(false);
	       connection.setDoInput(true);
	       connection.setDoOutput(true);
	       return connection;
	    }
}
