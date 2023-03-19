package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.beans.entities.HumanEntityBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.context.PlayerBean;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.entities.HumanEntityBean;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Value
@AllArgsConstructor
public class PlayerBeanImpl extends HumanEntityBeanImpl implements PlayerBean
{
    private static final float SPEED_DEFAULT = 0.2f;

    @NotNull
    String name;
    @Nullable
    String displayName;
    @Nullable
    String playerListName;
    @Nullable
    String playerListHeader;
    @Nullable
    String playerListFooter;
    @Nullable
    Location compassTarget;
    @Nullable
    Location bedSpawnLocation;
    @Nullable
    Integer exp;
    @Nullable
    Integer level;
    @Nullable
    Integer totalExperience;
    boolean allowFlight;
    boolean flying;
    @Nullable
    Float walkSpeed;
    @Nullable
    Float flySpeed;

    public PlayerBeanImpl(@NotNull HumanEntityBean human, @NotNull String name, @Nullable String displayName,
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

    public static Map<String, Object> serialize(PlayerBean bean)
    {
        Map<String, Object> map = HumanEntityBeanImpl.serialize(bean);
        map.put(KEY_NAME, bean.getName());

        MapUtils.putIfNotNull(map, KEY_DISPLAY_NAME, bean.getDisplayName());
        MapUtils.putLocationIfNotNull(map, KEY_COMPASS_TARGET, bean.getCompassTarget());
        MapUtils.putLocationIfNotNull(map, KEY_BED_SPAWN_LOCATION, bean.getBedSpawnLocation());
        MapUtils.putIfNotNull(map, KEY_EXP, bean.getExp());
        MapUtils.putIfNotNull(map, KEY_LEVEL, bean.getLevel());
        MapUtils.putIfNotNull(map, KEY_TOTAL_EXPERIENCE, bean.getTotalExperience());

        boolean isFlyableGamemode = bean.getGamemode() == GameMode.CREATIVE || bean.getGamemode() == GameMode.SPECTATOR;
        if (bean.isAllowFlight())
        {
            if (!isFlyableGamemode)
                map.put(KEY_ALLOW_FLIGHT, true);
        }
        else if (isFlyableGamemode)
            map.put(KEY_ALLOW_FLIGHT, false);

        if (bean.isFlying())
        {
            if (!isFlyableGamemode)
                map.put(KEY_FLYING, true);
        }
        else if (isFlyableGamemode)
            map.put(KEY_FLYING, false);

        if (bean.getFlySpeed() != null && bean.getFlySpeed() != SPEED_DEFAULT)
            MapUtils.putIfNotNull(map, KEY_WALK_SPEED, bean.getWalkSpeed());
        if (bean.getPlayerListFooter() != null && bean.getWalkSpeed() != SPEED_DEFAULT)
            MapUtils.putIfNotNull(map, KEY_FLY_SPEED, bean.getFlySpeed());

        if (!(bean.getPlayerListName() == null && bean.getPlayerListHeader() == null && bean.getPlayerListFooter() == null))
        {
            Map<String, Object> playerList = new HashMap<>();
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_NAME, bean.getPlayerListName());
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_HEADER, bean.getPlayerListHeader());
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_FOOTER, bean.getPlayerListFooter());
            map.put(KEY_PLAYER_LIST, playerList);
        }

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        HumanEntityBeanImpl.validate(map);
        MapUtils.checkType(map, KEY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_DISPLAY_NAME, String.class);
        MapUtils.checkLocationIfContains(map, KEY_COMPASS_TARGET);
        MapUtils.checkLocationIfContains(map, KEY_BED_SPAWN_LOCATION);
        MapUtils.checkTypeIfContains(map, KEY_EXP, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_LEVEL, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_TOTAL_EXPERIENCE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_ALLOW_FLIGHT, Boolean.class);
    }

    public static PlayerBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        HumanEntityBean human = HumanEntityBeanImpl.deserialize(map);

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

        return new PlayerBeanImpl(
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
        if (!(o instanceof PlayerBeanImpl)) return false;
        if (!super.equals(o)) return false;
        PlayerBeanImpl that = (PlayerBeanImpl) o;
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