package net.minecraftcapes.configs;

import com.moandjiezana.toml.Toml;
import net.minecraftcapes.MinecraftCapesAuth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configs {

    public static Settings settings;

    /**
     * Loads the config files.
     * @param dataDirectory
     */
    public static void loadConfigs(Path dataDirectory) {
        //Create data directory
        if(!dataDirectory.toFile().exists()) {
            dataDirectory.toFile().mkdir();
        }

        //Find the settings file otherwise create it
        File settingsFile = new File(dataDirectory + "/settings.toml");
        if(!settingsFile.exists()) {
            try (InputStream in = MinecraftCapesAuth.class.getResourceAsStream("/settings.toml")) {
                Files.copy(in, new File(dataDirectory + "/settings.toml").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Configs.settings = new Toml().read(settingsFile).to(Settings.class);
    }

    public class Settings {
        public String API_ENDPOINT;
        public String API_KEY;
    }
}