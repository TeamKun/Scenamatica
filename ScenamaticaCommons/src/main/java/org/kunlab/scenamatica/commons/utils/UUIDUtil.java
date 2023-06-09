package org.kunlab.scenamatica.commons.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public class UUIDUtil
{
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

    @NotNull
    public static UUID toUUIDOrThrow(@NotNull String name)
    {
        UUID uuid = toUUIDOrNull(name);
        if (uuid == null)
            throw new IllegalArgumentException("Invalid UUID: " + name);
        return uuid;
    }

    private static UUID noDashesStringToUUIDOrNull(@NotNull String uuid)
    {
        if (uuid.contains("-"))
            return null;
        else if (uuid.length() != 32)
            return null;

        try
        {
            BigInteger mostSigBits = new BigInteger(uuid.substring(0, 16), 16);
            BigInteger leastSigBits = new BigInteger(uuid.substring(16, 32), 16);
            return new UUID(mostSigBits.longValue(), leastSigBits.longValue());
        }
        catch (NumberFormatException | StringIndexOutOfBoundsException ignored)
        {
            return null;
        }
    }
}
