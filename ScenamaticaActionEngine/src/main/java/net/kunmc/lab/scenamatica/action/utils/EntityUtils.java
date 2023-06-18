package net.kunmc.lab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

@UtilityClass
public class EntityUtils
{
    public static Entity getPlayerOrEntityOrThrow(String target)
    {
        Entity entity = PlayerUtils.getPlayerOrNull(target);
        if (entity != null)
            return entity;

        entity = Bukkit.selectEntities(Bukkit.getConsoleSender(), target).stream().findFirst().orElse(null);
        if (entity != null)
            return entity;

        UUID uuid;
        try
        {
            uuid = UUID.fromString(target);
        }
        catch (IllegalArgumentException ignored)
        {
            throw new IllegalArgumentException("Invalid target: " + target);
        }

        entity = Bukkit.getEntity(uuid);

        if (entity == null)
            throw new IllegalArgumentException("Invalid target: " + target);

        return entity;
    }

    public static Entity getPlayerOrEntityOrNull(String target)
    {
        try
        {
            return getPlayerOrEntityOrThrow(target);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }
    }

}
