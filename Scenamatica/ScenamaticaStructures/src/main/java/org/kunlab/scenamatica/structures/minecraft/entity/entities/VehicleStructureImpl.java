package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.VehicleStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VehicleStructureImpl extends EntityStructureImpl implements VehicleStructure
{
    public VehicleStructureImpl(EntityType type, LocationStructure location, Vector velocity, String customName, UUID uuid, Boolean glowing, Boolean gravity, Boolean silent, Boolean customNameVisible, Boolean invulnerable, @NotNull List<String> tags, Integer maxHealth, Integer health, DamageStructure lastDamageCause, Integer fireTicks, Integer ticksLived, Integer portalCooldown, Boolean persistent, Float fallDistance)
    {
        super(type, location, velocity, customName, uuid, glowing, gravity, silent, customNameVisible, invulnerable, tags, maxHealth, health, lastDamageCause, fireTicks, ticksLived, portalCooldown, persistent, fallDistance);
    }

    public VehicleStructureImpl(@NotNull EntityStructure original)
    {
        super(original);
    }

    public VehicleStructureImpl(@Nullable EntityType type, @NotNull EntityStructure original)
    {
        super(type, original);
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull VehicleStructure structure, @NotNull StructureSerializer serializer)
    {
        return EntityStructureImpl.serialize(structure, serializer);
    }

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        EntityStructureImpl.validate(node);
    }

    @NotNull
    public static VehicleStructureImpl deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        return new VehicleStructureImpl(EntityStructureImpl.deserialize(node, serializer));
    }

    public static VehicleStructure of(@NotNull Vehicle vehicle, @NotNull StructureSerializer serializer)
    {
        return new VehicleStructureImpl(EntityStructureImpl.of(vehicle));
    }
}
