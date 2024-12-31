package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Action("block_explode")
@ActionDoc(
        name = "ブロックの爆発",
        description = "ブロックを爆発させます。",
        events = BlockExplodeEvent.class,

        executable = "ブロックを爆発させます。",
        expectable = "ブロックが爆発するまで待機します。",
        requireable = ActionDoc.UNALLOWED,

        admonitions = {
                @Admonition(
                        type = AdmonitionType.INFORMATION,
                        content = "このアクションは, TNT 等の起爆や, それの爆発についてのアクションではありません。\n  " +
                                "これは, 何らかの理由で爆風が発生し, 周囲のブロックが破壊されることについてのアクションです。"
                ),
                @Admonition(
                        type = AdmonitionType.INFORMATION,
                        content = "このアクションでは, TNT の爆発を検知・作成できません。\n  " +
                                "それを行うためには, 代わりに `entity_explode` アクションを使用してください。"
                )
        }
)
public class BlockExplodeAction extends AbstractBlockAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "爆発の大きさ",
            description = "爆発の大きさ（周囲のブロックに影響する範囲）を半径で指定します。",
            type = float.class,
            requiredOn = ActionMethod.EXECUTE,

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.TIP,
                            content = "ベッド爆発の大きさは `5.0f`, ウィザーの頭蓋骨爆発の大きさは `1.0f` です。"
                    )
            }
    )
    public static final InputToken<Float> IN_YIELD = ofInput("yield", Float.class);

    @InputDoc(
            name = "周囲の着火",
            description = "爆発によって周囲のブロックに火が付くかどうかを指定します。",
            type = boolean.class,

            availableFor = ActionMethod.EXECUTE
    )
    public static final InputToken<Boolean> IN_FIRE = ofInput("fire", Boolean.class);

    @InputDoc(
            name = "ブロックの破壊",
            description = "爆発によって周囲のブロックが破壊されるかどうかを指定します。",
            type = boolean.class,

            availableFor = ActionMethod.EXECUTE,

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            content = "この値はデフォルトで `true` です。"
                    )
            }
    )
    public static final InputToken<Boolean> IN_BREAK_BLOCKS = ofInput("breakBlocks", Boolean.class);

    @InputDoc(
            name = "影響を受けるブロック",
            description = "爆発によって影響を受ける（破壊される）周囲のブロックの一覧を指定します。",
            type = BlockStructure.class,

            availableFor = ActionMethod.EXPECT
    )
    public static final InputToken<List<BlockStructure>> IN_BLOCKS = ofInput(
            "blocks",
            InputTypeToken.ofList(BlockStructure.class),
            ofListDeserializer(ofDeserializer(BlockStructure.class))
    );

    private static boolean isBrokenBlocksMatches(@NotNull ActionContext ctxt, List<Block> blocks)
    {
        List<BlockStructure> blockDefs = ctxt.input(IN_BLOCKS);
        Predicate<BlockStructure> blockDefPredicate = blockDef -> blocks.stream().anyMatch(blockDef::isAdequate);

        return blockDefs.stream().allMatch(blockDefPredicate);
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, ctxt);
        Block block = location.getBlock();

        Float yield = ctxt.input(IN_YIELD);

        boolean setFire = ctxt.orElseInput(IN_FIRE, () -> false);
        boolean breakBlocks = ctxt.orElseInput(IN_BREAK_BLOCKS, () -> true);

        block.getWorld().createExplosion(location, yield, setFire, breakBlocks);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(ctxt, event))
            return false;

        assert event instanceof BlockExplodeEvent;
        BlockExplodeEvent explodeEvent = (BlockExplodeEvent) event;

        return ctxt.ifHasInput(IN_YIELD, yield -> explodeEvent.getYield() == yield)
                || ctxt.ifHasInput(IN_BLOCKS, blocks -> isBrokenBlocksMatches(ctxt, explodeEvent.blockList()));
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_YIELD);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_FIRE, IN_BREAK_BLOCKS)
                    .requirePresent(IN_YIELD);
        else
            board.register(IN_BLOCKS);

        return board;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(BlockExplodeEvent.class);
    }
}
