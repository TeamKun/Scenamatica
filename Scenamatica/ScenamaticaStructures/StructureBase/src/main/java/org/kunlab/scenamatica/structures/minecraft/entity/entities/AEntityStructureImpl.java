package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.AEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AEntityStructureImpl extends EntityStructureImpl implements AEntityStructure
{
    public AEntityStructureImpl(EntityType type, LocationStructure location, Vector velocity, String customName, UUID uuid, Boolean glowing, Boolean gravity, Boolean silent, Boolean customNameVisible, Boolean invulnerable, @NotNull List<String> tags, Integer maxHealth, Integer health, DamageStructure lastDamageCause, Integer fireTicks, Integer ticksLived, Integer portalCooldown, Boolean persistent, Float fallDistance)
    {
        super(type, location, velocity, customName, uuid, glowing, gravity, silent, customNameVisible, invulnerable, tags, maxHealth, health, lastDamageCause, fireTicks, ticksLived, portalCooldown, persistent, fallDistance);
    }

    public AEntityStructureImpl()
    {
    }

    public AEntityStructureImpl(@NotNull EntityStructure original)
    {
        super(original);
    }

    @NotNull
    public static AEntityStructureImpl deserialize(@NotNull Map<String, Object> structure, @NotNull StructureSerializer serializer)
    {
        return new AEntityStructureImpl(EntityStructureImpl.deserialize(structure, serializer));
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull AEntityStructure structure, @NotNull StructureSerializer serializer)
    {
        return EntityStructureImpl.serialize(structure, serializer);
    }

    public static void validate(@NotNull Map<String, Object> structure)
    {
        EntityStructureImpl.validate(structure);
    }

    public static AEntityStructureImpl of(@NotNull Entity entity)
    {
        return new AEntityStructureImpl(EntityStructureImpl.of(entity));
    }

    public static boolean isApplicable(@NotNull Object obj)
    {
        return obj instanceof Entity;
    }

    @Override
    public Entity create()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyTo(Entity entity)
    {
        super.applyToEntity(entity);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return isApplicable(target);
    }

    @Override
    public boolean isAdequate(Entity entity, boolean strict)
    {
        return super.isAdequateEntity(entity, strict);
    }
}
