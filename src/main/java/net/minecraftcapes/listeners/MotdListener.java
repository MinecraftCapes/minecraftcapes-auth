package net.minecraftcapes.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.configs.Configs;
import net.minecraftcapes.utils.WebUtils;

public class MotdListener {

	@Subscribe
	public void onPing(ProxyPingEvent event) {
		WebUtils.UsersResponse usersResponse = MinecraftCapesAuth.getInstance().getUsersResponse();
		if(
				usersResponse != null &&
						usersResponse.success()
		) {
			MiniMessage miniMessage = MiniMessage.miniMessage();

			// Pretty User Count
			String users = String.format("%,d", Integer.parseInt(usersResponse.users()));

			// Apply placeholders
			Component finalMessage = miniMessage.deserialize(
					Configs.settings.MOTD,
					Placeholder.parsed("users", users)
			);

			ServerPing ping = event.getPing().asBuilder().description(finalMessage).build();
			event.setPing(ping);
		}
	}

}
