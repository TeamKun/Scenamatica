package org.kunlab.scenamatica.commons.utils;

import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@UtilityClass
public class EntityUtils
{
    public static <T extends Entity> Mapped<T> tryCastMapped(EntityStructure entity, Entity targetEntity)
    {
        if (!(entity instanceof Mapped))
            throw new IllegalStateException("Entity is not mapped");

        // noinspection unchecked
        Mapped<T> mapped = (Mapped<T>) entity;
        if (!mapped.canApplyTo(targetEntity))
            throw new IllegalStateException("Entity cannot be applied to mapped entity");

        return mapped;
    }

    public static boolean tryCheckIsAdequate(EntityStructure structure, Entity entity)
    {
        return tryCastMapped(structure, entity).isAdequate(entity);
    }

    @SuppressWarnings("rawtypes")
    public static boolean checkIsAdequate(EntityStructure entity, Entity targetEntity)
    {
        if (!(entity instanceof Mapped))
            throw new IllegalStateException("Entity is not mapped");

        Mapped mapped = (Mapped) entity;
        return mapped.canApplyTo(targetEntity) && mapped.isAdequate(targetEntity);
    }

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

    public static <T extends Entity> Predicate<T> getEntityPredicate(EntityStructure structure)
    {
        if (!(structure instanceof Mapped))
            throw new IllegalStateException("Entity is not mapped");

        // noinspection unchecked
        Mapped<T> mapped = (Mapped<T>) structure;

        return ((Predicate<T>) mapped::canApplyTo).and(mapped::isAdequate);
    }

    private static Entity pickEntityOrNull(List<? extends Entity> entities, EntityStructure structure, @Nullable Predicate<? super Entity> predicate)
    {
        Predicate<? super Entity> finalPredicate;
        if (predicate == null)
            finalPredicate = getEntityPredicate(structure);
        else
            finalPredicate = getEntityPredicate(structure).and(predicate);

        return entities.stream()
                .filter(finalPredicate)
                .findFirst()
                .orElse(null);
    }

    public static Entity getEntity(EntityStructure structure, Context context, @Nullable Predicate<? super Entity> predicate)
    {
        Entity result = null;
        if (!(context.getEntities() == null || context.getEntities().isEmpty()))
            result = pickEntityOrNull(context.getEntities(), structure, predicate);

        if (result == null)
            result = pickEntityOrNull(context.getStage().getEntities(), structure, predicate);

        return result;
    }

    public static Entity getEntity(EntityStructure structure, ScenarioEngine engine, @Nullable Predicate<? super Entity> predicate)
    {
        return getEntity(structure, engine.getContext(), predicate);
    }
}
