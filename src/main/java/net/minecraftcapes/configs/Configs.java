package net.minecraftcapes.configs;

import com.moandjiezana.toml.Toml;
import net.minecraftcapes.MinecraftCapesAuth;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configs {

    public static Settings settings;

    public static void loadConfigs(Path dataDirectory) {
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

            settings = new Toml()
                    .read(settingsPath.toFile())
                    .to(Settings.class);

        } catch (IOException e) {
            MinecraftCapesAuth.getInstance().getLogger().error(
                    "Failed to load config files",
                    e
            );
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
    }
}