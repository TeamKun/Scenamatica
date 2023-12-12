package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.VoxelUtils;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectileHitAction extends AbstractEntityAction<ProjectileHitAction.Argument>
        implements Executable<ProjectileHitAction.Argument>, Watchable<ProjectileHitAction.Argument>
{
    public static final String KEY_ACTION_NAME = "projectile_hit";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        assert argument.getTargetHolder() != null;
        Projectile target = argument.getTargetHolder().selectTarget(engine.getContext());
        if (target == null)
            throw new IllegalStateException("Target is not found");

        Entity hitEntity = null;
        if (argument.getHitEntity() != null)
        {
            hitEntity = argument.getHitEntity().selectTarget(engine.getContext());
            if (hitEntity == null)
                throw new IllegalStateException("Hit entity is not found");
        }

        Block hitBlock = null;
        if (argument.getHitBlock() != null)
            hitBlock = argument.getHitBlock().getBlockSafe();

        if (argument.isEventOnly())
            this.doEventOnlyMode(engine, target, hitEntity, hitBlock, argument.getBlockFace());
        else
            this.doNormalMode(engine, target, hitEntity, hitBlock, argument.getBlockFace());
    }

    private void doNormalMode(ScenarioEngine engine, @NotNull Projectile target, @Nullable Entity hitEntity, @Nullable Block hitBlock, @Nullable BlockFace blockFace)
    {
        if (hitEntity != null)
        {
            Location loc = hitEntity.getLocation();
            target.teleport(loc);

            return;
        }

        if (hitBlock == null)
            return;

        Location blockLoc = hitBlock.getLocation();

        if (blockFace == null)
            blockFace = VoxelUtils.getOpenFace(blockLoc);

        Location frontLoc = VoxelUtils.getFrontOf(target.getLocation(), blockFace, 0.2);
        target.teleport(frontLoc);

        Vector blockVec = blockLoc.toVector().normalize();

        target.setVelocity(blockVec.multiply(0.02));
    }

    private void doEventOnlyMode(@NotNull ScenarioEngine engine, @NotNull Projectile target, @Nullable Entity hitEntity, @Nullable Block hitBlock, @Nullable BlockFace blockFace)
    {
        ProjectileHitEvent event = new ProjectileHitEvent(
                target,
                hitEntity,
                hitBlock,
                blockFace
        );

        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        assert event instanceof ProjectileHitEvent;
        ProjectileHitEvent e = (ProjectileHitEvent) event;

        if (argument.getHitBlock() != null)
        {
            if (e.getHitBlock() == null)
                return false;
            else if (!argument.getHitBlock().isAdequate(e.getHitBlock()))
                return false;
        }

        return argument.getHitEntity() == null || argument.getHitEntity().checkMatchedEntity(e.getHitEntity())
                && (argument.getHitBlock() == null || argument.getHitBlock().isAdequate(e.getHitBlock()))
                && (argument.getBlockFace() == null || argument.getBlockFace() == e.getHitBlockFace());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ProjectileHitEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        BlockStructure hitBlock = null;
        if (map.containsKey(Argument.KEY_HIT_BLOCK))
            hitBlock = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_HIT_BLOCK)),
                    BlockStructure.class
            );

        return new Argument(
                super.deserializeTarget(map, serializer, ProjectileStructure.class),
                EntitySpecifierImpl.tryDeserialize(map.get(Argument.KEY_HIT_ENTITY), serializer, EntityStructure.class),
                hitBlock,
                MapUtils.getAsEnumOrNull(map, Argument.KEY_BLOCK_FACE, BlockFace.class),
                MapUtils.getOrDefault(map, Argument.KEY_EVENT_ONLY, false)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Projectile>
    {
        public static final String KEY_HIT_ENTITY = "hitEntity";
        public static final String KEY_HIT_BLOCK = "hitBlock";
        public static final String KEY_BLOCK_FACE = "blockFace";
        public static final String KEY_EVENT_ONLY = "eventOnly";

        EntitySpecifier<?> hitEntity;
        BlockStructure hitBlock;
        BlockFace blockFace;
        boolean eventOnly;

        public Argument(
                @Nullable EntitySpecifier<Projectile> target,
                @Nullable EntitySpecifier<?> hitEntity,
                @Nullable BlockStructure hitBlock,
                @Nullable BlockFace blockFace,
                boolean eventOnly
        )
        {
            super(target);
            this.hitEntity = hitEntity;
            this.hitBlock = hitBlock;
            this.blockFace = blockFace;
            this.eventOnly = eventOnly;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            if (type == ScenarioType.ACTION_EXECUTE)
                ensurePresent(KEY_TARGET_ENTITY, this.getTargetHolder());
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.hitEntity, arg.hitEntity)
                    && Objects.equals(this.hitBlock, arg.hitBlock)
                    && this.blockFace == arg.blockFace
                    && this.eventOnly == arg.eventOnly;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_HIT_ENTITY, this.hitEntity,
                    KEY_HIT_BLOCK, this.hitBlock,
                    KEY_BLOCK_FACE, this.blockFace,
                    KEY_EVENT_ONLY, this.eventOnly
            );
        }
    }
}
