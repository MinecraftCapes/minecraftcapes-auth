package net.minecraftcapes;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
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
import java.util.concurrent.atomic.AtomicBoolean;

@Plugin(id = "minecraftcapesauth", name = "MinecraftCapesAuth", version = "1.2.1",
        url = "https://minecraftcapes.net", description = "Auth Plugin for MinecraftCapes",
        authors = {"james090500"})
public final class MinecraftCapesAuth {
    private static MinecraftCapesAuth instance;
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final MotdListener motd = new MotdListener();
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private final DisconnectFilter disconnectFilter = new DisconnectFilter();
    private org.apache.logging.log4j.core.Logger rootLogger;
    private ScheduledTask refreshTask;

    @Inject
    public MinecraftCapesAuth(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;
    }

    public static MinecraftCapesAuth getInstance() { return instance; }
    public Logger getLogger() { return logger; }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!Configs.loadConfigs(dataDirectory)) {
            throw new IllegalStateException("MinecraftCapesAuth requires valid settings.toml");
        }
        if (LogManager.getRootLogger() instanceof org.apache.logging.log4j.core.Logger root) {
            rootLogger = root;
            root.addFilter(disconnectFilter);
        }
        server.getEventManager().register(this, motd);
        server.getEventManager().register(this, new PlayerListener());
        refreshTask = server.getScheduler().buildTask(this, this::refreshUsers)
                .repeat(60L, TimeUnit.SECONDS).schedule();

        var commands = server.getCommandManager();
        commands.register(commands.metaBuilder("minecraftcapesauth").aliases("mcauth").plugin(this).build(),
                new SimpleCommand() {
                    @Override
                    public boolean hasPermission(Invocation invocation) {
                        return invocation.source().hasPermission("minecraftcapesauth.reload");
                    }

                    @Override
                    public void execute(Invocation invocation) {
                        boolean loaded = Configs.loadConfigs(dataDirectory);
                        if (loaded) motd.reload();
                        invocation.source().sendMessage(Component.text(loaded
                                        ? "[MinecraftCapesAuth] Config reloaded."
                                        : "[MinecraftCapesAuth] Reload failed; previous config retained. See logs.",
                                loaded ? NamedTextColor.GREEN : NamedTextColor.RED));
                    }
                });
    }

    private void refreshUsers() {
        if (!refreshing.compareAndSet(false, true)) return;
        try {
            WebUtils.requestUsers().thenAccept(response -> {
                if (response != null && response.success()) motd.updateUsers(response.users());
            }).whenComplete((ignored, error) -> {
                refreshing.set(false);
                if (error != null) logger.warn("Unable to update user count", error);
            });
        } catch (RuntimeException error) {
            refreshing.set(false);
            logger.warn("Unable to request user count", error);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (refreshTask != null) refreshTask.cancel();
        WebUtils.close();
        if (rootLogger != null) {
            rootLogger.getContext().getConfiguration().getRootLogger().removeFilter(disconnectFilter);
            rootLogger.getContext().updateLoggers();
        }
    }
}
