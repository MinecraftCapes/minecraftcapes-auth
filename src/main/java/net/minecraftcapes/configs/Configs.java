package net.minecraftcapes.configs;

import com.moandjiezana.toml.Toml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraftcapes.MinecraftCapesAuth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Configs {

    public static volatile Settings settings;

    public static boolean loadConfigs(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);

            Path settingsPath = dataDirectory.resolve("settings.toml");

            if (Files.notExists(settingsPath)) {
                try (InputStream in = MinecraftCapesAuth.class.getResourceAsStream("/settings.toml")) {
                    if (in == null) {
                        throw new IOException("Default settings.toml resource not found");
                    }

                    Files.copy(in, settingsPath);
                }
            }

            Settings loaded = new Toml()
                    .read(settingsPath.toFile())
                    .to(Settings.class);
            loaded.prepare();
            settings = loaded;
            return true;

        } catch (IOException | RuntimeException e) {
            MinecraftCapesAuth.getInstance().getLogger().error(
                    "Failed to load config files",
                    e
            );
            return false;
        }
    }

    public static class Settings {
        public String AUTH_ENDPOINT;
        public String USERS_ENDPOINT;
        public String API_KEY;
        public String MOTD;
        public String ERROR_MESSAGE;
        public String BLOCKED_MESSAGE;
        public String AUTH_MESSAGE;
        public String RESPONSE_LAYOUT;

        public transient Component errorMessage;
        public transient Component blockedMessage;

        private void prepare() {
            validateEndpoint(AUTH_ENDPOINT);
            validateEndpoint(USERS_ENDPOINT);
            Objects.requireNonNull(API_KEY, "API_KEY is required");
            Objects.requireNonNull(MOTD, "MOTD is required");
            Objects.requireNonNull(AUTH_MESSAGE, "AUTH_MESSAGE is required");
            Objects.requireNonNull(RESPONSE_LAYOUT, "RESPONSE_LAYOUT is required");
            errorMessage = layout(MiniMessage.miniMessage().deserialize(ERROR_MESSAGE));
            blockedMessage = layout(MiniMessage.miniMessage().deserialize(BLOCKED_MESSAGE));
        }

        public Component layout(Component response) {
            return MiniMessage.miniMessage().deserialize(RESPONSE_LAYOUT,
                    Placeholder.component("response", response));
        }

        private static void validateEndpoint(String value) {
            URI uri = URI.create(value);
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("API endpoints must be absolute HTTP(S) URLs");
            }
        }
    }
}
