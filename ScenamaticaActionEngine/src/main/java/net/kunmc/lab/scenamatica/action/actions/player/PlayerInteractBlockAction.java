package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.BeanUtils;
import net.kunmc.lab.scenamatica.action.utils.EntityUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.scenariofile.beans.misc.BlockBeanImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerInteractBlockAction extends AbstractPlayerAction<PlayerInteractBlockAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_interact_block";

    private static Block getClickBlock(ScenarioEngine engine, Argument argument)
    {
        World world = engine.getManager().getRegistry().getContextManager().getStageManager().getStage();

        Location clickPos;
        BlockBean blockBean = argument.getBlock();
        if (blockBean != null)
            clickPos = new Location(world, blockBean.getX(), blockBean.getY(), blockBean.getZ());
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


        EntityUtils.getActorOrThrow(engine, argument.getTarget()).interactAt(
                action,
                getClickBlock(engine, argument)
        );
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
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
        if (block != null && !BeanUtils.isSame(block, e.getClickedBlock()))
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
    public Argument deserializeArgument(@NotNull Map<String, Object> map)
    {
        Action action = null;
        if (map.containsKey(Argument.KEY_ACTION))
            action = MapUtils.getAsEnum(map, Argument.KEY_ACTION, Action.class);

        EquipmentSlot hand = null;
        if (map.containsKey(Argument.KEY_HAND))
            hand = MapUtils.getAsEnum(map, Argument.KEY_HAND, EquipmentSlot.class);

        BlockBean block = null;
        if (map.containsKey(Argument.KEY_BLOCK))
            block = BlockBeanImpl.deserialize(MapUtils.checkAndCastMap(
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

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        super.validateArgument(engine, type, argument);

        if (argument == null)
            return;

        EquipmentSlot hand = argument.getHand();
        if (!(hand == null || hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND))
            throw new IllegalArgumentException("Argument hand must be either HAND or OFF_HAND");

        if (type != ScenarioType.ACTION_EXECUTE)
            return;

        if (argument.getBlockFace() != null)
            throw new IllegalArgumentException("Argument block_face is not allowed in action execute");

        Action action = argument.getAction();
        if (action == null)
            throw new IllegalArgumentException("Argument action is not allowed to be null");
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
        public String getArgumentString()
        {
            StringBuilder builder = new StringBuilder();
            if (this.action != null)
                builder.append(", ").append(KEY_ACTION).append("=").append(this.action);
            if (this.hand != null)
                builder.append(", ").append(KEY_HAND).append("=").append(this.hand);
            if (this.block != null)
                builder.append(", ").append(KEY_BLOCK).append("=").append(this.block);
            if (this.blockFace != null)
                builder.append(", ").append(KEY_BLOCK_FACE).append("=").append(this.blockFace);

            return builder.toString();
        }
    }
}
