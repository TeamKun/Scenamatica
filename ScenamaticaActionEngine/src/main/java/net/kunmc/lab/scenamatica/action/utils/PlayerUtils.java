package net.kunmc.lab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@UtilityClass
public class PlayerUtils
{
    @NotNull
    public static Player getPlayerOrThrow(@NotNull String name)
    {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null)
        {
            UUID mayUUID = toUUIDOrNull(name);
            if (mayUUID != null)
                player = Bukkit.getPlayer(mayUUID);
            if (player == null)
                throw new IllegalArgumentException("Player not found: " + name);
        }
        return player;
    }

    @Nullable
    public static UUID toUUIDOrNull(@NotNull String name)
    {
        try
        {
            return UUID.fromString(name);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }
    }
}
