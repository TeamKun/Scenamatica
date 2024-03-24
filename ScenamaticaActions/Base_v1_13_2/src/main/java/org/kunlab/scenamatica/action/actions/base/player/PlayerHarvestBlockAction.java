package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.block.BlockBreakAction;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ActionMeta("player_harvest_block")
public class PlayerHarvestBlockAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final InputToken<BlockStructure> IN_HARVESTED_BLOCK = ofInput(
            "block",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    ).validator(ScenarioType.ACTION_EXECUTE, block -> block.getLocation() != null,
            "block must have location in action execute mode"
    );
    public static final InputToken<List<ItemStackStructure>> IN_ITEMS_HARVESTED = ofInput(
            "items",
            InputTypeToken.ofList(ItemStackStructure.class),
            ofTraverser(
                    List.class,
                    (ser, map) -> {
                        List<ItemStackStructure> items = new ArrayList<>();
                        List<Map<String, Object>> itemMaps = MapUtils.checkAndCastList(map, InputTypeToken.ofMap(String.class, Object.class));
                        for (Map<String, Object> itemMap : itemMaps)
                            items.add(ser.deserialize(itemMap, ItemStackStructure.class));
                        return items;
                    }
            )
    );

    public static final String KEY_BLOCK_HARVESTED = "block";
    public static final String KEY_ITEMS_HARVESTED = "items";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockBreakAction breakAction = ctxt.findAction(BlockBreakAction.class);

        InputBoard blockBreakBoard = breakAction.getInputBoard(ScenarioType.ACTION_EXECUTE);
        blockBreakBoard.getHolder(BlockBreakAction.IN_BLOCK).set(ctxt.input(IN_HARVESTED_BLOCK));
        blockBreakBoard.getHolder(BlockBreakAction.IN_ACTOR).set(ctxt.input(IN_TARGET));

        breakAction.execute(ctxt.renew(blockBreakBoard));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerHarvestBlockEvent;
        PlayerHarvestBlockEvent e = (PlayerHarvestBlockEvent) event;

        boolean result = ctxt.ifHasInput(IN_HARVESTED_BLOCK, block -> block.isAdequate(e.getHarvestedBlock()))
                && ctxt.ifHasInput(
                IN_ITEMS_HARVESTED,
                items -> {
                    @NotNull
                    List<ItemStack> drops = e.getItemsHarvested();
                    for (ItemStackStructure item : items)
                    {
                        boolean isExpectedItemHarvested =
                                drops.stream().anyMatch(item::isAdequate);
                        if (!isExpectedItemHarvested)
                            return false;
                    }
                    return false;
                }
        );

        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getHarvestedBlock(), e.getItemsHarvested());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Block block, List<ItemStack> items)
    {
        ctxt.output(KEY_BLOCK_HARVESTED, block);
        ctxt.output(KEY_ITEMS_HARVESTED, items);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerHarvestBlockEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_HARVESTED_BLOCK);
        if (type != ScenarioType.ACTION_EXECUTE)
            board.register(IN_ITEMS_HARVESTED);
        return board;
    }
}
