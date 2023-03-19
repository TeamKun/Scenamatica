package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.beans.entities.HumanEntityBean;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 疑似プレイヤーを表すクラスです。
 */
@Value
@AllArgsConstructor
public class PlayerBean extends HumanEntityBean implements Serializable
{
    public static final String KEY_NAME = "name";
    public static final String KEY_DISPLAY_NAME = "display";
    public static final String KEY_PLAYER_LIST = "playerList";
    public static final String KEY_PLAYER_LIST_NAME = "name";  // Inner of playerList
    public static final String KEY_PLAYER_LIST_HEADER = "header"; // Inner of playerList
    public static final String KEY_PLAYER_LIST_FOOTER = "footer"; // Inner of playerList
    public static final String KEY_COMPASS_TARGET = "compass";
    public static final String KEY_BED_SPAWN_LOCATION = "bedLocation";
    public static final String KEY_EXP = "exp";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_TOTAL_EXPERIENCE = "totalExp";
    public static final String KEY_ALLOW_FLIGHT = "flyable";
    public static final String KEY_FLYING = "flying";
    public static final String KEY_FLY_SPEED = "flySpeed";
    public static final String KEY_WALK_SPEED = "walkSpeed";

    /**
     * プレイヤーの名前です。
     */
    @NotNull
    String name;
    /**
     * プレイヤーの表示名前です。
     */
    @Nullable
    String displayName;
    /**
     * プレイヤーリストに表示される名前です。
     */
    @Nullable
    String playerListName;
    /**
     * プレイヤーリストのヘッダーです。
     */
    @Nullable
    String playerListHeader;
    /**
     * プレイヤーリストのフッターです。
     */
    @Nullable
    String playerListFooter;
    /**
     * コンパスのターゲットを設定します。
     */
    @Nullable
    Location compassTarget;
    /**
     * プレイヤーのベッドのスポーン位置です。
     */
    @Nullable
    Location bedSpawnLocation;
    /**
     * プレイヤーの経験値です。
     */
    @Nullable
    Integer exp;
    /**
     * プレイヤーのレベルです。
     */
    @Nullable
    Integer level;
    /**
     * プレイヤーの経験値の総量です。
     */
    @Nullable
    Integer totalExperience;
    /**
     * プレイヤーが飛べるかどうかです。
     */
    boolean allowFlight;
    /**
     * プレイヤーが飛んでいるかどうかです。
     */
    boolean flying;
    /**
     * プレイヤーの歩く速度です。
     */
    @Nullable
    Float walkSpeed;
    /**
     * プレイヤーの走る速度です。
     */
    @Nullable
    Float flySpeed;

    public PlayerBean(@NotNull HumanEntityBean human, @NotNull String name, @Nullable String displayName,
                      @Nullable String playerListName, @Nullable String playerListHeader,
                      @Nullable String playerListFooter, @Nullable Location compassTarget,
                      @Nullable Location bedSpawnLocation, @Nullable Integer exp,
                      @Nullable Integer level, @Nullable Integer totalExperience,
                      boolean allowFlight, boolean flying,
                      @Nullable Float walkSpeed, @Nullable Float flySpeed)
    {
        super(human, human.getInventory(), human.getEnderChest(),
                human.getMainHand(), human.getGamemode(), human.getFoodLevel()
        );
        this.name = name;
        this.displayName = displayName;
        this.playerListName = playerListName;
        this.playerListHeader = playerListHeader;
        this.playerListFooter = playerListFooter;
        this.compassTarget = compassTarget;
        this.bedSpawnLocation = bedSpawnLocation;
        this.exp = exp;
        this.level = level;
        this.totalExperience = totalExperience;
        this.allowFlight = allowFlight;
        this.flying = flying;
        this.walkSpeed = walkSpeed;
        this.flySpeed = flySpeed;
    }

