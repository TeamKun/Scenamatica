package net.kunmc.lab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

@UtilityClass
public class PlayerUtils
{
    @NotNull
    public static Player getPlayerOrThrow(@NotNull String name)
    {
        Player player = getPlayerOrNull(name);
        if (player == null)
            throw new IllegalArgumentException("Player " + name + " is not found.");
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
            return noDashesStringToUUIDOrNull(name);
        }
    }

    private static UUID noDashesStringToUUIDOrNull(@NotNull String uuid)
    {
        try
        {
            BigInteger mostSigBits = new BigInteger(uuid.substring(0, 16), 16);
            BigInteger leastSigBits = new BigInteger(uuid.substring(16, 32), 16);
            return new UUID(mostSigBits.longValue(), leastSigBits.longValue());
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    @Nullable
    public static Player getPlayerOrNull(@Nullable String name)
    {
        if (name == null)
            return null;

        Player player = Bukkit.getPlayerExact(name);
        if (player == null)
        {
            UUID mayUUID = toUUIDOrNull(name);
            if (mayUUID != null)
                player = Bukkit.getPlayer(mayUUID);
        }
        return player;
    }
}
