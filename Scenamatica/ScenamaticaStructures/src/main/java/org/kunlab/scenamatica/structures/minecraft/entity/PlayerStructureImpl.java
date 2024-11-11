package org.kunlab.scenamatica.structures.minecraft.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.structures.StructureValidators;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.HumanEntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.misc.LocationStructureImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class PlayerStructureImpl extends HumanEntityStructureImpl implements PlayerStructure
{
    private static final float SPEED_DEFAULT = 0.2f;

    protected final String name;
    protected final Boolean online;
    protected final InetAddress remoteAddress;
    protected final Integer port;
    protected final String hostName;
    protected final String displayName;
    protected final String playerListName;
    protected final String playerListHeader;
    protected final String playerListFooter;
    protected final LocationStructure compassTarget;
    protected final LocationStructure bedSpawnLocation;
    protected final Integer exp;
    protected final Integer level;
    protected final Integer totalExperience;
    protected final Boolean allowFlight;
    protected final Boolean flying;
    protected final Boolean sneaking;
    protected final Boolean sprinting;
    protected final Float walkSpeed;
    protected final Float flySpeed;

    protected final Integer opLevel;
    protected final List<String> activePermissions;

    public PlayerStructureImpl(@NotNull HumanEntityStructure human, @Nullable String name, Boolean online,
                               InetAddress remoteAddress, Integer port, String hostName, String displayName,
                               String playerListName, String playerListHeader,
                               String playerListFooter, LocationStructure compassTarget,
                               LocationStructure bedSpawnLocation, Integer exp,
                               Integer level, Integer totalExperience,
                               Boolean allowFlight, Boolean flying, Boolean sneaking, Boolean sprinting,
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
        this.sneaking = sneaking;
        this.sprinting = sprinting;
        this.walkSpeed = walkSpeed;
        this.flySpeed = flySpeed;
        this.opLevel = opLevel;
        this.activePermissions = activePermissions;
    }

    public PlayerStructureImpl(String name, Boolean online, InetAddress remoteAddress, Integer port, String hostName,
                               String displayName, String playerListName, String playerListHeader,
                               String playerListFooter, LocationStructure compassTarget,
                               LocationStructure bedSpawnLocation, Integer exp, Integer level, Integer totalExperience,
                               Boolean allowFlight, Boolean flying, Boolean sneaking, Boolean sprinting,
                               Float walkSpeed, Float flySpeed, Integer opLevel,
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
        this.sneaking = sneaking;
        this.sprinting = sprinting;
        this.walkSpeed = walkSpeed;
        this.flySpeed = flySpeed;
        this.opLevel = opLevel;
        this.activePermissions = activePermissions;
    }

    @NotNull
    public static Map<String, Object> serializePlayer(@NotNull PlayerStructure structure, @NotNull StructureSerializer serializer)
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
        MapUtils.putIfNotNull(map, KEY_SNEAKING, structure.getSneaking());
        MapUtils.putIfNotNull(map, KEY_SPRINTING, structure.getSprinting());


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

    public static void validatePlayer(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validateHuman(node, serializer);
        node.get(KEY_NAME).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_ONLINE).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_DISPLAY_NAME).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_COMPASS_TARGET).ensureTypeOfIfExists(YAMLNodeType.MAPPING);
        node.get(KEY_BED_SPAWN_LOCATION).ensureTypeOfIfExists(YAMLNodeType.MAPPING);
        node.get(KEY_EXP).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_LEVEL).ensureTypeOfIfExists(YAMLNodeType.INTEGER);
        node.get(KEY_TOTAL_EXPERIENCE).ensureTypeOfIfExists(YAMLNodeType.INTEGER);
        node.get(KEY_ALLOW_FLIGHT).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_FLYING).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_SNEAKING).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_SPRINTING).ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
        node.get(KEY_WALK_SPEED).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_FLY_SPEED).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_ACTIVE_PERMISSIONS).ensureTypeOfIfExists(YAMLNodeType.LIST);
        node.get(KEY_PLAYER_LIST).ensureTypeOfIfExists(YAMLNodeType.MAPPING);
        node.get(KEY_CONNECTION).ensureTypeOfIfExists(YAMLNodeType.MAPPING);

        StructuredYamlNode opNode = node.get(KEY_OP_LEVEL);
        opNode.ensureTypeOfIfExists(YAMLNodeType.INTEGER, YAMLNodeType.BOOLEAN);
        if (opNode.isType(YAMLNodeType.INTEGER))
            opNode.validate(StructureValidators.ranged(0, 4));

    }

    @NotNull
    public static PlayerStructure deserializePlayer(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validateLivingEntity(node);

        HumanEntityStructure human = deserializeHuman(node, serializer);

        String name = node.get(KEY_NAME).asString(null);

        Boolean online = node.get(KEY_ONLINE).asBoolean(null);
        String displayName = node.get(KEY_DISPLAY_NAME).asString(null);
        LocationStructure compassTarget = null;
        if (node.containsKey(KEY_COMPASS_TARGET))
            compassTarget = serializer.deserialize(node.get(KEY_COMPASS_TARGET), LocationStructure.class);
        LocationStructure bedSpawnLocation = null;
        if (node.containsKey(KEY_BED_SPAWN_LOCATION))
            bedSpawnLocation = serializer.deserialize(node.get(KEY_BED_SPAWN_LOCATION), LocationStructure.class);
        Integer exp = node.get(KEY_EXP).asInteger(null);
        Integer level = node.get(KEY_LEVEL).asInteger(null);
        Integer totalExperience = node.get(KEY_TOTAL_EXPERIENCE).asInteger(null);
        Boolean allowFlight = node.get(KEY_ALLOW_FLIGHT).asBoolean(null);
        Boolean flying = node.get(KEY_FLYING).asBoolean(null);
        Boolean sneaking = node.get(KEY_SNEAKING).asBoolean(null);
        Boolean sprinting = node.get(KEY_SPRINTING).asBoolean(null);
        Float walkSpeed = node.get(KEY_WALK_SPEED).asFloat(null);
        Float flySpeed = node.get(KEY_FLY_SPEED).asFloat(null);

        String playerListName = null;
        String playerListHeader = null;
        String playerListFooter = null;
        if (node.containsKey(KEY_PLAYER_LIST))
        {
            StructuredYamlNode playerListNode = node.get(KEY_PLAYER_LIST);
            playerListName = playerListNode.get(KEY_PLAYER_LIST_NAME).asString(null);
            playerListHeader = playerListNode.get(KEY_PLAYER_LIST_HEADER).asString(null);
            playerListFooter = playerListNode.get(KEY_PLAYER_LIST_FOOTER).asString(null);
        }

        InetAddress remoteAddress = null;
        Integer portNumber = null;
        String hostName = null;
        if (node.containsKey(KEY_CONNECTION))
        {
            StructuredYamlNode connectionNode = node.get(KEY_CONNECTION);
            remoteAddress = connectionNode.get(KEY_CONNECTION_HOSTNAME).getAs(n -> InetAddress.getByName(n.asString()));
            portNumber = connectionNode.get(KEY_CONNECTION_PORT).asInteger();
            hostName = connectionNode.get(KEY_CONNECTION_HOSTNAME).asString();
        }

        Integer opLevel = null;
        if (node.containsKey(KEY_OP_LEVEL))
        {
            StructuredYamlNode opLevelObj = node.get(KEY_OP_LEVEL);
            if (opLevelObj.isType(YAMLNodeType.BOOLEAN))
                opLevel = opLevelObj.asBoolean(null) ? 4: 0;
            else /* opLevel.isType(YAMLNodeType.INTEGER) */
                opLevel = opLevelObj.asInteger(null);
        }

        List<String> activePermissions = node.get(KEY_ACTIVE_PERMISSIONS).asList(StructuredYamlNode::asString);

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
                sneaking,
                sprinting,
                walkSpeed,
                flySpeed,
                opLevel,
                activePermissions
        );
    }

    public static PlayerStructure ofPlayer(@NotNull Player player)
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
                player.isSneaking(),
                player.isSprinting(),
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
                && Objects.equals(this.sneaking, that.sneaking)
                && Objects.equals(this.sprinting, that.sprinting)
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
                this.exp, this.level, this.totalExperience, this.allowFlight, this.flying, this.sneaking, this.sprinting,
                this.walkSpeed, this.flySpeed
        );
    }

    @Override
    public void applyTo(@NotNull Entity entity, boolean applyLocation)
    {
        super.applyTo(entity, applyLocation);
        if (!(entity instanceof Player))
            return;
        Player player = (Player) entity;

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
        if (this.sneaking != null)
            player.setSneaking(this.sneaking);
        if (this.sprinting != null)
            player.setSprinting(this.sprinting);
        if (this.walkSpeed != null)
            player.setWalkSpeed(this.walkSpeed);
        if (this.flySpeed != null)
            player.setFlySpeed(this.flySpeed);

        if (this.opLevel != null)
            player.setOp(this.opLevel > 0);
    }

    @Override
    public boolean isAdequate(@Nullable Entity entity, boolean strict)
    {
        if (!(super.isAdequate(entity, strict) && entity instanceof Player))
            return false;
        Player player = (Player) entity;

        return (this.name == null || this.name.equals(player.getName()))
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
                && (this.sneaking == null || this.sneaking.equals(player.isSneaking()))
                && (this.sprinting == null || this.sprinting.equals(player.isSprinting()))
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
