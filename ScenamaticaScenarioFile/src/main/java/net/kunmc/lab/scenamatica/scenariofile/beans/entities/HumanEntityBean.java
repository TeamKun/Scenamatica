package net.kunmc.lab.scenamatica.scenariofile.beans.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.InventoryBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.PlayerInventoryBean;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 人形エンティティを表すクラスです。
 */
@Data
@AllArgsConstructor
public class HumanEntityBean extends EntityBean
{
    public static final String KEY_INVENTORY = "inventory";
    public static final String KEY_ENDER_CHEST = "enderChest";
    public static final String KEY_MAIN_HAND = "mainHand";
    public static final String KEY_GAMEMODE = "gamemode";
    public static final String KEY_FOOD_LEVEL = "food";

    /**
     * プレイヤーのインベントリです。
     */
    @Nullable
    private final PlayerInventoryBean inventory;

    /**
     * エンダーチェストの定義を表すクラスです。
     */
    @Nullable
    private final InventoryBean enderChest;
    /**
     * プレイヤーの利き手です。
     */
    @NotNull
    private final MainHand mainHand;
    /**
     * プレイヤーのゲームモードです。
     */
    @NotNull
    private final GameMode gamemode;
    /**
     * プレイヤーの食料レベルです。
     */
    @Nullable
    private final Integer foodLevel;

    public HumanEntityBean(
            @Nullable Location location,
            @Nullable String customName,
            @Nullable UUID uuid,
            boolean glowing,
            boolean gravity,
            @Nullable List<String> tags,
            @Nullable DamageBean lastDamage,
            @Nullable Integer maxHealth,
            @Nullable Integer health,
            @Nullable PlayerInventoryBean inventory,
            @Nullable InventoryBean enderChest,
            @NotNull MainHand mainHand,
            @NotNull GameMode gamemode,
            @Nullable Integer foodLevel)
    {
        super(location, customName, uuid, glowing, gravity, tags, maxHealth, health, lastDamage);
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.mainHand = mainHand;
        this.gamemode = gamemode;
        this.foodLevel = foodLevel;
    }

    public HumanEntityBean(
            @NotNull EntityBean entityBean,
            @Nullable PlayerInventoryBean inventory,
            @Nullable InventoryBean enderChest,
            @NotNull MainHand mainHand,
            @NotNull GameMode gamemode,
            @Nullable Integer foodLevel)
    {
        super(entityBean.getLocation(), entityBean.getCustomName(), entityBean.getUuid(), entityBean.isGlowing(),
                entityBean.isGravity(), entityBean.getTags(), entityBean.getMaxHealth(), entityBean.getHealth(), entityBean.getLastDamageCause()
        );
        this.inventory = inventory;
        this.enderChest = enderChest;
        this.mainHand = mainHand;
        this.gamemode = gamemode;
        this.foodLevel = foodLevel;
    }

    public HumanEntityBean()
    {
        this(
                null,
                null,
                null,
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                MainHand.RIGHT,
                GameMode.SURVIVAL,
                null
        );
    }

    /**
     * 人形エンティティの情報をMapにシリアライズします。
     *
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(HumanEntityBean bean)
    {
        Map<String, Object> map = EntityBean.serialize(bean);

        if (bean.getInventory() != null)
            map.put(KEY_INVENTORY, PlayerInventoryBean.serialize(bean.getInventory()));
        if (bean.getEnderChest() != null)
            map.put(KEY_ENDER_CHEST, InventoryBean.serialize(bean.getEnderChest()));

        if (bean.getMainHand() != MainHand.RIGHT)
            map.put(KEY_MAIN_HAND, bean.getMainHand().name());
        if (bean.getGamemode() != GameMode.SURVIVAL)
            map.put(KEY_GAMEMODE, bean.getGamemode().name());

        MapUtils.putIfNotNull(map, KEY_FOOD_LEVEL, bean.getFoodLevel());

        return map;
    }

    /**
     * Mapがシリアライズされた人形エンティティの情報かどうかを判定します。
     *
     * @param map 判定するMap
     * @throws IllegalArgumentException Mapがシリアライズされた人形エンティティの情報でない場合
     */
    public static void validateMap(@NotNull Map<String, Object> map)
    {
        EntityBean.validateMap(map);

        if (map.containsKey(KEY_INVENTORY))
            PlayerInventoryBean.validateMap(MapUtils.checkAndCastMap(
                    map.get(KEY_INVENTORY),
                    String.class,
                    Object.class
            ));
        if (map.containsKey(KEY_ENDER_CHEST))
            InventoryBean.validateMap(MapUtils.checkAndCastMap(
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
    public static HumanEntityBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        EntityBean entityBean = EntityBean.deserialize(map);

        PlayerInventoryBean inventory = null;
        if (map.containsKey(KEY_INVENTORY))
            inventory = PlayerInventoryBean.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_INVENTORY),
                    String.class,
                    Object.class
            ));

        InventoryBean enderChest = null;
        if (map.containsKey(KEY_ENDER_CHEST))
            enderChest = InventoryBean.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_ENDER_CHEST),
                    String.class,
                    Object.class
            ));

        MainHand mainHand = MapUtils.getAsEnumOrDefault(
                map,
                KEY_MAIN_HAND,
                MainHand.class,
                MainHand.RIGHT
        );
        GameMode gamemode = MapUtils.getAsEnumOrDefault(
                map,
                KEY_GAMEMODE,
                GameMode.class,
                GameMode.SURVIVAL
        );

        Integer foodLevel = MapUtils.getOrNull(map, KEY_FOOD_LEVEL);

        return new HumanEntityBean(
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
        if (!(o instanceof HumanEntityBean)) return false;
        if (!super.equals(o)) return false;
        HumanEntityBean that = (HumanEntityBean) o;
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
