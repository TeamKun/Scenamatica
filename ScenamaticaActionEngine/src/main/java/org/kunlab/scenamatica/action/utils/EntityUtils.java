package org.kunlab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class EntityUtils
{
    public static Entity getPlayerOrEntityOrThrow(String target)
    {
        if (target.isEmpty())
            return selectFirstEntity("@e");

        Entity entity = PlayerUtils.getPlayerOrNull(target);
        if (entity != null)
            return entity;

        try
        {
            UUID uuid = UUID.fromString(target);
            entity = Bukkit.getEntity(uuid);

            if (entity != null)
                return entity;
        }
        catch (IllegalArgumentException ignored)
        {
        }

        String normalizedSelector = normalizeSelector(target);
        try
        {
            entity = selectFirstEntity(normalizedSelector);
        }
        catch (IllegalArgumentException ignored)
        {
            throw new IllegalArgumentException("Invalid target specifier: " + target);
        }

        if (entity == null)
            throw new IllegalArgumentException("No entity found: " + target);

        return entity;
    }

    public static List<Entity> selectEntities(String specifier)
    {
        if (specifier.isEmpty())
            return Bukkit.selectEntities(Bukkit.getConsoleSender(), "@e");

        List<Entity> result = new ArrayList<>();

        Entity entity = PlayerUtils.getPlayerOrNull(specifier);
        if (entity != null)
            result.add(entity);

        try
        {
            UUID uuid = UUID.fromString(specifier);
            Entity uuidSelected = Bukkit.getEntity(uuid);
            if (uuidSelected != null)
                return new ArrayList<>(Collections.singleton(uuidSelected));
            // UUID 選択は 1 体しかいないが, UUID で選択される可能性は低いので下の方。
        }
        catch (IllegalArgumentException ignored)
        {
        }

        String normalizedSelector = normalizeSelector(specifier);
        try
        {
            result.addAll(Bukkit.selectEntities(Bukkit.getConsoleSender(), normalizedSelector));
        }
        catch (IllegalArgumentException ignored)
        {
        }

        return result;
    }

    public static String normalizeSelector(String original)
    {
        if (original.startsWith("@"))  // @ から始まる場合はそのまま
            return original;
        else if (original.startsWith("[") && original.endsWith("]"))  // [ から始まり ] の場合は @e をつけてあげる
            return "@e" + original;
        else  // それ以外は @e[...] としてあげる
            return "@e[" + original + "]";
    }

    private static Entity selectFirstEntity(String selector)
    {
        return Bukkit.selectEntities(Bukkit.getConsoleSender(), selector).stream().findFirst().orElse(null);
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
