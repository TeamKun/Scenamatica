package org.kunlab.scenamatica.scenariofile.structures.context;

import lombok.Value;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.HumanEntityStructureImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
public class PlayerStructureImpl extends HumanEntityStructureImpl implements PlayerStructure
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

    public PlayerStructureImpl(@NotNull HumanEntityStructure human, @Nullable String name, Boolean online, String displayName,
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

    public PlayerStructureImpl(String name, Boolean online, String displayName, String playerListName,
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
    public static Map<String, Object> serialize(@NotNull PlayerStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = serializeHuman(structure, serializer);
        MapUtils.putIfNotNull(map, KEY_NAME, structure.getName());
        MapUtils.putIfNotNull(map, KEY_ONLINE, structure.getOnline());

        MapUtils.putIfNotNull(map, KEY_DISPLAY_NAME, structure.getDisplayName());
        MapUtils.putLocationIfNotNull(map, KEY_COMPASS_TARGET, structure.getCompassTarget());
        MapUtils.putLocationIfNotNull(map, KEY_BED_SPAWN_LOCATION, structure.getBedSpawnLocation());
        MapUtils.putIfNotNull(map, KEY_EXP, structure.getExp());
        MapUtils.putIfNotNull(map, KEY_LEVEL, structure.getLevel());
        MapUtils.putIfNotNull(map, KEY_TOTAL_EXPERIENCE, structure.getTotalExperience());
        MapUtils.putIfNotNull(map, KEY_OP_LEVEL, structure.getOpLevel());
        MapUtils.putIfNotNull(map, KEY_FLYING, structure.getFlying());
        MapUtils.putIfNotNull(map, KEY_ALLOW_FLIGHT, structure.getAllowFlight());


        if (structure.getFlySpeed() != null && structure.getFlySpeed() != SPEED_DEFAULT)
            MapUtils.putIfNotNull(map, KEY_WALK_SPEED, structure.getWalkSpeed());
        if (structure.getWalkSpeed() != null && structure.getWalkSpeed() != SPEED_DEFAULT)
            MapUtils.putIfNotNull(map, KEY_FLY_SPEED, structure.getFlySpeed());

        if (!(structure.getPlayerListName() == null && structure.getPlayerListHeader() == null && structure.getPlayerListFooter() == null))
        {
            Map<String, Object> playerList = new HashMap<>();
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_NAME, structure.getPlayerListName());
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_HEADER, structure.getPlayerListHeader());
            MapUtils.putIfNotNull(playerList, KEY_PLAYER_LIST_FOOTER, structure.getPlayerListFooter());
            map.put(KEY_PLAYER_LIST, playerList);
        }

        if (!structure.getActivePermissions().isEmpty())
            map.put(KEY_ACTIVE_PERMISSIONS, structure.getActivePermissions());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validateHuman(map, serializer);
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
    public static PlayerStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map);

        HumanEntityStructure human = deserializeHuman(map, serializer);

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
            Map<String, Object> playerList =
                    MapUtils.checkAndCastMap(map.get(KEY_PLAYER_LIST));
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

        return new PlayerStructureImpl(
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
        if (!(o instanceof PlayerStructureImpl)) return false;
        if (!super.equals(o)) return false;
        PlayerStructureImpl that = (PlayerStructureImpl) o;
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
