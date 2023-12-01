package org.kunlab.scenamatica.scenariofile.structures.misc;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.BlockDataParser;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
@AllArgsConstructor
public class BlockStructureImpl implements BlockStructure
{
    Material type;
    Location location;
    @NotNull
    Map<String, Object> metadata;
    Integer lightLevel;  // 0-15
    Biome biome;
    Map<String, Object> blockData;
    Byte blockState;

    public static BlockStructure fromBlock(Block block)
    {
        // noinspection deprecation
        return new BlockStructureImpl(
                block.getType(),
                block.getLocation(),
                new HashMap<>(),
                (int) block.getLightLevel(),
                block.getBiome(),
                BlockDataParser.toMap(block.getBlockData()),
                block.getState().getData().getData()
        );
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull BlockStructure blockStructure)
    {
        Map<String, Object> map = new HashMap<>();

        if (blockStructure.getType() != null)
            map.put(KEY_BLOCK_TYPE, blockStructure.getType().name());
        MapUtils.putLocationIfNotNull(map, KEY_BLOCK_LOCATION, blockStructure.getLocation());

        if (blockStructure.getBiome() != null)
            map.put(KEY_BIOME, blockStructure.getBiome().name());

        if (!blockStructure.getMetadata().isEmpty())
            map.put(KEY_METADATA, blockStructure.getMetadata());
        if (blockStructure.getLightLevel() != null)
            map.put(KEY_LIGHT_LEVEL, blockStructure.getLightLevel());

        if (!blockStructure.getBlockData().isEmpty())
            map.put(KEY_BLOCK_DATA, blockStructure.getBlockData());

        if (blockStructure.getBlockState() != null)
            map.put(KEY_BLOCK_STATE, blockStructure.getBlockState());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkMaterialNameIfContains(map, KEY_BLOCK_TYPE);
        MapUtils.checkLocationIfContains(map, KEY_BLOCK_LOCATION);

        if (map.containsKey(KEY_LIGHT_LEVEL))
        {
            MapUtils.checkType(map, KEY_LIGHT_LEVEL, Integer.class);
            int lightLevel = (int) map.get(KEY_LIGHT_LEVEL);
            if (lightLevel < 0 || lightLevel > 15)
                throw new IllegalArgumentException("lightLevel must be between 0 and 15");
        }

        MapUtils.checkEnumNameIfContains(map, KEY_BIOME, Biome.class);

        if (map.containsKey(KEY_METADATA))
            MapUtils.checkAndCastMap(map.get(KEY_METADATA));

        if (map.containsKey(KEY_BLOCK_DATA))
            MapUtils.checkAndCastMap(map.get(KEY_BLOCK_DATA));

        MapUtils.checkTypeIfContains(map, KEY_BLOCK_STATE, Byte.class);
    }

    @NotNull
    public static BlockStructure deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        Material material = Utils.searchMaterial(MapUtils.getOrNull(map, KEY_BLOCK_TYPE));

