package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;

@Action("block")
public class BlockAction extends AbstractBlockAction
        implements Executable, Requireable
{
    public static final InputToken<BlockStructure> IN_DATA = ofInput(
            "data",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, ctxt);
        Block block = location.getBlock();

        this.makeOutputs(ctxt, block, null);

        BlockStructure dataDef = ctxt.input(IN_DATA);
        dataDef.apply(location);
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, ctxt);
        Block block = location.getBlock();

        boolean result = ctxt.hasInput(IN_DATA) ? ctxt.input(IN_DATA).isAdequate(block):
                !block.isEmpty();

        if (result)
            this.makeOutputs(ctxt, block, null);

        return result;
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_DATA);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_DATA);

        return board;
    }
}
