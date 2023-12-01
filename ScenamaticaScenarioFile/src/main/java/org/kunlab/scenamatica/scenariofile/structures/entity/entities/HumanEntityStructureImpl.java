package org.kunlab.scenamatica.scenariofile.structures.entity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.scenariofile.structures.entity.EntityStructureImpl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HumanEntityStructureImpl<T extends HumanEntity> extends EntityStructureImpl<T> implements HumanEntityStructure<T>
{
    private final PlayerInventoryStructure inventory;
    private final InventoryStructure<Inventory> enderChest;
    private final MainHand mainHand;
    private final GameMode gamemode;
    private final Integer foodLevel;

    public HumanEntityStructureImpl(Location location, Vector velocity, String customName, UUID uuid, Boolean glowing,
                                    Boolean gravity, Boolean silent, Boolean customNameVisible, Boolean invulnerable,
                                    @NotNull List<String> tags, Integer maxHealth, Integer health,
                                    DamageStructure lastDamageCause, @NotNull List<PotionEffect> potionEffects, Integer fireTicks,
                                    Integer ticksLived, Integer portalCooldown, Boolean persistent, Float fallDistance,
                                    PlayerInventoryStructure inventory, InventoryStructure<Inventory> enderChest, MainHand mainHand,
                                    GameMode gamemode, Integer foodLevel)
    {
        super(EntityType.PLAYER, location, velocity, customName, uuid, glowing, gravity, silent, customNameVisible, invulnerable, tags, maxHealth, health, lastDamageCause, potionEffects, fireTicks, ticksLived, portalCooldown, persistent, fallDistance);
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.mainHand = mainHand;
        this.gamemode = gamemode;
        this.foodLevel = foodLevel;
    }

    public HumanEntityStructureImpl(
            EntityStructure<?> entityStructure,
            PlayerInventoryStructure inventory,
            InventoryStructure<Inventory> enderChest,
            MainHand mainHand,
            GameMode gamemode,
            Integer foodLevel)
    {
        super(EntityType.PLAYER,
                entityStructure.getLocation(), entityStructure.getVelocity(), entityStructure.getCustomName(), entityStructure.getUuid(),
                entityStructure.getGlowing(), entityStructure.getGravity(), entityStructure.getSilent(), entityStructure.getCustomNameVisible(),
                entityStructure.getInvulnerable(), entityStructure.getTags(), entityStructure.getMaxHealth(), entityStructure.getHealth(),
                entityStructure.getLastDamageCause(), entityStructure.getPotionEffects(), entityStructure.getFireTicks(),
                entityStructure.getTicksLived(), entityStructure.getPortalCooldown(), entityStructure.getPersistent(), entityStructure.getFallDistance()
        );
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
    public static Map<String, Object> serializeHuman(@NotNull HumanEntityStructure<?> structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = serializer.serialize(structure, EntityStructure.class);
        map.remove(EntityStructure.KEY_TYPE);

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
        validate(map);

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
    public static HumanEntityStructure<?> deserializeHuman(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map);

        EntityStructure<?> entityStructure = serializer.deserialize(map, EntityStructure.class);

        PlayerInventoryStructure inventory = null;
        if (map.containsKey(KEY_INVENTORY))
            inventory = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_INVENTORY)), PlayerInventoryStructure.class);

        InventoryStructure<Inventory> enderChest = null;
        if (map.containsKey(KEY_ENDER_CHEST))
            // noinspection unchecked
            enderChest = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_ENDER_CHEST)), InventoryStructure.class);

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

        return new HumanEntityStructureImpl<>(
                entityStructure,
                inventory,
                enderChest,
                mainHand,
                gamemode,
                foodLevel
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof HumanEntityStructureImpl)) return false;
        if (!super.equals(o)) return false;
        HumanEntityStructureImpl<?> that = (HumanEntityStructureImpl<?>) o;
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
    public boolean canApplyTo(Object target)
    {
        return super.canApplyTo(target) && target instanceof HumanEntity;
    }
}
