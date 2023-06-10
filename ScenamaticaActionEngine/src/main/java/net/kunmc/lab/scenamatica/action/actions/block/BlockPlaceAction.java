package net.kunmc.lab.scenamatica.action.actions.block;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.BeanUtils;
import net.kunmc.lab.scenamatica.action.utils.EntityUtils;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BlockPlaceAction extends AbstractBlockAction<BlockPlaceAction.Argument> implements Requireable<BlockPlaceAction.Argument>
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

            Actor scenarioActor = EntityUtils.getActorOrThrow(engine, actor);
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
        if (!super.isFired(argument, engine, event))
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
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        assert argument != null;

        if (argument.getDirection() != null
                // Direction が有効かどうかのチェック。(NMS との互換性)
                && Arrays.stream(ALLOWED_FACES).parallel().noneMatch(face -> face == argument.getDirection()))
            throw new IllegalArgumentException("Invalid direction: " + argument.getDirection() + ", allowed: " + Arrays.toString(ALLOWED_FACES));

        EquipmentSlot hand = argument.getHand();
        if (!(hand == null || hand == EquipmentSlot.OFF_HAND || hand == EquipmentSlot.HAND))
            throw new IllegalArgumentException("Invalid hand: " + argument.getHand() + ", allowed: " + EquipmentSlot.HAND + ", " + EquipmentSlot.OFF_HAND);

        if (type == ScenarioType.ACTION_EXECUTE)
        {
            BlockBean blockDef = argument.getBlock();
            if (blockDef.getType() == null)
                throw new IllegalArgumentException("Block type cannot be null");
        }
        else
            this.throwIfPresent(Argument.KEY_DIRECTION, argument.getDirection());
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
            StringBuilder builder = new StringBuilder(super.getArgumentString());

            if (this.actor != null)
                builder.append(KEY_ACTOR).append("=").append(this.actor).append(",");
            if (this.hand != null)
                builder.append("hand=").append(this.hand).append(",");
            if (this.direction != null)
                builder.append("direction=").append(this.direction).append(",");

            return builder.toString();
        }
    }
}
