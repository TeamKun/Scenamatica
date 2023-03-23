package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.WorldBean;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Value
public class WorldBeanImpl implements WorldBean
{
    @Nullable
    String originalName;
    @NotNull
    WorldType type;
    @Nullable
    Long seed;
    boolean generateStructures;
    @Nullable
    World.Environment environment;
    boolean hardcore;

    @NotNull
    public static Map<String, Object> serialize(WorldBean bean)
    {
        Map<String, Object> result = new HashMap<>();

        // オプション項目
        if (bean.getOriginalName() != null)
            result.put(KEY_ORIGINAL_NAME, bean.getOriginalName());
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

    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkTypeIfContains(map, KEY_ORIGINAL_NAME, String.class);
        MapUtils.checkEnumNameIfContains(map, KEY_TYPE, WorldType.class);
        MapUtils.checkTypeIfContains(map, KEY_SEED, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_GENERATE_STRUCTURES, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_ENVIRONMENT, String.class);
        MapUtils.checkTypeIfContains(map, KEY_HARDCORE, Boolean.class);
    }

    @NotNull
    public static WorldBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        return new WorldBeanImpl(
                MapUtils.getOrDefault(map, KEY_ORIGINAL_NAME, "world"),
                MapUtils.getAsEnumOrDefault(map, KEY_TYPE, WorldType.class, WorldType.NORMAL),
                MapUtils.getOrNull(map, KEY_SEED),
                MapUtils.getOrDefault(map, KEY_GENERATE_STRUCTURES, true),
                MapUtils.getAsEnumOrNull(map, KEY_ENVIRONMENT, World.Environment.class),
                MapUtils.getOrDefault(map, KEY_HARDCORE, false)
        );
    }
}
