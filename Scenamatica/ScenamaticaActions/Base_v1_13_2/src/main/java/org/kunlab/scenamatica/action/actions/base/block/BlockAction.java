package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;

@Action("block")
@ActionDoc(
        name = "ブロック",
        description = "ブロックの状態や振る舞い、属性を変更します。",

        executable = "指定されたブロックの状態・属性を変更します。",
        requireable = "指定されたブロックの状態・属性を検証します。",
        outputs = {
                @OutputDoc(
                        name = "block",
                        description = "変更されたブロックです。",
                        type = Block.class
                )
        },
        admonitions = {
                @Admonition(
                        type = AdmonitionType.DANGER,
                        on = ActionMethod.REQUIRE,
                        content = "引数 `data` が空な場合、ブロックが空であるか（空気かどうか）を判定します。\n" +
                                "\n" +
                                "参考：[Block#isEmpty()V](https://jd.papermc.io/paper/1.16/org/bukkit/block/Block.html#isEmpty--)"
                )
        }
)
public class BlockAction extends AbstractBlockAction
        implements Executable, Requireable
{
    @InputDoc(
            name = "data",
            description = "変更するブロックのデータを指定します。",
            type = BlockStructure.class
    )
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
