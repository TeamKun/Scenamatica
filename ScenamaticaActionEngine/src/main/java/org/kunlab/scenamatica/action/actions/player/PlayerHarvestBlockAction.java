package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.block.BlockBreakAction;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerHarvestBlockAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_harvest_block";

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

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        BlockBreakAction breakAction = engine.getManager().getRegistry().getActionManager().getCompiler()
                .findAction(BlockBreakAction.class);

        InputBoard blockBreakBoard = breakAction.getInputBoard(ScenarioType.ACTION_EXECUTE);
        blockBreakBoard.getHolder(BlockBreakAction.IN_BLOCK).set(argument.get(IN_HARVESTED_BLOCK));
        blockBreakBoard.getHolder(BlockBreakAction.IN_ACTOR).set(argument.get(IN_TARGET));

        breakAction.execute(engine, blockBreakBoard);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerHarvestBlockEvent;
        PlayerHarvestBlockEvent e = (PlayerHarvestBlockEvent) event;

        return argument.ifPresent(IN_HARVESTED_BLOCK, block -> block.isAdequate(e.getHarvestedBlock()))
                && argument.ifPresent(
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
