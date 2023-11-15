package org.kunlab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityBean;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.HumanEntityBeanImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
public class PlayerBeanImpl extends HumanEntityBeanImpl implements PlayerBean
{
    private static final float SPEED_DEFAULT = 0.2f;

    String name;
    Boolean online;
    String displayName;
    String playerListName;
    String playerListHeader;
    String playerListFooter;
    Location compassTarget;
    Location bedSpawnLocation;
    Integer exp;
    Integer level;
    Integer totalExperience;
    Boolean allowFlight;
    Boolean flying;
    Float walkSpeed;
    Float flySpeed;

    Integer opLevel;
    List<String> activePermissions;

    public PlayerBeanImpl(@NotNull HumanEntityBean human, @Nullable String name, Boolean online, String displayName,
                          String playerListName, String playerListHeader,
                          String playerListFooter, Location compassTarget,
                          Location bedSpawnLocation, Integer exp,
                          Integer level, Integer totalExperience,
                          Boolean allowFlight, Boolean flying,
                          Float walkSpeed, Float flySpeed, Integer opLevel, List<String> activePermissions)
    {
        super(human, human.getInventory(), human.getEnderChest(),
                human.getMainHand(), human.getGamemode(), human.getFoodLevel()
        );
        this.name = name;
        this.online = online;
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
        this.opLevel = opLevel;
        this.activePermissions = activePermissions;
    }

    public PlayerBeanImpl(String name, Boolean online, String displayName, String playerListName,
                          String playerListHeader, String playerListFooter, Location compassTarget,
                          Location bedSpawnLocation, Integer exp, Integer level, Integer totalExperience,
                          Boolean allowFlight, Boolean flying, Float walkSpeed, Float flySpeed, Integer opLevel,
                          List<String> activePermissions)
    {
        this.name = name;
        this.online = online;
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
        this.opLevel = opLevel;
        this.activePermissions = activePermissions;
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull PlayerBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = serializer.serialize(bean, HumanEntityBean.class);
        MapUtils.putIfNotNull(map, KEY_NAME, bean.getName());
        MapUtils.putIfNotNull(map, KEY_ONLINE, bean.getOnline());

        MapUtils.putIfNotNull(map, KEY_DISPLAY_NAME, bean.getDisplayName());
        MapUtils.putLocationIfNotNull(map, KEY_COMPASS_TARGET, bean.getCompassTarget());
        MapUtils.putLocationIfNotNull(map, KEY_BED_SPAWN_LOCATION, bean.getBedSpawnLocation());
        MapUtils.putIfNotNull(map, KEY_EXP, bean.getExp());
        MapUtils.putIfNotNull(map, KEY_LEVEL, bean.getLevel());
        MapUtils.putIfNotNull(map, KEY_TOTAL_EXPERIENCE, bean.getTotalExperience());
        MapUtils.putIfNotNull(map, KEY_OP_LEVEL, bean.getOpLevel());
        MapUtils.putIfNotNull(map, KEY_FLYING, bean.getFlying());
        MapUtils.putIfNotNull(map, KEY_ALLOW_FLIGHT, bean.getAllowFlight());


        if (bean.getFlySpeed() != null && bean.getFlySpeed() != SPEED_DEFAULT)
            MapUtils.putIfNotNull(map, KEY_WALK_SPEED, bean.getWalkSpeed());
        if (bean.getWalkSpeed() != null && bean.getWalkSpeed() != SPEED_DEFAULT)
            MapUtils.putIfNotNull(map, KEY_FLY_SPEED, bean.getFlySpeed());

        if (!(bean.getPlayerListName() == null && bean.getPlayerListHeader() == null && bean.getPlayerListFooter() == null))
        {
            Map<String, Object> playerList = new HashMap<>();
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_NAME, bean.getPlayerListName());
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_HEADER, bean.getPlayerListHeader());
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_FOOTER, bean.getPlayerListFooter());
            map.put(KEY_PLAYER_LIST, playerList);
        }

        if (!bean.getActivePermissions().isEmpty())
            map.put(KEY_ACTIVE_PERMISSIONS, bean.getActivePermissions());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        serializer.validate(map, HumanEntityBean.class);
        MapUtils.checkTypeIfContains(map, KEY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_ONLINE, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_DISPLAY_NAME, String.class);
        MapUtils.checkLocationIfContains(map, KEY_COMPASS_TARGET);
        MapUtils.checkLocationIfContains(map, KEY_BED_SPAWN_LOCATION);
        MapUtils.checkTypeIfContains(map, KEY_EXP, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_LEVEL, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_TOTAL_EXPERIENCE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_ALLOW_FLIGHT, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_FLYING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_WALK_SPEED, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_FLY_SPEED, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_ACTIVE_PERMISSIONS, List.class);

        Object opLevel = map.get(KEY_OP_LEVEL);
        if (opLevel != null)
            if (!(opLevel instanceof Integer || opLevel instanceof Boolean))
                throw new IllegalArgumentException("opLevel must be an Integer or a Boolean");
            else if (opLevel instanceof Integer && ((Integer) opLevel) > 4)
                throw new IllegalArgumentException("opLevel must be between 0 and 4");
    }

    @NotNull
    public static PlayerBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map);

        HumanEntityBean human = serializer.deserialize(map, HumanEntityBean.class);

        String name = (String) map.get(KEY_NAME);

        Boolean online = MapUtils.getOrNull(map, KEY_ONLINE);
        String displayName = MapUtils.getOrNull(map, KEY_DISPLAY_NAME);
        Location compassTarget = MapUtils.getAsLocationOrNull(map, KEY_COMPASS_TARGET);
        Location bedSpawnLocation = MapUtils.getAsLocationOrNull(map, KEY_BED_SPAWN_LOCATION);
        Integer exp = MapUtils.getOrNull(map, KEY_EXP);
        Integer level = MapUtils.getOrNull(map, KEY_LEVEL);
        Integer totalExperience = MapUtils.getOrNull(map, KEY_TOTAL_EXPERIENCE);
        Boolean allowFlight = MapUtils.getOrNull(map, KEY_ALLOW_FLIGHT);
        Boolean flying = MapUtils.getOrNull(map, KEY_FLYING);
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

        Integer opLevel = null;
        if (map.containsKey(KEY_OP_LEVEL))
        {
            Object opLevelObj = map.get(KEY_OP_LEVEL);
            if (opLevelObj instanceof Boolean)
                opLevel = (Boolean) opLevelObj ? 4: 0;
            else if (opLevelObj instanceof Integer)
                opLevel = (Integer) opLevelObj;
        }

        List<String> activePermissions = MapUtils.getAsListOrEmpty(map, KEY_ACTIVE_PERMISSIONS);

        return new PlayerBeanImpl(
                human,
                name,
                online,
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
                flySpeed,
                opLevel,
                activePermissions
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PlayerBeanImpl)) return false;
        if (!super.equals(o)) return false;
        PlayerBeanImpl that = (PlayerBeanImpl) o;
        return Objects.equals(this.opLevel, that.opLevel)
                && Objects.equals(this.allowFlight, that.allowFlight)
                && Objects.equals(this.flying, that.flying)
                && Objects.equals(this.name, that.name)
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
