package net.minecraftcapes.utils;

import com.google.gson.Gson;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class WebUtils {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static UsersResponse requestUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Configs.settings.USERS_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "minecraftcapes-auth/2026")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Non-2xx response
            if (response.statusCode() / 100 != 2) {
                MinecraftCapesAuth.getInstance().getLogger().error(
                        "Received invalid {} response from MinecraftCapes!",
                        response.statusCode()
                );

                return null;
            }

            // Parse JSON response
            return GSON.fromJson(response.body(), UsersResponse.class);

        } catch (IOException | InterruptedException e) {
            MinecraftCapesAuth.getInstance().getLogger().error(
                    "Failed to contact MinecraftCapes API",
                    e
            );

            return null;
        }
    }

    public static AuthResponse requestAuth(UUID uuid, String username) {
        try {

            // Build JSON body
            String json = GSON.toJson(new AuthRequest(
                    uuid.toString().replace("-", ""),
                    username
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Configs.settings.AUTH_ENDPOINT))
                    .header("Authorization", "Bearer " + Configs.settings.API_KEY)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "minecraftcapes-auth/2026")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Non-2xx response
            if (response.statusCode() / 100 != 2) {
                MinecraftCapesAuth.getInstance().getLogger().error(
                        "{} Received invalid {} response from MinecraftCapes!",
                        uuid,
                        response.statusCode()
                );

                return null;
            }

            // Parse JSON response
            return GSON.fromJson(response.body(), AuthResponse.class);

        } catch (IOException | InterruptedException e) {
            MinecraftCapesAuth.getInstance().getLogger().error(
                    "{} Failed to contact MinecraftCapes API",
                    uuid,
                    e
            );

            return null;
        }
    }

    public record UsersResponse(boolean success, String users) {}

    private record AuthRequest(String uuid, String username) {}

    public record AuthResponse(boolean success, boolean banned, String code) {}
}