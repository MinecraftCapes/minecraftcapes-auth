package net.minecraftcapes.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraftcapes.MinecraftCapesAuth;
import net.minecraftcapes.utils.WebUtils;

public class PlayerListener {

    /**
     * This runs on player login
     * @param event The LoginEvent
     */
    @Subscribe
    public void onPreLogin(LoginEvent event) {
        MinecraftCapesAuth.getInstance().getLogger().info(event.getPlayer().getUsername(), "is requesting an auth code");

        //The Auth Data
        WebUtils.AuthData authData = WebUtils.requestAuthData(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
        Component playerResponse = this.getLoginResponse(authData);

        event.setResult(ResultedEvent.ComponentResult.denied(playerResponse));

        MinecraftCapesAuth.getInstance().getLogger().info(event.getPlayer().getUsername(), "has been served");
    }

    /**
     * Get the players response message
     * @param authData The auth data from the API
     * @return The players response in component form
     */
    private Component getLoginResponse(WebUtils.AuthData authData) {
        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&c&lSomething went wrong\nPlease reconnect to try again");
        Component blockedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&c&lYour account has been banned for violating our terms of service.");
        Component authMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&fYour authorization code is\n&c&l\u00BB&f " + authData.code + " &c&l\u00AB`");

        //If we have no data, lets tell the user
        if(authData == null) return errorMessage;

        //If we have data we can return a code, an error or a banned message
        if(authData.success) {
            return (authData.code == null) ? errorMessage : authMessage;
        } else if(authData.banned) {
            return blockedMessage;
        }

        //All else fails, we return an error
        return errorMessage;
    }
}
