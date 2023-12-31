package org.kunlab.scenamatica.action.actions.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBlockAction
        extends AbstractAction
{
    public static final InputToken<BlockStructure> IN_BLOCK = ofInput("block", BlockStructure.class,
            ofTraverser(Map.class, (ser, map) -> ser.deserialize(
                    MapUtils.checkAndCastMap(map),
                    BlockStructure.class
            ))
    );
    public static final String OUT_KEY_BLOCK = "block";
    public static final String OUT_KEY_PLAYER = "player";

    public static List<? extends AbstractBlockAction> getActions()
    {
        List<AbstractBlockAction> actions = new ArrayList<>();

        actions.add(new BlockBreakAction());
        actions.add(new BlockPlaceAction());

        return actions;
    }

    protected static void makeOutputs(@NotNull ActionContext ctxt, @NotNull Block block, @Nullable Player player)
    {
        ctxt.output(OUT_KEY_BLOCK, ctxt.getSerializer().toStructure(block, BlockStructure.class));
        if (player != null)
            ctxt.output(OUT_KEY_PLAYER, ctxt.getSerializer().toStructure(player, PlayerStructure.class));
        ctxt.commitOutput();
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_BLOCK);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_BLOCK);

        return board;
    }

    protected boolean checkMatchedBlockEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof BlockEvent))
            return false;

        BlockEvent e = (BlockEvent) event;

        return ctxt.ifHasInput(IN_BLOCK, block -> block.isAdequate(e.getBlock()));
    }

    protected Location getBlockLocationWithWorld(@NotNull BlockStructure block, @NotNull ActionContext ctxt)
    {
        return Utils.assignWorldToLocation(block.getLocation().create().clone(), ctxt.getEngine());
    }
}