        return new BlockStructureImpl(
                material,
                MapUtils.getAsLocationOrNull(map, KEY_BLOCK_LOCATION),
                MapUtils.getAndCastOrEmptyMap(map, KEY_METADATA),
                MapUtils.getOrNull(map, KEY_LIGHT_LEVEL),
                MapUtils.getAsEnumOrNull(map, KEY_BIOME, Biome.class),
                MapUtils.getAndCastOrEmptyMap(map, KEY_METADATA),
                MapUtils.getOrNull(map, KEY_BLOCK_STATE)
        );
    }

    public BlockStructure changeLocation(Location location)
    {
        return new BlockStructureImpl(
                this.type,
                location,
                this.metadata,
                this.lightLevel,
                this.biome,
                this.blockData,
                this.blockState
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof BlockStructureImpl))
            return false;

        BlockStructureImpl blockStructure = (BlockStructureImpl) o;
        return this.getType() == blockStructure.getType()

                && Objects.equals(this.getLocation(), blockStructure.getLocation())
                && Objects.equals(this.getMetadata(), blockStructure.getMetadata())
                && Objects.equals(this.getLightLevel(), blockStructure.getLightLevel())
                && this.getBiome() == blockStructure.getBiome()
                && MapUtils.equals(this.getBlockData(), blockStructure.getBlockData())
                && Objects.equals(this.getBlockState(), blockStructure.getBlockState());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(
                this.getType(),
                this.getLocation(),
                this.getMetadata(),
                this.getLightLevel(),
                this.getBiome(),
                this.getBlockData(),
                this.getBlockState()
        );
    }

    @Override
    public String toString()
    {
        return "Block{" +
                "type=" + this.type +
                ", location=" + this.location +
                ", metadata=" + this.metadata +
                ", lightLevel=" + this.lightLevel +
                ", biome=" + this.biome +
                ", blockData=" + this.blockData +
                ", blockState=" + this.blockState +
                '}';
    }

    @Override
    public Block create()
    {
        if (this.location == null)
            throw new IllegalStateException("location is null");
        else if (this.location.getWorld() == null)
            throw new IllegalStateException("location.world is null");

        Block block = this.location.getBlock();
        this.applyTo(block);

        return block;
    }

    @Override
    public void applyTo(Block block)
    {
        Location location = this.location;

        if (this.type != null)
            block.setType(this.type, true);
        if (this.biome != null)
            block.getWorld().setBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), this.biome);
        if (!this.metadata.isEmpty())
        {
            ClassLoader classLoader = BlockStructure.class.getClassLoader();
            if (!(classLoader instanceof PluginClassLoader))
                throw new IllegalStateException("ClassLoader is not PluginClassLoader");
            Plugin owningPlugin = ((PluginClassLoader) classLoader).getPlugin();

            for (Map.Entry<String, Object> entry : this.metadata.entrySet())
                block.setMetadata(
                        entry.getKey(),
                        new FixedMetadataValue(owningPlugin, entry.getValue())
                );
        }

        if (!this.blockData.isEmpty())
        {
            BlockData data = BlockDataParser.fromMap(block.getType(), this.getBlockData());
            block.setBlockData(block.getBlockData().merge(data), true);
        }

        if (this.blockState != null)
        {
            BlockState state = block.getState();
            // noinspection deprecation
            state.getData().setData(this.blockState);
            state.update(true, true);
        }
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof Block;
    }

    @Override
    public boolean isAdequate(Block block, boolean strict)
    {
        if (!(this.type == null || this.type == block.getType()))
            return false;

        Location expectedLoc = this.location;
        Location actualLoc = block.getLocation();
        if (expectedLoc != null)  // TODO: Refactor: to LocationStructure
        {
            if (Double.doubleToLongBits(expectedLoc.getX()) != Double.doubleToLongBits(actualLoc.getX()))
                return false;
            if (Double.doubleToLongBits(expectedLoc.getY()) != Double.doubleToLongBits(actualLoc.getY()))
                return false;
            if (Double.doubleToLongBits(expectedLoc.getZ()) != Double.doubleToLongBits(actualLoc.getZ()))
                return false;

            if (Float.floatToIntBits(expectedLoc.getYaw()) != Float.floatToIntBits(actualLoc.getYaw()))
                return false;
            if (Float.floatToIntBits(expectedLoc.getPitch()) != Float.floatToIntBits(actualLoc.getPitch()))
                return false;

            if (expectedLoc.getWorld() != null && !expectedLoc.getWorld().equals(actualLoc.getWorld()))
                return false;
        }

        if (!(this.biome == null || this.biome == block.getBiome()))
            return false;

        if (!this.metadata.isEmpty())
        {
            for (Map.Entry<String, Object> entry : this.metadata.entrySet())
            {
                List<MetadataValue> actual = block.getMetadata(entry.getKey());
                if (actual.isEmpty()
                        || actual.stream().noneMatch(v -> v.asString().equals(entry.getValue().toString())))
                    return false;
            }
        }

        if (!this.blockData.isEmpty())
        {
            Map<String, Object> actual = BlockDataParser.toMap(block.getBlockData());
            if (!MapUtils.isAdequate(this.blockData, actual))
                return false;
        }

        if (this.blockState != null)
        {
            byte expected = this.blockState;
            // noinspection deprecation
            byte actual = block.getState().getData().getData();

            return expected == actual;
        }

        return true;
    }

    @Override
    public Block apply(@NotNull Location location)
    {
        Block block = location.getBlock();
        this.applyTo(block);

        return block;
    }

    @Override
    public Block apply(@NotNull ScenarioEngine engine, @Nullable Location location)
    {
        Location targetLoc;
        if (location != null)
            targetLoc = Utils.assignWorldToLocation(location, engine);
        else if (this.location != null)
            targetLoc = Utils.assignWorldToLocation(this.location, engine);
        else
            return null;

        return this.apply(targetLoc);
    }
}
