package org.kunlab.scenamatica.scenariofile.structures.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.GenericEntityStructure;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityStructureImpl extends GenericEntityStructureImpl implements EntityStructure
{
    public EntityStructureImpl(EntityType type, Location location, Vector velocity, String customName, UUID uuid, Boolean glowing, Boolean gravity, Boolean silent, Boolean customNameVisible, Boolean invulnerable, @NotNull List<String> tags, Integer maxHealth, Integer health, DamageStructure lastDamageCause, @NotNull List<PotionEffect> potionEffects, Integer fireTicks, Integer ticksLived, Integer portalCooldown, Boolean persistent, Float fallDistance)
    {
        super(type, location, velocity, customName, uuid, glowing, gravity, silent, customNameVisible, invulnerable, tags, maxHealth, health, lastDamageCause, potionEffects, fireTicks, ticksLived, portalCooldown, persistent, fallDistance);
    }

    public EntityStructureImpl()
    {
    }

    public EntityStructureImpl(GenericEntityStructure original)
    {
        super(original);
    }

    @NotNull
    public static EntityStructureImpl deserialize(@NotNull Map<String, Object> structure, @NotNull StructureSerializer serializer)
    {
        return new EntityStructureImpl(GenericEntityStructureImpl.deserialize(structure, serializer));
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull EntityStructure structure, @NotNull StructureSerializer serializer)
    {
        return GenericEntityStructureImpl.serialize(structure, serializer);
    }

    public static void validate(@NotNull Map<String, Object> structure)
    {
        GenericEntityStructureImpl.validate(structure);
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
        return target instanceof Entity;
    }

    @Override
    public boolean isAdequate(Entity entity, boolean strict)
    {
        return super.isAdequateEntity(entity, strict);
    }
}
