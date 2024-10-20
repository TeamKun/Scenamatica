package org.kunlab.scenamatica.scenariofile.structures.context;

import lombok.Value;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;
import org.kunlab.scenamatica.structures.StructureMappers;
import org.kunlab.scenamatica.structures.StructureValidators;

import java.util.HashMap;
import java.util.Map;

@Value
public class StageStructureImpl implements StageStructure
{
    @Nullable
    String originalWorldName;
    @NotNull
    WorldType type;
    @Nullable
    Long seed;
    boolean generateStructures;
    @Nullable
    World.Environment environment;
    boolean hardcore;

    @NotNull
    public static Map<String, Object> serialize(@NotNull StageStructure structure)
    {
        Map<String, Object> result = new HashMap<>();

        // オプション項目
        if (structure.getOriginalWorldName() != null)
            result.put(KEY_ORIGINAL_WORLD_NAME, structure.getOriginalWorldName());
        if (structure.getType() != WorldType.NORMAL)
            MapUtils.putIfNotNull(result, KEY_TYPE, structure.getType().name());
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_SEED, structure.getSeed());
        if (!structure.isGenerateStructures())
            result.put(KEY_GENERATE_STRUCTURES, false);
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_ENVIRONMENT, structure.getEnvironment());
        if (structure.isHardcore())
            result.put(KEY_HARDCORE, true);

        return result;
    }

    public static void validate(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_ORIGINAL_WORLD_NAME).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_TYPE).validateIfExists(StructureValidators.enumName(WorldType.class));
        node.get(KEY_SEED).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_GENERATE_STRUCTURES).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_ENVIRONMENT).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_HARDCORE).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
    }

    @NotNull
    public static StageStructure deserialize(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        validate(node);

        return new StageStructureImpl(
                node.get(KEY_ORIGINAL_WORLD_NAME).asString(null),
                node.get(KEY_TYPE).getAs(StructureMappers.enumName(WorldType.class), null),
                node.get(KEY_SEED).asLong(null),
                node.get(KEY_GENERATE_STRUCTURES).asBoolean(false),
                node.get(KEY_ENVIRONMENT).getAs(StructureMappers.enumName(World.Environment.class), null),
                node.get(KEY_HARDCORE).asBoolean(null)
        );
    }
}
