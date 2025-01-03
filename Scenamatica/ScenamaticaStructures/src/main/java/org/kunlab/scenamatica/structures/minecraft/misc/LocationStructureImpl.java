package org.kunlab.scenamatica.structures.minecraft.misc;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.HashMap;
import java.util.Map;

@Value
@AllArgsConstructor
public class LocationStructureImpl implements LocationStructure
{
    Double x;
    Double y;
    Double z;
    Float yaw;
    Float pitch;
    String world;

    public static LocationStructure of(Location location)
    {
        return new LocationStructureImpl(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw() == 0 ? null: location.getYaw(),
                location.getPitch() == 0 ? null: location.getPitch(),
                location.getWorld() == null ? null: location.getWorld().getName()
        );
    }

    public static Map<String, Object> serialize(LocationStructure location)
    {
        Map<String, Object> map = new HashMap<>();

        MapUtils.putIfNotNull(map, KEY_X, location.getX());
        MapUtils.putIfNotNull(map, KEY_Y, location.getY());
        MapUtils.putIfNotNull(map, kEY_Z, location.getZ());

        MapUtils.putIfNotNull(map, KEY_YAW, location.getYaw());
        MapUtils.putIfNotNull(map, KEY_PITCH, location.getPitch());

        MapUtils.putIfNotNull(map, KEY_WORLD, location.getWorld());

        return map;
    }

    public static void validate(StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_X).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_Y).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(kEY_Z).ensureTypeOfIfExists(YAMLNodeType.NUMBER);

        node.get(KEY_YAW).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_PITCH).ensureTypeOfIfExists(YAMLNodeType.NUMBER);

        node.get(KEY_WORLD).ensureTypeOfIfExists(YAMLNodeType.STRING);
    }

    public static LocationStructure deserialize(StructuredYamlNode node) throws YamlParsingException
    {
        validate(node);

        return new LocationStructureImpl(
                node.get(KEY_X).getAs(StructuredYamlNode::asDouble, null),
                node.get(KEY_Y).getAs(StructuredYamlNode::asDouble, null),
                node.get(kEY_Z).getAs(StructuredYamlNode::asDouble, null),
                node.get(KEY_YAW).getAs(StructuredYamlNode::asFloat, null),
                node.get(KEY_PITCH).getAs(StructuredYamlNode::asFloat, null),
                node.get(KEY_WORLD).getAs(StructuredYamlNode::asString, null)
        );
    }

    public static boolean isApplicable(Object o)
    {
        return o instanceof Location;
    }

    @Override
    public void applyTo(@NotNull Location object)
    {
        if (this.x != null)
            object.setX(this.x);
        if (this.y != null)
            object.setY(this.y);
        if (this.z != null)
            object.setZ(this.z);

        if (this.yaw != null)
            object.setYaw(this.yaw);
        if (this.pitch != null)
            object.setPitch(this.pitch);

        if (this.world != null)
            object.setWorld(Bukkit.getWorld(this.world));
    }

    @Override
    public boolean isAdequate(@Nullable Location object, boolean strict)
    {
        double x = object.getX();
        double y = object.getY();
        double z = object.getZ();

        float yaw = object.getYaw();
        float pitch = object.getPitch();

        String world = object.getWorld().getName();

        double EPSILON = strict ? 1e-3: 1;

        return (this.x == null || Math.abs(this.x - x) < EPSILON)
                && (this.y == null || Math.abs(this.y - y) < EPSILON)
                && (this.z == null || Math.abs(this.z - z) < EPSILON)
                && (this.yaw == null || Math.abs(this.yaw - yaw) < EPSILON)
                && (this.pitch == null || Math.abs(this.pitch - pitch) < EPSILON)
                && (this.world == null || (strict ? this.world.equals(world): this.world.equalsIgnoreCase(world)));
    }

    @Override
    public Location create()
    {
        return this.create(null);
    }

    @Override
    public LocationStructure changeWorld(String world)
    {
        return new LocationStructureImpl(this.x, this.y, this.z, this.yaw, this.pitch, world);
    }

    @Override
    public Location create(@Nullable World world)
    {
        World actualWorld = world;
        if (actualWorld == null)
            actualWorld = this.world == null ? null: Bukkit.getWorld(this.world);

        return new Location(
                actualWorld,
                this.x == null ? 0: this.x,
                this.y == null ? 0: this.y,
                this.z == null ? 0: this.z,
                this.yaw == null ? 0: this.yaw,
                this.pitch == null ? 0: this.pitch
        );
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Location{")
                .append("x=").append(this.x).append(", ")
                .append("y=").append(this.y).append(", ")
                .append("z=").append(this.z);
        if (this.yaw != null)
            builder.append(", yaw=").append(this.yaw);
        if (this.pitch != null)
            builder.append(", pitch=").append(this.pitch);

        if (this.world != null)
            builder.append(", world=").append(this.world);

        return builder.append("}").toString();
    }
}
