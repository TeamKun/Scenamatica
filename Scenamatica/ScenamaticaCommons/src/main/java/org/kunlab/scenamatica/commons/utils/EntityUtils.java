package org.kunlab.scenamatica.commons.utils;

import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@UtilityClass
public class EntityUtils
{
    public static <T extends Entity> Predicate<T> getEntityPredicate(EntityStructure structure)
    {
        return ((Predicate<T>) structure::canApplyTo).and(structure::isAdequate)
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
}
