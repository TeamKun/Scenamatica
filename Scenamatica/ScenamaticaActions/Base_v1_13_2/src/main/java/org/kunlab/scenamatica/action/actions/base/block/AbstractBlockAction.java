package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;

import java.util.Map;

@Category(
        id = "block",
        name = "ブロック",
        description = "ブロックに関するアクションを提供します。"
)
@OutputDocs({
        @OutputDoc(
                name = AbstractBlockAction.OUT_KEY_BLOCK,
                description = "対象となったブロックです。",
                type = Block.class
        ),
        @OutputDoc(
                name = AbstractBlockAction.OUT_KEY_ACTOR,
                description = "結果をもたらしたアクタです。",
                type = Player.class
        )
})
public abstract class AbstractBlockAction
        extends AbstractAction
{
    @InputDoc(
            name = "block",
            description = "対象のブロックのデータを指定します。",
            type = BlockStructure.class,
            requiredOn = ActionMethod.EXECUTE
    )
    public static final InputToken<BlockStructure> IN_BLOCK = ofInput("block", BlockStructure.class,
            ofTraverser(Map.class, (ser, map) -> ser.deserialize(
                    MapUtils.checkAndCastMap(map),
                    BlockStructure.class
            ))
    );
    public static final String OUT_KEY_BLOCK = "block";
    public static final String OUT_KEY_ACTOR = "actor";

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Block block, @Nullable Player player)
    {
        ctxt.output(OUT_KEY_BLOCK, block);
        if (player != null)
            ctxt.output(OUT_KEY_ACTOR, player);
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
        if (block.getLocation() == null)
            throw new IllegalStateException("Block location is not specified");
        else if (block.getLocation().getX() == null || block.getLocation().getY() == null || block.getLocation().getZ() == null)
            throw new IllegalStateException("Unable to specify block location: " + block.getLocation());

        return Utils.assignWorldToLocation(block.getLocation().create().clone(), ctxt.getEngine());
    }
}
