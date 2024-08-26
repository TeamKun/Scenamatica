package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.LivingEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;
import org.kunlab.scenamatica.structures.minecraft.entity.LivingEntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.InventoryStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.PlayerInventoryStructureImpl;

import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
public class HumanEntityStructureImpl extends LivingEntityStructureImpl implements HumanEntityStructure
{
    protected final PlayerInventoryStructure inventory;
    protected final InventoryStructure enderChest;
    protected final MainHand mainHand;
    protected final GameMode gamemode;
    protected final Integer foodLevel;

    public HumanEntityStructureImpl(HumanEntityStructure original)
    {
        this(
                original,
                original.getInventory(),
                original.getEnderChest(),
                original.getMainHand(),
                original.getGamemode(),
                original.getFoodLevel()
        );
    }

    public HumanEntityStructureImpl(
            LivingEntityStructure original,
            PlayerInventoryStructure inventory,
            InventoryStructure enderChest,
            MainHand mainHand,
            GameMode gamemode,
            Integer foodLevel)
    {
        super(EntityType.PLAYER, original);
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.mainHand = mainHand;
        this.gamemode = gamemode;
        this.foodLevel = foodLevel;
    }

    public HumanEntityStructureImpl()
    {
        super();
        this.inventory = null;
        this.enderChest = null;
        this.mainHand = null;
        this.gamemode = null;
        this.foodLevel = null;
    }

    /**
     * 人形エンティティの情報をMapにシリアライズします。
     *
     * @return シリアライズされたMap
     */
    @NotNull
    public static Map<String, Object> serializeHuman(@NotNull HumanEntityStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = LivingEntityStructureImpl.serializeLivingEntity(structure, serializer);
        map.remove(KEY_TYPE);

        if (structure.getInventory() != null)
            map.put(KEY_INVENTORY, serializer.serialize(structure.getInventory(), PlayerInventoryStructure.class));
        if (structure.getEnderChest() != null)
            map.put(KEY_ENDER_CHEST, serializer.serialize(structure.getEnderChest(), InventoryStructure.class));

        MapUtils.putAsStrIfNotNull(map, KEY_MAIN_HAND, structure.getMainHand());
        MapUtils.putAsStrIfNotNull(map, KEY_GAMEMODE, structure.getGamemode());
        MapUtils.putIfNotNull(map, KEY_FOOD_LEVEL, structure.getFoodLevel());

        return map;
    }

    /**
     * Mapがシリアライズされた人形エンティティの情報かどうかを判定します。
     *
     * @param map        判定するMap
     * @param serializer シリアライザ
     * @throws IllegalArgumentException Mapがシリアライズされた人形エンティティの情報でない場合
     */
    public static void validateHuman(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validateLivingEntity(map);

        if (map.containsKey(KEY_INVENTORY))
            serializer.validate(
                    MapUtils.checkAndCastMap(map.get(KEY_INVENTORY)), PlayerInventoryStructure.class);
        if (map.containsKey(KEY_ENDER_CHEST))
            serializer.validate(
                    MapUtils.checkAndCastMap(map.get(KEY_ENDER_CHEST)), InventoryStructure.class);
    }

    /**
     * Mapから人形エンティティの情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズされた人形エンティティの情報
     */
    @NotNull
    public static HumanEntityStructure deserializeHuman(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validateLivingEntity(map);

        LivingEntityStructure livingEntityStructure = LivingEntityStructureImpl.deserializeLivingEntity(map, serializer);

        PlayerInventoryStructure inventory = null;
        if (map.containsKey(KEY_INVENTORY))
            inventory = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_INVENTORY)), PlayerInventoryStructure.class);

        InventoryStructure enderChest = null;
        if (map.containsKey(KEY_ENDER_CHEST))
            enderChest = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_ENDER_CHEST)), InventoryStructure.class);

        MainHand mainHand = MapUtils.getAsEnumOrNull(
                map,
                KEY_MAIN_HAND,
                MainHand.class
        );
        GameMode gamemode = MapUtils.getAsEnumOrNull(
                map,
                KEY_GAMEMODE,
                GameMode.class
        );

        Integer foodLevel = MapUtils.getOrNull(map, KEY_FOOD_LEVEL);

        return new HumanEntityStructureImpl(
                livingEntityStructure,
                inventory,
                enderChest,
                mainHand,
                gamemode,
                foodLevel
        );
    }

    @NotNull
    public static HumanEntityStructure ofHuman(@NotNull HumanEntity entity)
    {
        NMSEntityHuman nmsHuman = NMSProvider.getProvider().wrap(entity);

        return new HumanEntityStructureImpl(
                LivingEntityStructureImpl.ofLivingEntity(entity),
                PlayerInventoryStructureImpl.ofPlayerInventory(entity.getInventory()),
                InventoryStructureImpl.of(entity.getEnderChest()),
                entity.getMainHand(),
                entity.getGameMode(),
                nmsHuman.getFoodLevel()
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof HumanEntityStructureImpl)) return false;
        if (!super.equals(o)) return false;
        HumanEntityStructureImpl that = (HumanEntityStructureImpl) o;
        return Objects.equals(this.inventory, that.inventory)
                && Objects.equals(this.enderChest, that.enderChest)
                && this.mainHand == that.mainHand
                && this.gamemode == that.gamemode
                && Objects.equals(this.foodLevel, that.foodLevel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), this.inventory, this.enderChest,
                this.mainHand, this.gamemode, this.foodLevel
        );
    }

    @Override
    public void applyTo(@NotNull Entity entity, boolean applyLocation)
    {
        if (!(entity instanceof HumanEntity))
            return;
        HumanEntity humanEntity = (HumanEntity) entity;

        super.applyTo(humanEntity, applyLocation);
        NMSEntityHuman nmsHuman = NMSProvider.getProvider().wrap(humanEntity);

        if (this.inventory != null)
            this.inventory.applyTo(humanEntity.getInventory());
        if (this.enderChest != null)
            this.enderChest.applyTo(humanEntity.getEnderChest());

        if (this.gamemode != null)
            humanEntity.setGameMode(this.gamemode);
        if (this.foodLevel != null)
            nmsHuman.setFoodLevel(this.foodLevel);
    }

    @Override
    public boolean isAdequate(@Nullable Entity entity, boolean strict)
    {
        if (!(super.isAdequate(entity, strict) && entity instanceof HumanEntity))
            return false;
        HumanEntity humanEntity = (HumanEntity) entity;
        NMSEntityHuman nmsHuman = NMSProvider.getProvider().wrap(humanEntity);

        return (this.inventory == null || this.inventory.isAdequate(humanEntity.getInventory(), strict))
                && (this.enderChest == null || this.enderChest.isAdequate(humanEntity.getEnderChest(), strict))
                && (this.gamemode == null || this.gamemode == humanEntity.getGameMode())
                && (this.foodLevel == null || this.foodLevel == nmsHuman.getFoodLevel());
    }
}
