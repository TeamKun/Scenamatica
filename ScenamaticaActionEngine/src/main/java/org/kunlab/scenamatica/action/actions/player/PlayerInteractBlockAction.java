package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerInteractBlockAction extends AbstractPlayerAction<PlayerInteractBlockAction.Argument>
        implements Executable<PlayerInteractBlockAction.Argument>, Watchable<PlayerInteractBlockAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_interact_block";

    private static Block getClickBlock(ScenarioEngine engine, Argument argument)
    {
        World world = engine.getManager().getRegistry().getContextManager().getStageManager().getStage();

        Location clickPos;
        BlockBean blockBean = argument.getBlock();
        if (!(blockBean == null || blockBean.getLocation() == null))
            clickPos = blockBean.getLocation().toBlockLocation();
        else  // 指定がなかったら自身の位置をクリックする(しかない)
            clickPos = argument.getTarget().getLocation().toBlockLocation();

        return world.getBlockAt(clickPos);
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

        Action action = argument.getAction();
        assert action != null;  // validateArgument()でチェック済み

        // 引数の検証を行う( validateArgument() はランタイムではないのでこちら側でやるしかない。)
        Block clickBlock = getClickBlock(engine, argument);
        if ((action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) && !clickBlock.getType().isAir())
            throw new IllegalArgumentException("Argument action is not allowed to be LEFT_CLICK_AIR or RIGHT_CLICK_AIR when the target block is not air");
        else if ((action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) && clickBlock.getType().isAir())
            throw new IllegalArgumentException("Argument action is not allowed to be LEFT_CLICK_BLOCK or RIGHT_CLICK_BLOCK when the target block is air");


        PlayerUtils.getActorOrThrow(engine, argument.getTarget()).interactAt(
                action,
                getClickBlock(engine, argument)
        );
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerInteractEvent;
        PlayerInteractEvent e = (PlayerInteractEvent) event;

        Action action = argument.getAction();
        if (action != null && action != e.getAction())
            return false;

        EquipmentSlot hand = argument.getHand();
        if (hand != null && hand != e.getHand())
            return false;

        BlockBean block = argument.getBlock();
        if (!(block == null || e.getClickedBlock() == null || BeanUtils.isSame(block, e.getClickedBlock(), engine)))
            return false;

        BlockFace blockFace = argument.getBlockFace();
        return blockFace == null || blockFace == e.getBlockFace();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        Action action = null;
        if (map.containsKey(Argument.KEY_ACTION))
            action = MapUtils.getAsEnum(map, Argument.KEY_ACTION, Action.class);

        EquipmentSlot hand = null;
        if (map.containsKey(Argument.KEY_HAND))
            hand = MapUtils.getAsEnum(map, Argument.KEY_HAND, EquipmentSlot.class);

        BlockBean block = null;
        if (map.containsKey(Argument.KEY_BLOCK))
            block = serializer.deserializeBlock(MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_BLOCK),
                    String.class,
                    Object.class
            ));

        BlockFace blockFace = null;
        if (map.containsKey(Argument.KEY_BLOCK_FACE))
            blockFace = MapUtils.getAsEnum(map, Argument.KEY_BLOCK_FACE, BlockFace.class);

        return new Argument(
                super.deserializeTarget(map),
                action,
                hand,
                block,
                blockFace
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ACTION = "action";
        public static final String KEY_HAND = "hand";
        public static final String KEY_BLOCK = "block";
        public static final String KEY_BLOCK_FACE = "block_face";
        @Nullable
        Action action;
        @Nullable
        EquipmentSlot hand;  // HAND or OFF_HAND
        @Nullable
        BlockBean block;
        @Nullable
        BlockFace blockFace;

        public Argument(@NotNull String target, @Nullable Action action, @Nullable EquipmentSlot hand, @Nullable BlockBean block, @Nullable BlockFace blockFace)
        {
            super(target);
            this.action = action;
            this.hand = hand;
            this.block = block;
            this.blockFace = blockFace;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && this.action == arg.action
                    && this.hand == arg.hand
                    && Objects.equals(this.block, arg.block)
                    && this.blockFace == arg.blockFace;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            EquipmentSlot hand = this.hand;
            if (!(hand == null || hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND))
                throw new IllegalArgumentException("Argument hand must be either HAND or OFF_HAND");

            if (type != ScenarioType.ACTION_EXECUTE)
                return;

            throwIfPresent(KEY_BLOCK_FACE, this.blockFace);

            Action action = this.action;
            if (action == null)
                throw new IllegalArgumentException("Argument action is not allowed to be null");
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ACTION, this.action,
                    KEY_HAND, this.hand,
                    KEY_BLOCK, this.block,
                    KEY_BLOCK_FACE, this.blockFace
            );
        }
    }
}
