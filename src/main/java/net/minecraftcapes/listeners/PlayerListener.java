package net.minecraftcapes.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;
import net.minecraftcapes.utils.WebUtils;

public class PlayerListener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Component errorMessage;
    private final Component blockedMessage;

    public PlayerListener() {
        this.errorMessage = MINI_MESSAGE.deserialize(Configs.settings.ERROR_MESSAGE);
        this.blockedMessage = MINI_MESSAGE.deserialize(Configs.settings.BLOCKED_MESSAGE);
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        event.setResult(ResultedEvent.ComponentResult.denied(buildResponse(event)));
    }

    private Component buildResponse(LoginEvent event) {
        String username = event.getPlayer().getUsername();

        MinecraftCapesAuth.getInstance().getLogger().info(
                "{} is requesting an auth code",
                username
        );

        WebUtils.AuthResponse authResponse = WebUtils.requestAuth(
                event.getPlayer().getUniqueId(),
                username
        );

        Component response = switch (authResponse) {
            case null -> errorMessage;
            case WebUtils.AuthResponse auth when auth.success() ->
                    MINI_MESSAGE.deserialize(
                            Configs.settings.AUTH_MESSAGE,
                            Placeholder.parsed("code", auth.code())
                    );
            case WebUtils.AuthResponse auth when auth.banned() -> blockedMessage;
            default -> errorMessage;
        };

        MinecraftCapesAuth.getInstance().getLogger().info(
                "{} has been served",
                username
        );

        return MINI_MESSAGE.deserialize(
                Configs.settings.RESPONSE_LAYOUT,
                Placeholder.component("response", response)
        );
    }
}