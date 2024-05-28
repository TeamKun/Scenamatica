package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;

import java.util.Collections;
import java.util.List;

@Action("block_break")
@ActionDoc(
        name = "ブロックの破壊",
        description = "指定されたブロックを破壊します。",
        events = BlockBreakEvent.class,

        executable = "指定されたブロックを破壊します。",
        watchable = "指定されたブロックが破壊されることを期待します。",
        requireable = "指定されたブロックが空気ブロックであるかどうか判定します。"
)
public class BlockBreakAction extends AbstractBlockAction
        implements Executable, Requireable, Watchable
{
    @InputDoc(
            name = "actor",
            description = "ブロックを破壊するアクタです。",
            type = PlayerSpecifier.class,
            availableFor = {ActionMethod.EXECUTE, ActionMethod.WATCH}
    )
    public static final InputToken<PlayerSpecifier> IN_ACTOR = ofInput("actor", PlayerSpecifier.class, ofPlayer());
    @InputDoc(
            name = "dropItems",
            description = "アイテムをドロップするかどうかを指定します。",
            type = boolean.class,
            availableFor = {ActionMethod.WATCH}
    )
    public static final InputToken<Boolean> IN_DROP_ITEMS = ofInput("dropItems", Boolean.class);

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, ctxt);
        Block block = location.getBlock();

        Player player = ctxt.input(IN_ACTOR).selectTarget(ctxt.getContext()).orElse(null);
        if (player == null)
        {
            block.breakNaturally();  // 自然に壊れたことにする
            return;
        }

        this.validateBreakable(block, player);

        this.makeOutputs(ctxt, block, player);
        NMSEntityPlayer nmsPlayer = NMSProvider.getProvider().wrap(player);
        NMSBlockPosition nmsBlockPosition = NMSProvider.getProvider().wrap(location);
        nmsPlayer.getInteractManager().breakBlock(nmsBlockPosition);
    }

    private void validateBreakable(@NotNull Block block, @NotNull Player player)
    {
        World world = block.getWorld();
        World playerWorld = player.getWorld();

        if (world != playerWorld)  // 同値比較でよい
            throw new IllegalArgumentException("The block and the player must be in the same world.");
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(ctxt, event))
            return false;

        BlockBreakEvent e = (BlockBreakEvent) event;

        boolean result = ctxt.ifHasInput(IN_ACTOR, player -> player.checkMatchedPlayer(e.getPlayer()))
                && ctxt.ifHasInput(IN_DROP_ITEMS, dropItems -> dropItems == e.isDropItems());
        if (result)
            this.makeOutputs(ctxt, e.getBlock(), e.getPlayer());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockBreakEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ACTOR);

        switch (type)
        {
            case ACTION_EXECUTE:
            case ACTION_EXPECT:
                board.register(IN_DROP_ITEMS);
                break;
            case CONDITION_REQUIRE:
                board.validator(
                                b -> b.ifPresent(IN_BLOCK, block -> block.getType() == null),
                                "The block must be specified in the condition requiring mode."
                        )
                        .validator(
                                b -> b.ifPresent(IN_BLOCK, block -> block.getBiome() == null),
                                "Cannot specify the biome in the condition requiring mode."
                        )
                        .validator(
                                b -> b.ifPresent(IN_BLOCK, block -> block.getLightLevel() == null),
                                "Cannot specify the light level in the condition requiring mode."
                        );
                break;
        }

        return board;
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Location loc = this.getBlockLocationWithWorld(ctxt.input(IN_BLOCK), ctxt);

        return loc.getBlock().getType() == Material.AIR;
    }
}
