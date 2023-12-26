package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;

// 注： AbstractPlayerBucketAction を継承していない。
public class PlayerBucketEntityAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_bucket_entity";
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

    @SuppressWarnings("deprecation")
    private static boolean canBucketPickupEntity(@NotNull Material type)
    {
        return type == Material.WATER_BUCKET || type == Material.LEGACY_WATER_BUCKET;
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player player = selectTarget(argument, engine);
        Actor actor = PlayerUtils.getActorOrThrow(engine, player);

        Entity targetEntity = argument.get(IN_ENTITY).selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalStateException("Target entity is not found."));
        // Null ではない
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (argument.isPresent(IN_ORIGINAL_BUCKET))
        {
            ItemStackStructure structureOriginalBucket = argument.get(IN_ORIGINAL_BUCKET);
            if (!structureOriginalBucket.isAdequate(itemInMainHand))
            {
                ItemStack originalBucket = structureOriginalBucket.create();
                player.getInventory().setItemInMainHand(originalBucket);
            }
        }
        else if (!canBucketPickupEntity(itemInMainHand.getType()))
            throw new IllegalStateException("The item in main hand is not water bucket, but " + itemInMainHand.getType() +
                    ". Please ensure that the player is holding correct bucket or specify original bucket.");


        actor.interactEntity(
                targetEntity,
                NMSEntityUseAction.INTERACT,
                EquipmentSlot.HAND,
                null
        );
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerBucketEntityEvent e = (PlayerBucketEntityEvent) event;

        return argument.ifPresent(IN_ENTITY, entity -> entity.checkMatchedEntity(e.getEntity()))
                && argument.ifPresent(IN_ORIGINAL_BUCKET, bucket -> bucket.isAdequate(e.getOriginalBucket()))
                && argument.ifPresent(IN_ENTITY_BUCKET, bucket -> bucket.isAdequate(e.getEntityBucket()));
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
