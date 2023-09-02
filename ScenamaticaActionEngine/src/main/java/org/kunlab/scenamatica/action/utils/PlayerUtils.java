package org.kunlab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.UUIDUtil;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
    public static Player getPlayerOrNull(@Nullable String name)
    {
        if (name == null)
            return null;

        Player player = Bukkit.getPlayerExact(name);
        if (player == null)
        {
            UUID mayUUID = UUIDUtil.toUUIDOrNull(name);
            if (mayUUID != null)
                player = Bukkit.getPlayer(mayUUID);
        }
        return player;
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

    public static Actor getActorByStringOrThrow(ScenarioEngine engine, String target)
    {
        Actor actor = engine.getManager().getRegistry().getContextManager().getActorManager().getByName(target);
        if (actor == null)
        {
            UUID mayUUID = UUIDUtil.toUUIDOrNull(target);
            if (mayUUID != null)
                actor = engine.getManager().getRegistry().getContextManager().getActorManager().getByUUID(mayUUID);
        }

        if (actor == null)
            throw new IllegalArgumentException("Invalid target: " + target);

        return actor;
    }
}
