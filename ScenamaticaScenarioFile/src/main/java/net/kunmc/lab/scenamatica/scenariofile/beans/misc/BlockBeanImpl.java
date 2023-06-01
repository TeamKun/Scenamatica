package net.kunmc.lab.scenamatica.scenariofile.beans.misc;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Value
@AllArgsConstructor
public class BlockBeanImpl implements BlockBean
{
    @NotNull
    Material type;
    int x;
    int y;
    int z;
    @NotNull
    Map<String, Object> metadata;
    int lightLevel;  // 0-15
    @Nullable
    Biome biome;

    public static BlockBean fromBlock(Block block)
    {
        return new BlockBeanImpl(
                block.getType(),
                block.getX(),
                block.getY(),
                block.getZ(),
                new HashMap<>(),
                block.getLightLevel(),
                block.getBiome()
        );
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull BlockBean blockBean)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_BLOCK_TYPE, blockBean.getType().name());
        map.put(KEY_BLOCK_X, blockBean.getX());
        map.put(KEY_BLOCK_Y, blockBean.getY());
        map.put(KEY_BLOCK_Z, blockBean.getZ());
        if (blockBean.getBiome() != null)
            map.put(KEY_BIOME, blockBean.getBiome().name());

        if (!blockBean.getMetadata().isEmpty())
            map.put(KEY_METADATA, blockBean.getMetadata());
        if (blockBean.getLightLevel() != 0)
            map.put(KEY_LIGHT_LEVEL, blockBean.getLightLevel());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumName(map, KEY_BLOCK_TYPE, Material.class);
        MapUtils.checkType(map, KEY_BLOCK_X, Integer.class);
        MapUtils.checkType(map, KEY_BLOCK_Y, Integer.class);
        MapUtils.checkType(map, KEY_BLOCK_Z, Integer.class);

        if (map.containsKey(KEY_LIGHT_LEVEL))
        {
            MapUtils.checkType(map, KEY_LIGHT_LEVEL, Integer.class);
            int lightLevel = (int) map.get(KEY_LIGHT_LEVEL);
            if (lightLevel < 0 || lightLevel > 15)
                throw new IllegalArgumentException("lightLevel must be between 0 and 15");
        }

        MapUtils.checkEnumNameIfContains(map, KEY_BIOME, Biome.class);

        if (map.containsKey(KEY_METADATA))
            MapUtils.checkAndCastMap(
                    map.get(KEY_METADATA),
                    String.class,
                    Object.class
            );
    }

    @NotNull
    public static BlockBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        return new BlockBeanImpl(
                Material.valueOf((String) map.get(KEY_BLOCK_TYPE)),
                (int) map.get(KEY_BLOCK_X),
                (int) map.get(KEY_BLOCK_Y),
                (int) map.get(KEY_BLOCK_Z),
                map.containsKey(KEY_METADATA) ? MapUtils.checkAndCastMap(
                        map.get(KEY_METADATA),
                        String.class,
                        Object.class
                ): new HashMap<>(),
                (byte) (int) MapUtils.getOrDefault(map, KEY_LIGHT_LEVEL, 0),
                MapUtils.getAsEnumOrNull(map, KEY_BIOME, Biome.class)
        );
    }

}
