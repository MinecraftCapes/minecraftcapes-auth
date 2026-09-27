package net.minecraftcapes.utils;

import com.google.gson.Gson;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WebUtils {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private static final Gson GSON = new Gson();

    private WebUtils() {}

    public static CompletableFuture<UsersResponse> requestUsers() {
        return send(request(Configs.settings.USERS_ENDPOINT).GET().build(), UsersResponse.class);
    }

    public static CompletableFuture<AuthResponse> requestAuth(Configs.Settings settings,
                                                              UUID uuid, String username, String skin) {
        String json = GSON.toJson(new AuthRequest(uuid.toString().replace("-", ""), username, skin));
        return send(request(settings.AUTH_ENDPOINT)
                .header("Authorization", "Bearer " + settings.API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(), AuthResponse.class);
    }

    private static HttpRequest.Builder request(String endpoint) {
        return HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "minecraftcapes-auth/2026");
    }

    private static <T> CompletableFuture<T> send(HttpRequest request, Class<T> type) {
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() / 100 != 2) {
                        MinecraftCapesAuth.getInstance().getLogger().warn(
                                "MinecraftCapes API returned HTTP {}", response.statusCode());
                        return null;
                    }
                    return GSON.fromJson(response.body(), type);
                }).exceptionally(error -> {
                    MinecraftCapesAuth.getInstance().getLogger().warn("MinecraftCapes API request failed", error);
                    return null;
                });
    }

    public static void close() {
        CLIENT.shutdownNow();
    }

    public record UsersResponse(boolean success, String users) {}
    private record AuthRequest(String uuid, String username, String skin) {}
    public record AuthResponse(boolean success, boolean banned, String code) {}
}
