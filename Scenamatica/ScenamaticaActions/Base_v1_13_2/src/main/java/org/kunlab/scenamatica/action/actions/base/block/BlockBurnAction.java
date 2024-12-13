package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;

import java.util.Collections;
import java.util.List;

@Action("block_burn")
@ActionDoc(
        name = "ブロックの燃焼",
        description = "指定されたブロックを燃やします。",
        events = BlockBurnEvent.class,
        executable = "指定されたブロックの燃焼をシミュレートします。",
        expectable = "指定されたブロックが燃やされることを期待します。",

        outputs = {
                @OutputDoc(
                        name = BlockBurnAction.OUT_KEY_IGNITING_BLOCK,
                        description = "ブロックを燃やしたブロックです。",
                        type = Block.class
                )
        },

        admonitions = {
                @Admonition(
                        type = AdmonitionType.WARNING,
                        on = ActionMethod.EXECUTE,
                        content = "このアクションは, 内部で `BlockBurnEvent` を擬似的に発火させます。\n" +
                                "イベントをキャンセルしなかった場合, 対象のブロックは空気ブロックに置換されます。"
                )
        }
)
public class BlockBurnAction extends AbstractBlockAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "block",
            description = "燃やすブロックを指定します。",
            type = BlockStructure.class
    )
    public static final InputToken<BlockStructure> IN_IGNITING_BLOCK = ofInput(
            "ignitingBlock",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );

    public static final String OUT_KEY_IGNITING_BLOCK = "ignitingBlock";

    private static void validateBlock(Block block)
    {
        if (block.isEmpty())
            throw new IllegalActionInputException(IN_BLOCK, "The air block cannot be burned.");
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);

        Block block = super.getBlockLocationWithWorld(blockDef, ctxt).getBlock();
        Block ignitingBlock = null;
        if (ctxt.hasInput(IN_IGNITING_BLOCK))
        {
            BlockStructure ignitingBlockDef = ctxt.input(IN_IGNITING_BLOCK);
            ignitingBlock = super.getBlockLocationWithWorld(ignitingBlockDef, ctxt).getBlock();
        }

        validateBlock(block);

        this.makeOutputs(ctxt, block, ignitingBlock);
        // イベントを発火させ, ブロックの燃焼をシミュレートする
        BlockBurnEvent event = new BlockBurnEvent(block, ignitingBlock);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            block.setType(Material.AIR, true);

    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(ctxt, event))
            return false;

        assert event instanceof BlockBurnEvent;
        BlockBurnEvent e = (BlockBurnEvent) event;

        boolean result = (e.getIgnitingBlock() == null || ctxt.ifHasInput(IN_IGNITING_BLOCK, b -> b.isAdequate(e.getIgnitingBlock())));
        if (result)
            ctxt.output(OUT_KEY_BLOCK, e.getBlock());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Block block, @Nullable Block ignitingBlock)
    {
        ctxt.output(OUT_KEY_BLOCK, block);
        if (ignitingBlock != null)
            ctxt.output(OUT_KEY_IGNITING_BLOCK, ignitingBlock);
        ctxt.commitOutput();
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_IGNITING_BLOCK);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_IGNITING_BLOCK);

        return board;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockBurnEvent.class
        );
    }
}
