package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// 注： AbstractPlayerBucketAction を継承していない。
public class PlayerBucketEntityAction extends AbstractPlayerAction<PlayerBucketEntityAction.Argument>
        implements Executable<PlayerBucketEntityAction.Argument>, Watchable<PlayerBucketEntityAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_bucket_entity";

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
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        Player player = argument.getTarget(engine);
        Actor actor = PlayerUtils.getActorOrThrow(engine, player);

        Entity targetEntity = argument.getEntity().selectTarget(engine.getContext());

        // Null ではない
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (argument.getOriginalBucket() != null)
        {
            ItemStackStructure structureOriginalBucket = argument.getOriginalBucket();
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
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerBucketEntityEvent e = (PlayerBucketEntityEvent) event;

        return argument.getEntity().checkMatchedEntity(e.getEntity())
                || (argument.getEntityBucket() == null || argument.getEntityBucket().isAdequate(e.getEntityBucket()))
                || (argument.getOriginalBucket() == null || argument.getOriginalBucket().isAdequate(e.getOriginalBucket()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketEntityEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        ItemStackStructure originalBucket = null;
        if (map.containsKey(Argument.KEY_ORIGINAL_BUCKET))
            originalBucket = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_ORIGINAL_BUCKET)),
                    ItemStackStructure.class
            );

        ItemStackStructure entityBucket = null;
        if (map.containsKey(Argument.KEY_ENTITY_BUCKET))
            entityBucket = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_ENTITY_BUCKET)),
                    ItemStackStructure.class
            );

        return new Argument(
                super.deserializeTarget(map, serializer),
                EntitySpecifierImpl.tryDeserialize(map.get(Argument.KEY_ENTITY), serializer),
                originalBucket,
                entityBucket
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ENTITY = "entity";
        public static final String KEY_ORIGINAL_BUCKET = "bucket";
        public static final String KEY_ENTITY_BUCKET = "entityBucket";

        EntitySpecifier<?> entity;
        ItemStackStructure originalBucket;
        ItemStackStructure entityBucket;

        public Argument(PlayerSpecifier target, EntitySpecifier<?> entity, ItemStackStructure originalBucket, ItemStackStructure entityBucket)
        {
            super(target);
            this.entity = entity;
            this.originalBucket = originalBucket;
            this.entityBucket = entityBucket;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                if (!this.entity.canProvideTarget())
                    throw new IllegalStateException("Entity is not set.");
                ensureNotPresent(KEY_ENTITY_BUCKET, this.entityBucket);
            }

            if (this.originalBucket != null)
            {
                Material originalBucketType = this.originalBucket.getType();
                if (originalBucketType == null)
                    throw new IllegalStateException("Original bucket type is null.");
                else if (!canBucketPickupEntity(originalBucketType))
                    throw new IllegalStateException("Original bucket type is not water bucket.");
            }
        }
    }
}
