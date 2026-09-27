package net.minecraftcapes.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.velocitypowered.api.util.GameProfile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SkinUtils {
    private SkinUtils() {}

    public static String getUrl(GameProfile profile) {
        for (var property : profile.getProperties()) {
            if (!property.getName().equalsIgnoreCase("textures")) continue;
            try {
                String decoded = new String(Base64.getDecoder().decode(property.getValue()), StandardCharsets.UTF_8);
                JsonElement value = JsonParser.parseString(decoded);
                for (String key : new String[]{"textures", "SKIN", "url"}) {
                    if (value == null || !value.isJsonObject()) return null;
                    value = value.getAsJsonObject().get(key);
                }
                return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                        ? value.getAsString() : null;
            } catch (IllegalArgumentException | JsonParseException error) {
                return null;
            }
        }
        return null;
    }
}
