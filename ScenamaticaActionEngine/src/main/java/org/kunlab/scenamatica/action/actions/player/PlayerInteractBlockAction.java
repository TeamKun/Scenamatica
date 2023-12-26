package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.Collections;
import java.util.List;

public class PlayerInteractBlockAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_interact_block";
    public static final InputToken<Action> IN_ACTION = ofEnumInput(
            "action",
            Action.class
    );
    public static final InputToken<EquipmentSlot> IN_HAND = ofEnumInput(
            "hand",
            EquipmentSlot.class
    ).validator(
            (slot) -> slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND,
            "The hand must be either hand or off hand"
    );
    public static final InputToken<BlockStructure> IN_BLOCK = ofInput(
            "block",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );
    public static final InputToken<BlockFace> IN_BLOCK_FACE = ofEnumInput(
            "blockFace",
            BlockFace.class
    );

    private static Block getClickBlock(ScenarioEngine engine, InputBoard argument)
    {
        Location clickPos;
        if (argument.isPresent(IN_BLOCK) && argument.ifPresent(IN_BLOCK, b -> b.getLocation() != null, false))  // 指定があったらその位置をクリックする
            clickPos = argument.get(IN_BLOCK).getLocation().create();
        else  // 指定がなかったら自身の位置をクリックする(しかない)
            clickPos = selectTarget(argument, engine).getLocation().toBlockLocation();

        Location normalizedPos = Utils.assignWorldToLocation(clickPos, engine);
        return normalizedPos.getWorld().getBlockAt(normalizedPos);
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Action action = argument.get(IN_ACTION);

        // 引数の検証を行う( validateArgument() はランタイムではないのでこちら側でやるしかない。)
        Block clickBlock = getClickBlock(engine, argument);
        if ((action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) && !clickBlock.getType().isAir())
            throw new IllegalArgumentException("Argument action is not allowed to be LEFT_CLICK_AIR or RIGHT_CLICK_AIR when the target block is not air");
        else if ((action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) && clickBlock.getType().isAir())
            throw new IllegalArgumentException("Argument action is not allowed to be LEFT_CLICK_BLOCK or RIGHT_CLICK_BLOCK when the target block is air");

        PlayerUtils.getActorOrThrow(engine, selectTarget(argument, engine))
                .interactAt(
                        action,
                        getClickBlock(engine, argument)
                );
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerInteractEvent;
        PlayerInteractEvent e = (PlayerInteractEvent) event;

        return argument.ifPresent(IN_ACTION, action -> action == e.getAction())
                && argument.ifPresent(IN_HAND, hand -> hand == e.getHand())
                && argument.ifPresent(IN_BLOCK, block -> block.isAdequate(e.getClickedBlock()))
                && argument.ifPresent(IN_BLOCK_FACE, face -> face == e.getBlockFace());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ACTION, IN_HAND, IN_BLOCK);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ACTION);
        else
            board.register(IN_BLOCK_FACE);

        return board;
    }
}
