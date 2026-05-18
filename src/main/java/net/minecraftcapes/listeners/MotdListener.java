package net.minecraftcapes.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;

public class MotdListener {

	@Subscribe
	public void onPing(ProxyPingEvent event) {
		if(
				MinecraftCapesAuth.getInstance().getUsersResponse() != null &&
				MinecraftCapesAuth.getInstance().getUsersResponse().success()
		) {
			MiniMessage miniMessage = MiniMessage.miniMessage();

			// Apply placeholders
			Component finalMessage = miniMessage.deserialize(
					Configs.settings.MOTD,
					Placeholder.parsed("users", MinecraftCapesAuth.getInstance().getUsersResponse().users())
			);

			ServerPing ping = event.getPing().asBuilder().description(finalMessage).build();
			event.setPing(ping);
		}
	}

}
