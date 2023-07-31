package org.kunlab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageBean;

import java.util.HashMap;
import java.util.Map;

@Value
public class StageBeanImpl implements StageBean
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
    public static Map<String, Object> serialize(@NotNull StageBean bean)
    {
        Map<String, Object> result = new HashMap<>();

        // オプション項目
        if (bean.getOriginalWorldName() != null)
            result.put(KEY_ORIGINAL_WORLD_NAME, bean.getOriginalWorldName());
        if (bean.getType() != WorldType.NORMAL)
            MapUtils.putIfNotNull(result, KEY_TYPE, bean.getType().name());
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_SEED, bean.getSeed());
        if (!bean.isGenerateStructures())
            result.put(KEY_GENERATE_STRUCTURES, false);
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_ENVIRONMENT, bean.getEnvironment());
        if (bean.isHardcore())
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
    public static StageBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        return new StageBeanImpl(
                MapUtils.getOrNull(map, KEY_ORIGINAL_WORLD_NAME),
                MapUtils.getAsEnumOrDefault(map, KEY_TYPE, WorldType.class, WorldType.NORMAL),
                MapUtils.getAsLongOrNull(map, KEY_SEED),
                MapUtils.getOrDefault(map, KEY_GENERATE_STRUCTURES, true),
                MapUtils.getAsEnumOrNull(map, KEY_ENVIRONMENT, World.Environment.class),
                MapUtils.getOrDefault(map, KEY_HARDCORE, false)
        );
    }
}
