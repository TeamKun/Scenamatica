package org.kunlab.scenamatica.commons.utils;

import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@UtilityClass
public class EntityUtils
{
    public static Mapped tryCastMapped(EntityStructure entity, Entity targetEntity)
    {
        if (!((Mapped) entity).canApplyTo(targetEntity))
            throw new IllegalStateException("Entity cannot be applied to mapped entity");

        return entity;
    }

    public static boolean tryCheckIsAdequate(EntityStructure structure, Entity entity)
    {
        return invokeIsAdequate(structure, entity);
    }

    public static boolean checkIsAdequate(EntityStructure entity, Entity targetEntity)
    {
        return ((Mapped) entity).canApplyTo(targetEntity) && invokeIsAdequate(entity, targetEntity);
    }

    public static <T extends Entity> Predicate<T> getEntityPredicate(EntityStructure structure)
    {
        return ((Predicate<T>) ((Mapped) structure)::canApplyTo).and(entity -> invokeIsAdequate(structure, entity))
                .or((entit) -> structure.getUuid() != null && structure.getUuid().equals(entit.getUniqueId()));
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

    public static List<? extends Entity> getEntities(EntityStructure structure, @Nullable Context context, @Nullable Predicate<? super Entity> predicate)
    {
        Predicate<? super Entity> finalPredicate;
        if (predicate == null)
            finalPredicate = getEntityPredicate(structure);
        else
            finalPredicate = getEntityPredicate(structure).and(predicate);

        if (context == null)
        {
            return Bukkit.getWorlds().stream()
                    .flatMap(world -> world.getEntities().stream())
                    .filter(finalPredicate)
                    .collect(Collectors.toList());
        }
        else if (!context.getEntities().isEmpty())
            return context.getEntities().stream()
                    .filter(finalPredicate)
                    .collect(Collectors.toList());

        return context.getStage().getWorld().getEntities().stream()
                .filter(finalPredicate)
                .collect(Collectors.toList());
    }

    public static Entity getEntity(EntityStructure structure, @Nullable Context context, @Nullable Predicate<? super Entity> predicate)
    {
        List<? extends Entity> entities = getEntities(structure, context, predicate);
        if (entities.isEmpty())
            return null;

        return pickEntityOrNull(entities, structure, predicate);
    }

    public static Entity getEntity(EntityStructure structure, ScenarioEngine engine, @Nullable Predicate<? super Entity> predicate)
    {
        return getEntity(structure, engine.getContext(), predicate);
    }

    private static boolean invokeIsAdequate(EntityStructure structure, Entity entity)
    {
        Method m;
        boolean strictNeeded = false;
        try
        {
            m = structure.getClass().getDeclaredMethod("isAdequate", entity.getClass());
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                m = structure.getClass().getDeclaredMethod("isAdequate", entity.getClass(), boolean.class);
                strictNeeded = true;
            }
            catch (NoSuchMethodException e2)
            {
                throw new IllegalStateException("Entity is not mapped");
            }
        }

        try
        {
            return (boolean) (strictNeeded ? m.invoke(structure, entity, false): m.invoke(structure, entity));
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static void invokeApplyTo(EntityStructure structure,  Entity entity)
    {
        ;
        try
        {
            Method m = structure.getClass().getDeclaredMethod("applyTo", entity.getClass(), boolean.class);
            m.invoke(structure, entity, true);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
