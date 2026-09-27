package net.minecraftcapes.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraftcapes.configs.Configs;

import java.util.Locale;

public final class MotdListener {
    private volatile Component description;
    private Long users;

    public synchronized void updateUsers(String value) {
        long count = Long.parseLong(value);
        if (count < 0) throw new IllegalArgumentException("Negative user count");
        if (users == null || users != count) {
            users = count;
            reload();
        }
    }

    public synchronized void reload() {
        if (users != null) {
            description = MiniMessage.miniMessage().deserialize(Configs.settings.MOTD,
                    Placeholder.unparsed("users", String.format(Locale.UK, "%,d", users)));
        }
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        Component cached = description;
        if (cached != null) {
            event.setPing(event.getPing().asBuilder().description(cached).build());
        }
    }
}
