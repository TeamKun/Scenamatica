package org.kunlab.scenamatica.action.actions.base.entity;

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
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.commons.utils.VoxelUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

@ActionMeta("projectile_hit")
public class ProjectileHitAction extends AbstractEntityAction<Projectile, ProjectileStructure>
        implements Executable, Watchable
{
    public static final InputToken<EntitySpecifier<Entity>> IN_HIT_ENTITY = ofSpecifier("hitEntity");
    public static final InputToken<BlockStructure> IN_HIT_BLOCK = ofInput(
            "hitBlock",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );
    public static final InputToken<BlockFace> IN_BLOCK_FACE = ofEnumInput("blockFace", BlockFace.class);
    public static final InputToken<Boolean> IN_EVENT_ONLY = ofInput("eventOnly", Boolean.class, false);
    public static final String KEY_OUT_HIT_ENTITY = "hitEntity";
    public static final String KEY_OUT_HIT_BLOCK = "hitBlock";
    public static final String KEY_OUT_BLOCK_FACE = "blockFace";

    public ProjectileHitAction()
    {
        super(Projectile.class, ProjectileStructure.class);
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Projectile target = this.selectTarget(ctxt);
        Entity hitEntity = ctxt.input(IN_HIT_ENTITY).selectTarget(ctxt.getContext()).orElse(null);
        Block hitBlock = null;
        if (ctxt.hasInput(IN_HIT_BLOCK))
            hitBlock = ctxt.input(IN_HIT_BLOCK).getBlockSafe();

        BlockFace face = ctxt.orElseInput(IN_BLOCK_FACE, () -> null);
        this.makeOutputs(ctxt, target, hitBlock, face);
        if (ctxt.input(IN_EVENT_ONLY))
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
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        assert event instanceof ProjectileHitEvent;
        ProjectileHitEvent e = (ProjectileHitEvent) event;

        boolean result = ctxt.ifHasInput(IN_HIT_ENTITY, specifier -> specifier.checkMatchedEntity(e.getHitEntity()))
                && ctxt.ifHasInput(IN_BLOCK_FACE, face -> face == e.getHitBlockFace())
                && ctxt.ifHasInput(IN_HIT_BLOCK, block -> {
            Block hitBlock = e.getHitBlock();
            return hitBlock != null && block.isAdequate(hitBlock);
        });
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getHitBlock(), e.getHitBlockFace());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Projectile entity, @Nullable Block hitBlock, @Nullable BlockFace blockFace)
    {
        ctxt.output(KEY_OUT_HIT_ENTITY, entity);
        if (hitBlock != null)
            ctxt.output(KEY_OUT_HIT_BLOCK, hitBlock);
        if (blockFace != null)
            ctxt.output(KEY_OUT_BLOCK_FACE, blockFace);
        super.makeOutputs(ctxt, entity);
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
