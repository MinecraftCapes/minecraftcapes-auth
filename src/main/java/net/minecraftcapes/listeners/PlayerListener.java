package net.minecraftcapes.listeners;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;
import net.minecraftcapes.utils.SkinUtils;
import net.minecraftcapes.utils.WebUtils;

public final class PlayerListener {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Subscribe
    public EventTask onLogin(LoginEvent event) {
        Configs.Settings settings = Configs.settings;
        // Fail closed even if request construction or response rendering fails.
        event.setResult(ResultedEvent.ComponentResult.denied(settings.errorMessage));

        var player = event.getPlayer();
        return EventTask.resumeWhenComplete(
                    WebUtils.requestAuth(settings, player.getUniqueId(), player.getUsername(), SkinUtils.getUrl(player.getGameProfile())
                )
                .thenAccept(auth -> event.setResult(ResultedEvent.ComponentResult.denied(response(settings, auth))))
                .exceptionally(error -> {
                    MinecraftCapesAuth.getInstance().getLogger().warn("Unable to build authentication response", error);
                    return null;
                }));
    }

    Component response(Configs.Settings settings, WebUtils.AuthResponse auth) {
        if (auth == null) return settings.errorMessage;
        if (auth.banned()) return settings.blockedMessage;
        if (!auth.success() || auth.code() == null || auth.code().isBlank()) return settings.errorMessage;
        return settings.layout(MINI_MESSAGE.deserialize(settings.AUTH_MESSAGE,
                Placeholder.unparsed("code", auth.code())));
    }
}
