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

@EqualsAndHashCode(callSuper = true)
@Value
public class ProjectileStructureImpl extends EntityStructureImpl<Projectile> implements ProjectileStructure
{

    EntityStructure<?> shooter;  // ProjectileSource だが, launchProjectile 以外存在しないので EntityStructure で代用。
    Boolean doesBounce;

    public ProjectileStructureImpl(@NotNull EntityStructure<?> original, @Nullable EntityStructure<?> shooter, @Nullable Boolean doesBounce)
    {
        super(original);
        this.shooter = shooter;
        this.doesBounce = doesBounce;
    }

    public static Map<String, Object> serialize(@NotNull ProjectileStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = serializer.serialize(structure, EntityStructure.class);

        if (structure.getShooter() != null)
            MapUtils.putMapIfNotEmpty(map, KEY_SHOOTER, serializer.serialize(structure.getShooter(), EntityStructure.class));

        MapUtils.putIfNotNull(map, KEY_DOES_BOUNCE, structure.getDoesBounce());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        serializer.validate(map, EntityStructure.class);

        MapUtils.checkTypeIfContains(map, KEY_DOES_BOUNCE, Boolean.class);

        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(map.get(KEY_SHOOTER));
            serializer.validate(shooterMap, EntityStructure.class);
        }

    }

    @NotNull
    public static ProjectileStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        EntityStructure<?> entity = serializer.deserialize(map, EntityStructure.class);

        EntityStructure<?> shooter = null;
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
    public boolean canApplyTo(Object target)
    {
        return super.canApplyTo(target) && target instanceof Projectile;
    }
}
