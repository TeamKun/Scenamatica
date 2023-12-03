package org.kunlab.scenamatica.scenariofile.structures.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.scenariofile.structures.entity.EntityStructureImpl;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class ProjectileStructureImpl extends EntityStructureImpl implements ProjectileStructure
{
    EntityStructure shooter;  // ProjectileSource だが, launchProjectile 以外存在しないので EntityStructure で代用。
    Boolean doesBounce;

    public ProjectileStructureImpl(@NotNull EntityStructure original, @Nullable EntityStructure shooter, @Nullable Boolean doesBounce)
    {
        super(original);
        this.shooter = shooter;
        this.doesBounce = doesBounce;
    }

    public static Map<String, Object> serialize(@NotNull ProjectileStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = EntityStructureImpl.serialize(structure, serializer);

        if (structure.getShooter() != null)
            MapUtils.putMapIfNotEmpty(map, KEY_SHOOTER, serializer.serialize(structure.getShooter(), EntityStructure.class));

        MapUtils.putIfNotNull(map, KEY_DOES_BOUNCE, structure.getDoesBounce());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        EntityStructureImpl.validate(map);

        MapUtils.checkTypeIfContains(map, KEY_DOES_BOUNCE, Boolean.class);

        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(map.get(KEY_SHOOTER));
            EntityStructureImpl.validate(shooterMap);
        }
    }

    @NotNull
    public static ProjectileStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        EntityStructure entity = EntityStructureImpl.deserialize(map, serializer);

        EntityStructure shooter = null;
        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(map.get(KEY_SHOOTER));
            shooter = serializer.deserialize(shooterMap, EntityStructure.class);
        }

        return new ProjectileStructureImpl(
                entity,
                shooter,
                MapUtils.getOrNull(map, KEY_DOES_BOUNCE)
        );
    }

    @Override
    public void applyTo(Projectile object)
    {

    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof Projectile;
    }

    @Override
    public boolean isAdequate(Projectile object, boolean strict)
    {
        return false;
    }

    @Override
    public Projectile create()
    {
        return null;
    }
}