    /**
     * プレイヤーの情報をMapにシリアライズします。
     *
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(PlayerBean bean)
    {
        Map<String, Object> map = HumanEntityBean.serialize(bean);
        map.put(KEY_NAME, bean.name);

        MapUtils.putIfNotNull(map, KEY_DISPLAY_NAME, bean.displayName);
        MapUtils.putLocationIfNotNull(map, KEY_COMPASS_TARGET, bean.compassTarget);
        MapUtils.putLocationIfNotNull(map, KEY_BED_SPAWN_LOCATION, bean.bedSpawnLocation);
        MapUtils.putIfNotNull(map, KEY_EXP, bean.exp);
        MapUtils.putIfNotNull(map, KEY_LEVEL, bean.level);
        MapUtils.putIfNotNull(map, KEY_TOTAL_EXPERIENCE, bean.totalExperience);

        boolean isFlyableGamemode = bean.getGamemode() == GameMode.CREATIVE || bean.getGamemode() == GameMode.SPECTATOR;
        if (bean.allowFlight)
        {
            if (!isFlyableGamemode)
                map.put(KEY_ALLOW_FLIGHT, true);
        }
        else if (isFlyableGamemode)
            map.put(KEY_ALLOW_FLIGHT, false);

        if (bean.flying)
        {
            if (!isFlyableGamemode)
                map.put(KEY_FLYING, true);
        }
        else if (isFlyableGamemode)
            map.put(KEY_FLYING, false);

        MapUtils.putIfNotNull(map, KEY_FLY_SPEED, bean.flySpeed);
        MapUtils.putIfNotNull(map, KEY_WALK_SPEED, bean.walkSpeed);

        if (!(bean.playerListName == null && bean.playerListHeader == null && bean.playerListFooter == null))
        {
            Map<String, Object> playerList = new HashMap<>();
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_NAME, bean.playerListName);
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_HEADER, bean.playerListHeader);
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_FOOTER, bean.playerListFooter);
            map.put(KEY_PLAYER_LIST, playerList);
        }

        return map;
    }

    /**
     * Mapがシリアライズされたプレイヤーの情報かどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException Mapがシリアライズされたプレイヤーの情報でない場合
     */
    public static void validateMap(@NotNull Map<String, Object> map)
    {
        HumanEntityBean.validateMap(map);
        MapUtils.checkType(map, KEY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_DISPLAY_NAME, String.class);
        MapUtils.checkLocationIfContains(map, KEY_COMPASS_TARGET);
        MapUtils.checkLocationIfContains(map, KEY_BED_SPAWN_LOCATION);
        MapUtils.checkTypeIfContains(map, KEY_EXP, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_LEVEL, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_TOTAL_EXPERIENCE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_ALLOW_FLIGHT, Boolean.class);
    }

    /**
     * Mapからプレイヤーのデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズされたプレイヤーの情報
     * @throws IllegalArgumentException Mapがシリアライズされたプレイヤーの情報でない場合
     */
    public static PlayerBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        HumanEntityBean human = HumanEntityBean.deserialize(map);

        String name = (String) map.get(KEY_NAME);

        String displayName = MapUtils.getOrNull(map, KEY_DISPLAY_NAME);
        Location compassTarget = MapUtils.getAsLocationOrNull(map, KEY_COMPASS_TARGET);
        Location bedSpawnLocation = MapUtils.getAsLocationOrNull(map, KEY_BED_SPAWN_LOCATION);
        Integer exp = MapUtils.getOrNull(map, KEY_EXP);
        Integer level = MapUtils.getOrNull(map, KEY_LEVEL);
        Integer totalExperience = MapUtils.getOrNull(map, KEY_TOTAL_EXPERIENCE);
        boolean allowFlight = MapUtils.getOrDefault(map, KEY_ALLOW_FLIGHT, false);
        boolean flying = MapUtils.getOrDefault(map, KEY_FLYING, false);
        Float walkSpeed = MapUtils.getOrNull(map, KEY_WALK_SPEED);
        Float flySpeed = MapUtils.getOrNull(map, KEY_FLY_SPEED);

        String playerListName = null;
        String playerListHeader = null;
        String playerListFooter = null;
        if (map.containsKey(KEY_PLAYER_LIST))
        {
            Map<String, Object> playerList = MapUtils.checkAndCastMap(
                    map.get(KEY_PLAYER_LIST),
                    String.class,
                    Object.class
            );
            playerListName = MapUtils.getOrNull(playerList, KEY_PLAYER_LIST_NAME);
            playerListHeader = MapUtils.getOrNull(playerList, KEY_PLAYER_LIST_HEADER);
            playerListFooter = MapUtils.getOrNull(playerList, KEY_PLAYER_LIST_FOOTER);
        }

        return new PlayerBean(
                human,
                name,
                displayName,
                playerListName,
                playerListHeader,
                playerListFooter,
                compassTarget,
                bedSpawnLocation,
                exp,
                level,
                totalExperience,
                allowFlight,
                flying,
                walkSpeed,
                flySpeed
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PlayerBean)) return false;
        if (!super.equals(o)) return false;
        PlayerBean that = (PlayerBean) o;
        return this.allowFlight == that.allowFlight
                && this.flying == that.flying
                && this.name.equals(that.name)
                && Objects.equals(this.displayName, that.displayName)
                && Objects.equals(this.playerListName, that.playerListName)
                && Objects.equals(this.playerListHeader, that.playerListHeader)
                && Objects.equals(this.playerListFooter, that.playerListFooter)
                && Objects.equals(this.compassTarget, that.compassTarget)
                && Objects.equals(this.bedSpawnLocation, that.bedSpawnLocation)
                && Objects.equals(this.exp, that.exp)
                && Objects.equals(this.level, that.level)
                && Objects.equals(this.totalExperience, that.totalExperience)
                && Objects.equals(this.walkSpeed, that.walkSpeed)
                && Objects.equals(this.flySpeed, that.flySpeed);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), this.name, this.displayName, this.playerListName,
                this.playerListHeader, this.playerListFooter, this.compassTarget, this.bedSpawnLocation,
                this.exp, this.level, this.totalExperience, this.allowFlight, this.flying, this.walkSpeed,
                this.flySpeed
        );
    }
}
