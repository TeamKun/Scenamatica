package org.kunlab.scenamatica.action.actions.player.bucket;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerBucketEmptyAction extends AbstractPlayerBucketAction<PlayerBucketEmptyAction.Argument>
        implements Watchable<PlayerBucketEmptyAction.Argument>, Executable<PlayerBucketEmptyAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_bucket_empty";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();
        Actor actor = PlayerUtils.getActorOrThrow(engine, player);
        ItemStack stack = getBucket(actor, argument);
        Block block = getPlaceAt(player, argument, engine);
        BlockFace direction = getDirection(player, block, argument);

        if (isEmptyBucket(stack.getType()))
            throw new IllegalArgumentException("The bucket is empty: " + stack.getType() + " held by " + player.getName());

        actor.placeItem(
                block.getLocation(),
                stack,
                direction
        );
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        return super.checkMatchedBucketEvent(argument, engine, event);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketEmptyEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(Argument.deserialize(map, serializer));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends BucketActionArgument
    {
        public Argument(BucketActionArgument origin)
        {
            super(origin);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                throwIfNotPresent(Argument.KEY_TARGET_PLAYER, this.getTargetSpecifier());

                if (this.getBlockClicked() == null && this.getBlock() == null)
                    throw new IllegalArgumentException("No block to place specified(" + Argument.KEY_BLOCK + " or " + Argument.KEY_BLOCK_CLICKED + " is required).");
            }
        }
    }
}
