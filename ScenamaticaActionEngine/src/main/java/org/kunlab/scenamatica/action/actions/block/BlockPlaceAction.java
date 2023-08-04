package org.kunlab.scenamatica.action.actions.block;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BlockPlaceAction extends AbstractBlockAction<BlockPlaceAction.Argument>
        implements Executable<BlockPlaceAction.Argument>, Requireable<BlockPlaceAction.Argument>, Watchable<BlockPlaceAction.Argument>
{
    public static final String KEY_ACTION_NAME = "block_place";
    private static final BlockFace[] ALLOWED_FACES = {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player actor = argument.getActor();

        BlockBean blockDef = argument.getBlock();
        Location location = this.getBlockLocationWithWorld(blockDef, engine);

        Block block;
        if (actor == null)
        {
            block = location.getBlock();
            block.setType(blockDef.getType());
        }
        else
        {
            BlockFace direction = argument.getDirection() == null ? BlockFace.EAST: argument.getDirection();
            EquipmentSlot hand = argument.getHand() == null ? EquipmentSlot.HAND: argument.getHand();

            Actor scenarioActor = PlayerUtils.getActorOrThrow(engine, actor);
            scenarioActor.placeBlock(
                    location,
                    new ItemStack(blockDef.getType()),  // assert blockDef.getType() != null
                    hand,
                    direction
            );

            block = location.getBlock();  // 更新の必要があるため、共通化できない。
        }

        if (blockDef.getBiome() != null)
            block.setBiome(blockDef.getBiome());
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(argument, engine, event))
            return false;

        BlockPlaceEvent e = (BlockPlaceEvent) event;

        Player actor;
        if ((actor = argument.getActor()) != null)
        {
            Player placer = e.getPlayer();
            if (!placer.getUniqueId().equals(actor.getUniqueId()))
                return false;
        }

        if (e.getHand() != argument.getHand())
            return false;

        return this.isConditionFulfilled(argument, engine);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockPlaceEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeBlock(map, serializer),
                MapUtils.getOrNull(map, Argument.KEY_ACTOR),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_HAND, EquipmentSlot.class),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_DIRECTION, BlockFace.class)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        BlockBean blockDef = argument.getBlock();
        Block block = this.getBlockLocationWithWorld(blockDef, engine).getBlock();

        return BeanUtils.isSame(blockDef, block, engine.getContext().getStage());
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractBlockActionArgument
    {
        public static final String KEY_ACTOR = "actor";
        public static final String KEY_HAND = "hand";
        public static final String KEY_DIRECTION = "direction";

        @Nullable
        String actor;
        @Nullable
        EquipmentSlot hand;  // HAND or OFF_HAND
        @Nullable
        BlockFace direction;

        public Argument(@NotNull BlockBean block, @Nullable String actor, @Nullable EquipmentSlot hand, @Nullable BlockFace direction)
        {
            super(block);
            this.actor = actor;
            this.hand = hand;
            this.direction = direction;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument) &&
                    (this.actor == null || this.actor.equals(arg.actor))
                    && this.hand == arg.hand
                    && this.direction == arg.direction;

        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (this.direction != null
                    // Direction が有効かどうかのチェック。(NMS との互換性)
                    && Arrays.stream(ALLOWED_FACES).parallel().noneMatch(face -> face == this.direction))
                throw new IllegalArgumentException("Invalid direction: " + this.direction + ", allowed: " + Arrays.toString(ALLOWED_FACES));

            EquipmentSlot hand = this.hand;
            if (!(hand == null || hand == EquipmentSlot.OFF_HAND || hand == EquipmentSlot.HAND))
                throw new IllegalArgumentException("Invalid hand: " + this.hand + ", allowed: " + EquipmentSlot.HAND + ", " + EquipmentSlot.OFF_HAND);

            if (type == ScenarioType.ACTION_EXECUTE)
            {
                BlockBean blockDef = this.block;
                if (blockDef.getType() == null)
                    throw new IllegalArgumentException("Block type cannot be null");
            }
            else
                throwIfPresent(Argument.KEY_DIRECTION, this.direction);
        }

        @Nullable
        public Player getActor()
        {
            if (this.actor == null)
                return null;

            return PlayerUtils.getPlayerOrNull(this.actor);
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_ACTOR, this.actor,
                    KEY_HAND, this.hand,
                    KEY_DIRECTION, this.direction
            );
        }
    }
}
