package org.kunlab.scenamatica.scenariofile.structures.context;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class ContextStructureImpl implements ContextStructure
{
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

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        if (node.containsKey(KEY_ACTORS))
        {
            StructuredYamlNode actors = node.get(KEY_ACTORS);
            actors.ensureTypeOf(YAMLNodeType.LIST);

            for (StructuredYamlNode player : actors.asList())
                serializer.validate(player, PlayerStructure.class);
        }

        if (node.containsKey(KEY_ENTITIES))
        {
            StructuredYamlNode entities = node.get(KEY_ENTITIES);
            entities.ensureTypeOf(YAMLNodeType.LIST);

            for (StructuredYamlNode entity : entities.asList())
                serializer.validate(entity, EntityStructure.class);
        }

        if (node.containsKey(KEY_STAGE))
            serializer.validate(node.get(KEY_STAGE), StageStructure.class);
    }

    @NotNull
    public static ContextStructure deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        List<PlayerStructure> actorList = new ArrayList<>();
        StructuredYamlNode actors = node.get(KEY_ACTORS);
        if (node.containsKey(KEY_ACTORS) && actors != null)
        {
            for (StructuredYamlNode player : actors.asList())
                actorList.add(serializer.deserialize(player, PlayerStructure.class));
        }

        List<EntityStructure> entityList = new ArrayList<>();
        StructuredYamlNode entities = node.get(KEY_ACTORS);
        if (node.containsKey(KEY_ENTITIES) && node.get(KEY_ENTITIES) != null)
        {
            for (StructuredYamlNode entity : node.get(KEY_ENTITIES).asList())
                entityList.add(serializer.deserialize(entity, EntityStructure.class));
        }

        StageStructure world = null;
        if (node.containsKey(KEY_STAGE))
            world = serializer.deserialize(node.get(KEY_STAGE), StageStructure.class);

        return new ContextStructureImpl(
                actorList,
                entityList,
                world
        );
    }
}
