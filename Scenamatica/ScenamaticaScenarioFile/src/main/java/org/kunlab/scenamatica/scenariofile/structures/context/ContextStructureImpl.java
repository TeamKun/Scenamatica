package org.kunlab.scenamatica.scenariofile.structures.context;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class ContextStructureImpl implements ContextStructure
{
    public static final String KEY_ACTORS = "actors";
    public static final String KEY_ENTITIES = "entities";
    public static final String KEY_STAGE = "stage";

    @NotNull
    List<PlayerStructure> actors;
    @NotNull
    List<EntityStructure> entities;

    StageStructure world;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ContextStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        if (!structure.getActors().isEmpty())
        {
            List<Map<String, Object>> actors = new ArrayList<>();
            for (PlayerStructure player : structure.getActors())
                actors.add(serializer.serialize(player, PlayerStructure.class));

            map.put(KEY_ACTORS, actors);
        }

        if (!structure.getEntities().isEmpty())
        {
            List<Map<String, Object>> entities = new ArrayList<>();
            for (EntityStructure entity : structure.getEntities())
                entities.add(serializer.serialize(entity, EntityStructure.class));

            map.put(KEY_ENTITIES, entities);
        }

        if (structure.getWorld() != null)
            map.put(KEY_STAGE, serializer.serialize(structure.getWorld(), StageStructure.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        if (map.containsKey(KEY_ACTORS))
        {
            Object actors = map.get(KEY_ACTORS);
            if (!(actors instanceof List))
                throw new IllegalArgumentException("actors must be List");

            for (Object player : (List<?>) actors)
                serializer.validate(
                        MapUtils.checkAndCastMap(player),
                        PlayerStructure.class
                );
        }

        if (map.containsKey(KEY_ENTITIES))
        {
            Object entities = map.get(KEY_ENTITIES);
            if (!(entities instanceof List))
                throw new IllegalArgumentException("entities must be List");

            for (Object entity : (List<?>) entities)
                serializer.validate(
                        MapUtils.checkAndCastMap(entity),
                        EntityStructure.class
                );
        }

        if (map.containsKey(KEY_STAGE))
            serializer.validate(
                    MapUtils.checkAndCastMap(map.get(KEY_STAGE)),
                    StageStructure.class
            );
    }

    @NotNull
    public static ContextStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        List<PlayerStructure> actorList = new ArrayList<>();
        if (map.containsKey(KEY_ACTORS) && map.get(KEY_ACTORS) != null)
        {
            for (Object player : (List<?>) map.get(KEY_ACTORS))
                actorList.add(serializer.deserialize(MapUtils.checkAndCastMap(player), PlayerStructure.class));
        }

        List<EntityStructure> entityList = new ArrayList<>();
        if (map.containsKey(KEY_ENTITIES) && map.get(KEY_ENTITIES) != null)
        {
            for (Object entity : (List<?>) map.get(KEY_ENTITIES))
                entityList.add(serializer.deserialize(MapUtils.checkAndCastMap(entity), EntityStructure.class));
        }

        StageStructure world = null;
        if (map.containsKey(KEY_STAGE))
            world = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_STAGE)), StageStructure.class);

        return new ContextStructureImpl(
                actorList,
                entityList,
                world
        );
    }
}
