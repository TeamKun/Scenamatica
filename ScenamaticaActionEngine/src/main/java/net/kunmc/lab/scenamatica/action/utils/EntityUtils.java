package net.kunmc.lab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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

    public static Actor getActorOrThrow(ActorManager manager, Player bukkitEntity)
    {
        if (!manager.isActor(bukkitEntity))
            throw new IllegalArgumentException("Only actor is allowed in this action: Invalid target: " + bukkitEntity.getName());

        Actor actor = manager.getByUUID(bukkitEntity.getUniqueId());
        if (actor == null)
            throw new IllegalArgumentException("No actor found: " + bukkitEntity.getName());

        return actor;
    }

    public static Actor getActorOrThrow(ScenarioEngine engine, Player bukkitEntity)
    {
        return getActorOrThrow(engine.getManager().getRegistry().getContextManager().getActorManager(), bukkitEntity);
    }
}
