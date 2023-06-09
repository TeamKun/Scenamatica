package org.kunlab.scenamatica.scenariofile.beans.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.HumanEntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HumanEntityBeanImpl extends EntityBeanImpl implements HumanEntityBean
{
    private final PlayerInventoryBean inventory;
    private final InventoryBean enderChest;
    private final MainHand mainHand;
    private final GameMode gamemode;
    private final Integer foodLevel;

    public HumanEntityBeanImpl(
            Location location,
            String customName,
            UUID uuid,
            Boolean glowing,
            Boolean gravity,
            @NotNull List<String> tags,
            DamageBeanImpl lastDamage,
            Integer maxHealth,
            Integer health,
            @NotNull List<PotionEffect> potionEffects,
            PlayerInventoryBean inventory,
            InventoryBean enderChest,
            MainHand mainHand,
            GameMode gamemode,
            Integer foodLevel)
    {
        super(location, customName, uuid, glowing, gravity, tags, maxHealth, health, lastDamage, potionEffects);
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.mainHand = mainHand;
        this.gamemode = gamemode;
        this.foodLevel = foodLevel;
    }

    public HumanEntityBeanImpl(
            EntityBean entityBean,
            PlayerInventoryBean inventory,
            InventoryBean enderChest,
            MainHand mainHand,
            GameMode gamemode,
            Integer foodLevel)
    {
        super(entityBean.getLocation(), entityBean.getCustomName(), entityBean.getUuid(), entityBean.getGlowing(),
                entityBean.getGravity(), entityBean.getTags(), entityBean.getMaxHealth(), entityBean.getHealth(), entityBean.getLastDamageCause(),
                entityBean.getPotionEffects()
        );
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.mainHand = mainHand;
        this.gamemode = gamemode;
        this.foodLevel = foodLevel;
    }

    public HumanEntityBeanImpl()
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
    public static Map<String, Object> serialize(@NotNull HumanEntityBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = serializer.serializeEntity(bean);

        if (bean.getInventory() != null)
            map.put(KEY_INVENTORY, serializer.serializePlayerInventory(bean.getInventory()));
        if (bean.getEnderChest() != null)
            map.put(KEY_ENDER_CHEST, serializer.serializeInventory(bean.getEnderChest()));

        MapUtils.putIfNotNull(map, KEY_MAIN_HAND, bean.getMainHand());
        MapUtils.putIfNotNull(map, KEY_GAMEMODE, bean.getGamemode());
        MapUtils.putIfNotNull(map, KEY_FOOD_LEVEL, bean.getFoodLevel());

        return map;
    }

    /**
     * Mapがシリアライズされた人形エンティティの情報かどうかを判定します。
     *
     * @param map        判定するMap
     * @param serializer シリアライザ
     * @throws IllegalArgumentException Mapがシリアライズされた人形エンティティの情報でない場合
     */
    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map);

        if (map.containsKey(KEY_INVENTORY))
            serializer.validatePlayerInventory(MapUtils.checkAndCastMap(
                    map.get(KEY_INVENTORY),
                    String.class,
                    Object.class
            ));
        if (map.containsKey(KEY_ENDER_CHEST))
            serializer.validateInventory(MapUtils.checkAndCastMap(
                    map.get(KEY_ENDER_CHEST),
                    String.class,
                    Object.class
            ));
    }

    /**
     * Mapから人形エンティティの情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズされた人形エンティティの情報
     */
    @NotNull
    public static HumanEntityBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map);

        EntityBean entityBean = serializer.deserializeEntity(map);

        PlayerInventoryBean inventory = null;
        if (map.containsKey(KEY_INVENTORY))
            inventory = serializer.deserializePlayerInventory(MapUtils.checkAndCastMap(
                    map.get(KEY_INVENTORY),
                    String.class,
                    Object.class
            ));

        InventoryBean enderChest = null;
        if (map.containsKey(KEY_ENDER_CHEST))
            enderChest = serializer.deserializeInventory(MapUtils.checkAndCastMap(
                    map.get(KEY_ENDER_CHEST),
                    String.class,
                    Object.class
            ));

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

        return new HumanEntityBeanImpl(
                entityBean,
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
        if (!(o instanceof HumanEntityBeanImpl)) return false;
        if (!super.equals(o)) return false;
        HumanEntityBeanImpl that = (HumanEntityBeanImpl) o;
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
}
