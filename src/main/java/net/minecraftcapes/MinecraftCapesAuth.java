package net.minecraftcapes;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.minecraftcapes.configs.Configs;
import net.minecraftcapes.listeners.PlayerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Plugin(id = "minecraftcapesauth", name = "MinecraftCapesAuth", version = "0.1.0-SNAPSHOT", url = "https://minecraftcapes.net", description = "Auth Plugin for MinecraftCapes", authors = {"james090500"})
public class MinecraftCapesAuth {

    @Getter private static MinecraftCapesAuth instance;
    private final ProxyServer server;
    @Getter private final Logger logger;
    private final Path dataDirectory;


    @Inject
    public MinecraftCapesAuth(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        //Load the configs
        Configs.loadConfigs(dataDirectory);

        //Listen for events
        server.getEventManager().register(this, new PlayerListener());
    }
}