package net.minecraftcapes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebUtils {

    /**
     * Will send a request to the website asking to auth a player
     *
     * @param uuid Players uuid
     * @param username Players username
     * @return The auth data in an object
     */
    public static AuthData requestAuthData(UUID uuid, String username) {
        HttpRequest.BodyPublisher requestData = getRequestData(uuid, username);

        //Build HTTP request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Configs.settings.API_ENDPOINT))
                .POST(requestData)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            //Make HTTP request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //Return nothing if an error
            if(response.statusCode() / 100 != 2) {
                MinecraftCapesAuth.getInstance().getLogger().error("{} Received an invalid {} response from MinecraftCapes!", uuid, response.statusCode());
                return null;
            }

            return new Gson().fromJson(JsonParser.parseString(response.body()), AuthData.class);
        } catch (Exception e) {
            MinecraftCapesAuth.getInstance().getLogger().error(e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Prepares the post body for an API request
     * @param uuid Players uuid
     * @param username Players username
     * @return The Post data
     */
    private static HttpRequest.BodyPublisher getRequestData(UUID uuid, String username) {
        Map<Object, Object> data = new HashMap<>();
        data.put("key", Configs.settings.API_KEY);
        data.put("uuid", uuid.toString().replaceAll("-", ""));
        data.put("username", username);

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    /**
     * Just the class object for returning
     */
    public class AuthData {
        public boolean success;
        public boolean banned;
        public String code;
    }

}
