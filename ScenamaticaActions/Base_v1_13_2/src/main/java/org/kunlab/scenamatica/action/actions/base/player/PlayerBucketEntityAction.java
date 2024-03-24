package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;

// 注： AbstractPlayerBucketAction を継承していない。
@ActionMeta("player_bucket_entity")
public class PlayerBucketEntityAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final InputToken<EntitySpecifier<Entity>> IN_ENTITY = ofSpecifier("entity");
    public static final InputToken<ItemStackStructure> IN_ORIGINAL_BUCKET = ofInput(
            "bucket",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final InputToken<ItemStackStructure> IN_ENTITY_BUCKET = ofInput(
            "entityBucket",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    ).validator(bucket -> bucket.getType() != null, "Original bucket type is null.")
            .validator(bucket -> canBucketPickupEntity(bucket.getType()), "Original bucket type is not water bucket.");
    public static final String KEY_OUT_ENTITY = "entity";
    public static final String KEY_OUT_BUCKET = "bucket";
    public static final String KEY_OUT_ENTITY_BUCKET = "entityBucket";

    @SuppressWarnings("deprecation")
    private static boolean canBucketPickupEntity(@NotNull Material type)
    {
        return type == Material.WATER_BUCKET || type == Material.LEGACY_WATER_BUCKET;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        Actor actor = ctxt.getActorOrThrow(player);

        Entity targetEntity = ctxt.input(IN_ENTITY).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Target entity is not found."));
        // Null ではない
        ItemStack originalBucket = player.getInventory().getItemInMainHand();
        if (ctxt.hasInput(IN_ORIGINAL_BUCKET))
        {
            ItemStackStructure structureOriginalBucket = ctxt.input(IN_ORIGINAL_BUCKET);
            if (!structureOriginalBucket.isAdequate(originalBucket))
            {
                originalBucket = structureOriginalBucket.create();
                player.getInventory().setItemInMainHand(originalBucket);
            }
        }
        else if (!canBucketPickupEntity(originalBucket.getType()))
            throw new IllegalStateException("The item in main hand is not water bucket, but " + originalBucket.getType() +
                    ". Please ensure that the player is holding correct bucket or specify original bucket.");

        this.makeOutputs(ctxt, player, targetEntity, originalBucket, null);

        actor.interactEntity(
                targetEntity,
                NMSEntityUseAction.INTERACT,
                NMSHand.MAIN_HAND,
                null
        );
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerBucketEntityEvent e = (PlayerBucketEntityEvent) event;

        boolean result = ctxt.ifHasInput(IN_ENTITY, entity -> entity.checkMatchedEntity(e.getEntity()))
                && ctxt.ifHasInput(IN_ORIGINAL_BUCKET, bucket -> bucket.isAdequate(e.getOriginalBucket()))
                && ctxt.ifHasInput(IN_ENTITY_BUCKET, bucket -> bucket.isAdequate(e.getEntityBucket()));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getEntity(), e.getOriginalBucket(), e.getEntityBucket());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Entity entity, @NotNull ItemStack originalBucket, @Nullable ItemStack entityBucket)
    {
        ctxt.output(KEY_OUT_ENTITY, entity);
        ctxt.output(KEY_OUT_BUCKET, originalBucket);
        if (entityBucket != null)
            ctxt.output(KEY_OUT_ENTITY_BUCKET, entityBucket);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketEntityEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ENTITY, IN_ORIGINAL_BUCKET);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ENTITY);
        else
            board.register(IN_ENTITY_BUCKET);

        return board;
    }
}
