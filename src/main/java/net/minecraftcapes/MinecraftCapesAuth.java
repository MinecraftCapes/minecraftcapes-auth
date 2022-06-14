package net.minecraftcapes;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(id = "minecraftcapesauth", name = "MinecraftCapesAuth", version = "0.1.0-SNAPSHOT", url = "https://minecraftcapes.net", description = "Auth Plugin for MinecraftCapes", authors = {"james090500"})
public class MinecraftCapesAuth {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public MinecraftCapesAuth(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    /**
     * This runs on player login
     * @param event The LoginEvent
     */
    @Subscribe
    public void onPreLogin(LoginEvent event) {
        logger.info(event.getPlayer().getUsername(), "is requesting an auth code");

        //The Auth Data
        AuthData authData = this.requestAuthData(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
        Component playerResponse = this.getLoginResponse(authData);

        event.setResult(ResultedEvent.ComponentResult.denied(playerResponse));

        logger.info(event.getPlayer().getUsername(), "has been served");
    }

    /**
     * Prepares the post body for an API request
     * @param uuid Players uuid
     * @param username Players username
     * @return The Post data
     */
    private HttpRequest.BodyPublisher getRequestData(UUID uuid, String username) {
        Map<Object, Object> data = new HashMap<>();
        data.put("key", "123");
        data.put("uuid", uuid);
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
     * Will send a request to the website asking to auth a player
     *
     * @param uuid Players uuid
     * @param username Players username
     * @return The auth data in an object
     */
    private AuthData requestAuthData(UUID uuid, String username) {
        HttpRequest.BodyPublisher requestData = getRequestData(uuid, username);
        String endpoint = "https://minecraftcapes.net/api/account/auth";

        //Build HTTP request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .POST(requestData)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            //Make HTTP request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //Return nothing if an error
            if(response.statusCode() / 100 != 2) {
                logger.error("{} Recieved an invalid {} response from MinecraftCapes!", uuid, response.statusCode());
                return null;
            }

            return new Gson().fromJson(JsonParser.parseString(response.body()), AuthData.class);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Get the players response message
     * @param authData The auth data from the API
     * @return The players response in component form
     */
    private Component getLoginResponse(AuthData authData) {
        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&c&lSomething went wrong\nPlease reconnect to try again");
        Component blockedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&c&lYour account has been banned for violating our terms of service.");
        Component authMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&fYour authorization code is\n&c&l\u00BB&f " + authData.code + " &c&l\u00AB`");

        //If we have no data, lets tell the user
        if(authData == null) return errorMessage;

        //If we have data we can return a code, an error or a banned message
        if(authData.success) {
            return (authData.code == null) ? errorMessage : authMessage;
        } else if(authData.banned) {
            return blockedMessage;
        }

        //All else fails, we return an error
        return errorMessage;
    }

    class AuthData {
        private boolean success;
        private boolean banned;
        private String code;
    }
}