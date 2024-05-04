package org.kunlab.scenamatica.scenariofile.structures.context;

import lombok.Value;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;

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

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkTypeIfContains(map, KEY_ORIGINAL_WORLD_NAME, String.class);
        MapUtils.checkEnumNameIfContains(map, KEY_TYPE, WorldType.class);
        MapUtils.checkTypeIfContains(map, KEY_SEED, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_GENERATE_STRUCTURES, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_ENVIRONMENT, String.class);
        MapUtils.checkTypeIfContains(map, KEY_HARDCORE, Boolean.class);
    }

    @NotNull
    public static StageStructure deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        return new StageStructureImpl(
                MapUtils.getOrNull(map, KEY_ORIGINAL_WORLD_NAME),
                MapUtils.getAsEnumOrDefault(map, KEY_TYPE, WorldType.class, WorldType.NORMAL),
                MapUtils.getAsLongOrNull(map, KEY_SEED),
                MapUtils.getOrDefault(map, KEY_GENERATE_STRUCTURES, true),
                MapUtils.getAsEnumOrNull(map, KEY_ENVIRONMENT, World.Environment.class),
                MapUtils.getOrDefault(map, KEY_HARDCORE, false)
        );
    }
}
