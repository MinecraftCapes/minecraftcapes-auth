package net.minecraftcapes;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraftcapes.configs.Configs;
import net.minecraftcapes.listeners.MotdListener;
import net.minecraftcapes.listeners.PlayerListener;
import net.minecraftcapes.logging.DisconnectFilter;
import net.minecraftcapes.utils.WebUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "minecraftcapesauth",
        name = "MinecraftCapesAuth",
        version = "1.2.0",
        url = "https://minecraftcapes.net",
        description = "Auth Plugin for MinecraftCapes",
        authors = {"james090500"}
)
public class MinecraftCapesAuth {

    @Getter
    private static MinecraftCapesAuth instance;

    @Getter
    private final Logger logger;

    private final ProxyServer server;
    private final Path dataDirectory;

    @Getter
    private WebUtils.UsersResponse usersResponse;

    @Inject
    public MinecraftCapesAuth(
            ProxyServer server,
            Logger logger,
            @DataDirectory Path dataDirectory
    ) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Config
        Configs.loadConfigs(dataDirectory);

        // Custom Logger
        org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        rootLogger.addFilter(new DisconnectFilter());

        // Listeners
        server.getEventManager().register(this, new MotdListener());
        server.getEventManager().register(this, new PlayerListener());

        // Scheduler
        server.getScheduler()
                .buildTask(this, () -> {
                    WebUtils.UsersResponse newResponse = WebUtils.requestUsers();
                    if(newResponse != null && newResponse.success()) {
                        usersResponse = newResponse;
                    }
                })
                .repeat(60L, TimeUnit.SECONDS)
                .schedule();

        //Commands
        CommandManager commandManager = server.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("minecraftcapesauth")
                .aliases("mcauth")
                .plugin(this)
                .build();

        commandManager.register(commandMeta, (SimpleCommand) invocation -> {
            CommandSource source = invocation.source();

            Configs.loadConfigs(dataDirectory);

            source.sendMessage(Component.text(
                    "[MinecraftCapesAuth] Config reloaded.",
                    NamedTextColor.GREEN
            ));
        });
    }
}