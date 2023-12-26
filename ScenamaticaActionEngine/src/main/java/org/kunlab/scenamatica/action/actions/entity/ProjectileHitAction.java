package org.kunlab.scenamatica.action.actions.entity;

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
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

public class ProjectileHitAction extends AbstractEntityAction<Projectile>
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "projectile_hit";
    public static final InputToken<EntitySpecifier<Entity>> IN_HIT_ENTITY = ofSpecifier("hitEntity");
    public static final InputToken<BlockStructure> IN_HIT_BLOCK = ofInput(
            "hitBlock",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );
    public static final InputToken<BlockFace> IN_BLOCK_FACE = ofEnumInput("blockFace", BlockFace.class);
    public static final InputToken<Boolean> IN_EVENT_ONLY = ofInput("eventOnly", Boolean.class, false);

    public ProjectileHitAction()
    {
        super(Projectile.class, ProjectileStructure.class);
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Projectile target = this.selectTarget(argument, engine.getContext());
        Entity hitEntity = argument.get(IN_HIT_ENTITY).selectTarget(engine.getContext()).orElse(null);
        Block hitBlock = null;
        if (argument.isPresent(IN_HIT_BLOCK))
            hitBlock = argument.get(IN_HIT_BLOCK).getBlockSafe();

        BlockFace face = argument.orElse(IN_BLOCK_FACE, null);
        if (argument.get(IN_EVENT_ONLY))
            this.doEventOnlyMode(target, hitEntity, hitBlock, face);
        else
            this.doNormalMode(target, hitEntity, hitBlock, face);
    }

    private void doNormalMode(@NotNull Projectile target, @Nullable Entity hitEntity, @Nullable Block hitBlock, @Nullable BlockFace blockFace)
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

    private void doEventOnlyMode(@NotNull Projectile target, @Nullable Entity hitEntity, @Nullable Block hitBlock, @Nullable BlockFace blockFace)
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
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        assert event instanceof ProjectileHitEvent;
        ProjectileHitEvent e = (ProjectileHitEvent) event;

        return argument.ifPresent(IN_HIT_ENTITY, specifier -> specifier.checkMatchedEntity(e.getHitEntity()))
                && argument.ifPresent(IN_BLOCK_FACE, face -> face == e.getHitBlockFace())
                && argument.ifPresent(IN_HIT_BLOCK, block -> {
            Block hitBlock = e.getHitBlock();
            return hitBlock != null && block.isAdequate(hitBlock);
        });
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ProjectileHitEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_HIT_ENTITY, IN_HIT_BLOCK, IN_BLOCK_FACE);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.register(IN_EVENT_ONLY)
                    .requirePresent(IN_HIT_ENTITY);

        return board;
    }


}
