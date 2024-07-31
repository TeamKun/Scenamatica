package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.scenamatica.structures.minecraft.misc.BlockStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.misc.SelectorProjectileSourceStructureImpl;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.SelectorProjectileSourceStructure;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;
import org.kunlab.scenamatica.selector.Selector;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class ProjectileStructureImpl extends EntityStructureImpl implements ProjectileStructure
{
    ProjectileSourceStructure shooter;
    Boolean doesBounce;

    public ProjectileStructureImpl(@NotNull EntityStructure original, @Nullable ProjectileSourceStructure shooter, @Nullable Boolean doesBounce)
    {
        super(original);
        this.shooter = shooter;
        this.doesBounce = doesBounce;
    }

    public static Map<String, Object> serialize(@NotNull ProjectileStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = EntityStructureImpl.serialize(structure, serializer);

        MapUtils.putIfNotNull(map, KEY_DOES_BOUNCE, structure.getDoesBounce());

        ProjectileSourceStructure specifier = structure.getShooter();

        if (specifier == null)
            return map;

        if (specifier instanceof SelectorProjectileSourceStructure)
            MapUtils.putIfNotNull(map, KEY_SHOOTER, ((SelectorProjectileSourceStructure) specifier).getSelectorString());
        else
            map.put("shooter", serializer.serialize(specifier, ProjectileSourceStructure.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        EntityStructureImpl.validate(map);

        MapUtils.checkTypeIfContains(map, KEY_DOES_BOUNCE, Boolean.class);

        if (map.containsKey(KEY_SHOOTER) && map.get(KEY_SHOOTER) instanceof Map)
            serializer.validate(MapUtils.checkAndCastMap(map.get(KEY_SHOOTER)), ProjectileSourceStructure.class);
        else if (map.containsKey(KEY_SHOOTER) && !(map.get(KEY_SHOOTER) instanceof String))
            throw new IllegalArgumentException("Invalid ProjectileSourceStructure: " + map.get(KEY_SHOOTER));
    }

    @NotNull
    public static ProjectileStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        EntityStructure entity = EntityStructureImpl.deserialize(map, serializer);

        ProjectileSourceStructure shooter = null;
        if (map.containsKey(KEY_SHOOTER) && map.get(KEY_SHOOTER) instanceof Map)
            shooter = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_SHOOTER)), ProjectileSourceStructure.class);
        else if (map.containsKey(KEY_SHOOTER) && map.get(KEY_SHOOTER) instanceof String)
            shooter = new SelectorProjectileSourceStructureImpl((String) map.get(KEY_SHOOTER));

        return new ProjectileStructureImpl(
                entity,
                shooter,
                MapUtils.getOrNull(map, KEY_DOES_BOUNCE)
        );
    }

    private static ProjectileSourceStructure ofSource(ProjectileSource source, StructureSerializer serializer)
    {
        if (source instanceof BlockProjectileSource)
            return BlockStructureImpl.of(((BlockProjectileSource) source).getBlock());
        else if (source instanceof Entity)
            return serializer.toStructure((Entity) source, null);
        else
            throw new IllegalArgumentException("Unrecognized ProjectileSource: " + source);
    }

    public static ProjectileStructure ofSource(@NotNull Projectile projectile, @NotNull StructureSerializer serializer)
    {
        return new ProjectileStructureImpl(
                EntityStructureImpl.of(projectile),
                ofSource(projectile.getShooter(), serializer),
                projectile.doesBounce()
        );
    }

    @Override
    public void applyTo(Projectile projectile)
    {
        super.applyToEntity(projectile);

        if (this.doesBounce != null)
            projectile.setBounce(this.doesBounce);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof Projectile;
    }

    @Override
    public boolean isAdequate(Projectile entity, boolean strict)
    {
        if (!super.isAdequateEntity(entity, strict))
            return false;

        if (!(this.doesBounce == null || this.doesBounce == entity.doesBounce()))
            return false;

        if (this.shooter == null)
            return true;

        ProjectileSource projectileSource = entity.getShooter();
        if (projectileSource == null)
            return false;

        if (this.shooter instanceof EntityStructure && this.shooter instanceof Mapped<?>)
        {
            // noinspection unchecked
            return ((Mapped<Entity>) this.shooter).isAdequate((Entity) projectileSource);
        }
        else if (this.shooter instanceof BlockStructure)
        {
            if (!(projectileSource instanceof BlockProjectileSource))
                return false;

            Block block = ((BlockProjectileSource) projectileSource).getBlock();
            return ((BlockStructure) this.shooter).isAdequate(block, strict);
        }
        else if (this.shooter instanceof SelectorProjectileSourceStructure)
        {
            String selector = ((SelectorProjectileSourceStructure) this.shooter).getSelectorString();
            if (selector == null)
                return true;
            else if (!(projectileSource instanceof Entity))
                return false;

            return Selector.compile(selector).test(null, (Entity) projectileSource);
        }

        return false;
    }
}
