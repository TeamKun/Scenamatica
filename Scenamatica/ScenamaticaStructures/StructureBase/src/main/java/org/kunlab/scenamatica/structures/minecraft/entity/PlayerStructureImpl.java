package org.kunlab.scenamatica.structures.minecraft.entity;

import lombok.Value;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.HumanEntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.misc.LocationStructureImpl;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Value
public class PlayerStructureImpl extends HumanEntityStructureImpl implements PlayerStructure
{
    private static final float SPEED_DEFAULT = 0.2f;

    String name;
    Boolean online;
    InetAddress remoteAddress;
    Integer port;
    String hostName;
    String displayName;
    String playerListName;
    String playerListHeader;
    String playerListFooter;
    LocationStructure compassTarget;
    LocationStructure bedSpawnLocation;
    Integer exp;
    Integer level;
    Integer totalExperience;
    Boolean allowFlight;
    Boolean flying;
    Float walkSpeed;
    Float flySpeed;

    Integer opLevel;
    List<String> activePermissions;

    public PlayerStructureImpl(@NotNull HumanEntityStructure human, @Nullable String name, Boolean online,
                               InetAddress remoteAddress, Integer port, String hostName, String displayName,
                               String playerListName, String playerListHeader,
                               String playerListFooter, LocationStructure compassTarget,
                               LocationStructure bedSpawnLocation, Integer exp,
                               Integer level, Integer totalExperience,
                               Boolean allowFlight, Boolean flying,
                               Float walkSpeed, Float flySpeed, Integer opLevel, List<String> activePermissions)
    {
        super(human);
        this.name = name;
        this.online = online;
        this.remoteAddress = remoteAddress;
        this.port = port;
        this.hostName = hostName;
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

    public PlayerStructureImpl(String name, Boolean online, InetAddress remoteAddress, Integer port, String hostName,
                               String displayName, String playerListName, String playerListHeader,
                               String playerListFooter, LocationStructure compassTarget,
                               LocationStructure bedSpawnLocation, Integer exp, Integer level, Integer totalExperience,
                               Boolean allowFlight, Boolean flying, Float walkSpeed, Float flySpeed, Integer opLevel,
                               List<String> activePermissions)
    {
        this.name = name;
        this.online = online;
        this.remoteAddress = remoteAddress;
        this.port = port;
        this.hostName = hostName;
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
        if (structure.getCompassTarget() != null)
            map.put(KEY_COMPASS_TARGET, serializer.serialize(structure.getCompassTarget(), LocationStructure.class));
        if (structure.getBedSpawnLocation() != null)
            map.put(KEY_BED_SPAWN_LOCATION, serializer.serialize(structure.getBedSpawnLocation(), LocationStructure.class));
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

        if (!(structure.getRemoteAddress() == null && structure.getPort() == null && structure.getHostName() == null))
        {
            Map<String, Object> connection = new HashMap<>();
            MapUtils.putIfNotNull(connection, KEY_CONNECTION_IP, structure.getRemoteAddress().getHostAddress());
            MapUtils.putIfNotNull(connection, KEY_CONNECTION_PORT, structure.getPort());
            MapUtils.putIfNotNull(connection, KEY_CONNECTION_HOSTNAME, structure.getHostName());
            map.put(KEY_CONNECTION, connection);
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
        if (map.containsKey(KEY_COMPASS_TARGET))
            MapUtils.checkTypeIfContains(map, KEY_COMPASS_TARGET, Map.class);
        if (map.containsKey(KEY_BED_SPAWN_LOCATION))
            MapUtils.checkTypeIfContains(map, KEY_BED_SPAWN_LOCATION, Map.class);
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
        validateLivingEntity(map);

        HumanEntityStructure human = deserializeHuman(map, serializer);

        String name = (String) map.get(KEY_NAME);

        Boolean online = MapUtils.getOrNull(map, KEY_ONLINE);
        String displayName = MapUtils.getOrNull(map, KEY_DISPLAY_NAME);
        LocationStructure compassTarget = null;
        if (map.containsKey(KEY_COMPASS_TARGET))
            compassTarget = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_COMPASS_TARGET)), LocationStructure.class);
        LocationStructure bedSpawnLocation = null;
        if (map.containsKey(KEY_BED_SPAWN_LOCATION))
            bedSpawnLocation = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_BED_SPAWN_LOCATION)), LocationStructure.class);
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
            Map<String, Object> playerList = MapUtils.checkAndCastMap(map.get(KEY_PLAYER_LIST));
            playerListName = MapUtils.getOrNull(playerList, KEY_PLAYER_LIST_NAME);
            playerListHeader = MapUtils.getOrNull(playerList, KEY_PLAYER_LIST_HEADER);
            playerListFooter = MapUtils.getOrNull(playerList, KEY_PLAYER_LIST_FOOTER);
        }

        InetAddress remoteAddress = null;
        Integer portNumber = null;
        String hostName = null;
        if (map.containsKey(KEY_CONNECTION))
        {
            Map<String, Object> connection = MapUtils.checkAndCastMap(map.get(KEY_CONNECTION));
            if (connection.containsKey(KEY_CONNECTION_IP))
            {
                try
                {
                    remoteAddress = InetAddress.getByName(MapUtils.getOrNull(connection, KEY_CONNECTION_IP));
                }
                catch (UnknownHostException e)
                {
                    throw new IllegalArgumentException("Failed to parse the player IP address", e);
                }
            }
            portNumber = MapUtils.getAsNumber(connection, KEY_CONNECTION_PORT, Number::intValue);
            hostName = MapUtils.getOrNull(connection, KEY_CONNECTION_HOSTNAME);
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
                remoteAddress,
                portNumber,
                hostName,
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

    public static PlayerStructure of(@NotNull Player player)
    {
        return new PlayerStructureImpl(
                HumanEntityStructureImpl.ofHuman(player),
                player.getName(),
                player.isOnline(),
                player.getAddress() == null ? null: player.getAddress().getAddress(),
                player.getAddress() == null ? null: player.getAddress().getPort(),
                player.getAddress() == null ? null: player.getAddress().getHostName(),
                player.getDisplayName(),
                player.getPlayerListName(),
                player.getPlayerListHeader(),
                player.getPlayerListFooter(),
                LocationStructureImpl.of(player.getCompassTarget()),
                player.getBedSpawnLocation() == null ? null: LocationStructureImpl.of(player.getBedSpawnLocation()),
                (int) (player.getExp() * 100),
                player.getLevel(),
                player.getTotalExperience(),
                player.getAllowFlight(),
                player.isFlying(),
                player.getWalkSpeed(),
                player.getFlySpeed(),
                guessOpLevel(player),
                player
                        .getEffectivePermissions()
                        .stream()
                        .map(PermissionAttachmentInfo::getPermission)
                        .collect(Collectors.toList())
        );
    }

    private static int guessOpLevel(@NotNull Player player)
    {
        if (!player.isOp())
            return 0;

        /* 1 – Can edit spawn protection
         * 2 – Can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, and /tp, and can edit command blocks
         * 3 – Can use /ban, /deop, /kick, and /op
         * 4 – Can use /stop
         */

        boolean canTP = player.hasPermission("minecraft.command.tp");
        boolean canOP = player.hasPermission("minecraft.command.op");
        boolean canStop = player.hasPermission("minecraft.command.stop");

        if (canTP && canOP && canStop)
            return 4;
        else if (canTP && canOP)
            return 3;
        else if (canTP)
            return 2;
        else
            return 1;
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
                && Objects.equals(this.online, that.online)
                && Objects.equals(this.remoteAddress, that.remoteAddress)
                && Objects.equals(this.port, that.port)
                && Objects.equals(this.hostName, that.hostName)
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

    @Override
    @SuppressWarnings("deprecation")
    public void applyTo(Player player)
    {
        super.applyToHumanEntity(player);

        if (this.displayName != null)
            player.setDisplayName(this.displayName);
        if (this.playerListName != null)
            player.setPlayerListName(this.playerListName);
        if (this.playerListHeader != null)
            player.setPlayerListHeader(this.playerListHeader);
        if (this.playerListFooter != null)
            player.setPlayerListFooter(this.playerListFooter);
        if (this.compassTarget != null)
            player.setCompassTarget(this.compassTarget.create());
        if (this.bedSpawnLocation != null)
            player.setBedSpawnLocation(this.bedSpawnLocation.create());
        if (this.exp != null)
            player.setExp(this.exp);
        if (this.level != null)
            player.setLevel(this.level);
        if (this.totalExperience != null)
            player.setTotalExperience(this.totalExperience);
        if (this.allowFlight != null)
            player.setAllowFlight(this.allowFlight);
        if (this.flying != null)
            player.setFlying(this.flying);
        if (this.walkSpeed != null)
            player.setWalkSpeed(this.walkSpeed);
        if (this.flySpeed != null)
            player.setFlySpeed(this.flySpeed);

        if (this.opLevel != null)
            player.setOp(this.opLevel > 0);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof Player;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isAdequate(Player player, boolean strict)
    {
        return super.isAdequateHumanEntity(player, strict)
                && (this.name == null || this.name.equals(player.getName()))
                && (this.displayName == null || this.displayName.equals(player.getDisplayName()))
                && (this.online == null || this.online.equals(player.isOnline()))
                && (this.remoteAddress == null || this.isIPAdequate(player))
                && (this.playerListName == null || this.playerListName.equals(player.getPlayerListName()))
                && (this.playerListHeader == null || this.playerListHeader.equals(player.getPlayerListHeader()))
                && (this.playerListFooter == null || this.playerListFooter.equals(player.getPlayerListFooter()))
                && (this.compassTarget == null || this.compassTarget.isAdequate(player.getCompassTarget(), strict))
                && (this.bedSpawnLocation == null || this.bedSpawnLocation.isAdequate(player.getBedSpawnLocation(), strict))
                && (this.exp == null || this.exp == player.getExp())
                && (this.level == null || this.level.equals(player.getLevel()))
                && (this.totalExperience == null || this.totalExperience.equals(player.getTotalExperience()))
                && (this.allowFlight == null || this.allowFlight.equals(player.getAllowFlight()))
                && (this.flying == null || this.flying.equals(player.isFlying()))
                && (this.walkSpeed == null || this.walkSpeed.equals(player.getWalkSpeed()))
                && (this.flySpeed == null || this.flySpeed.equals(player.getFlySpeed()))
                && (this.activePermissions == null || this.activePermissions.stream().allMatch(player::hasPermission));
    }

    private boolean isIPAdequate(Player player)
    {
        InetSocketAddress playerAddr = player.getAddress();
        if (playerAddr == null
                && !(this.remoteAddress == null && this.port == null && this.hostName == null))
            return false;

        return (this.remoteAddress == null || this.remoteAddress.equals(playerAddr.getAddress()))
                && (this.port == null || this.port.equals(playerAddr.getPort()))
                && (this.hostName == null || this.hostName.equals(playerAddr.getHostName()));
    }
}
