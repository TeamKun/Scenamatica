package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.context.WorldBean;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Value
public class WorldBeanImpl implements WorldBean
{
    @NotNull
    String name;
    @NotNull
    WorldType type;
    @Nullable
    Long seed;
    @Nullable
    Boolean generateStructures;
    @Nullable
    World.Environment environment;
    boolean hardcore;

    @NotNull
    public static Map<String, Object> serialize(WorldBean bean)
    {
        Map<String, Object> result = new HashMap<>();

        // 必須項目
        result.put(KEY_NAME, bean.getName());

        // オプション項目
        if (bean.getType() != WorldType.NORMAL)
            MapUtils.putIfNotNull(result, KEY_TYPE, bean.getType().name());
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_SEED, bean.getSeed());
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_GENERATE_STRUCTURES, bean.getGenerateStructures());
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_ENVIRONMENT, bean.getEnvironment());
        if (bean.isHardcore())
            result.put(KEY_HARDCORE, true);

        return result;
    }

    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, KEY_NAME);
        MapUtils.checkTypeIfContains(map, KEY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_SEED, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_GENERATE_STRUCTURES, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_ENVIRONMENT, String.class);
        MapUtils.checkTypeIfContains(map, KEY_HARDCORE, Boolean.class);
    }

    @NotNull
    public static WorldBeanImpl deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        return new WorldBeanImpl(
                (String) map.get(KEY_NAME),
                MapUtils.getAsEnumOrDefault(map, KEY_TYPE, WorldType.class, WorldType.NORMAL),
                MapUtils.getOrNull(map, KEY_SEED),
                MapUtils.getOrNull(map, KEY_GENERATE_STRUCTURES),
                MapUtils.getAsEnumOrNull(map, KEY_ENVIRONMENT, World.Environment.class),
                MapUtils.getOrDefault(map, KEY_HARDCORE, false)
        );
    }
}
