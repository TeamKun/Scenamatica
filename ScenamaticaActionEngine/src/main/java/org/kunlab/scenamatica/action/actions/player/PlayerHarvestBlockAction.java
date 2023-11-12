package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.block.BlockBreakAction;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerHarvestBlockAction extends AbstractPlayerAction<PlayerHarvestBlockAction.Argument>
        implements Executable<PlayerHarvestBlockAction.Argument>, Watchable<PlayerHarvestBlockAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_harvest_block";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        BlockBreakAction.Argument blockBreakArgument = new BlockBreakAction.Argument(
                argument.getBlock(),
                argument.getTargetSpecifier(),
                null
        );

        BlockBreakAction breakAction = engine.getManager().getRegistry().getActionManager().getCompiler()
                .findAction(BlockBreakAction.class);

        breakAction.execute(engine, blockBreakArgument);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerHarvestBlockEvent e = (PlayerHarvestBlockEvent) event;

        List<ItemStackBean> items = argument.getItems();
        if (!items.isEmpty())
        {
            @NotNull
            List<ItemStack> drops = e.getItemsHarvested();
            for (ItemStackBean item : items)
            {
                boolean isExpectedItemHarvested =
                        drops.stream().anyMatch(drop -> BeanUtils.isSame(item, drop, false));
                if (!isExpectedItemHarvested)
                    return false;
            }
        }

        return (argument.block == null || BeanUtils.isSame(argument.getBlock(), e.getHarvestedBlock(), engine));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerHarvestBlockEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        BlockBean block = null;
        if (map.containsKey(Argument.KEY_HARVESTED_BLOCK))
            block = serializer.deserializeBlock(
                    MapUtils.checkAndCastMap(
                            map.get(Argument.KEY_HARVESTED_BLOCK),
                            String.class,
                            Object.class
                    )
            );

        List<ItemStackBean> items = new ArrayList<>();
        if (map.containsKey(Argument.KEY_ITEMS_HARVESTED))
        {
            List<Map<String, Object>> itemMaps = MapUtils.getAsList(map, Argument.KEY_ITEMS_HARVESTED);
            for (Map<String, Object> itemMap : itemMaps)
                items.add(serializer.deserializeItemStack(itemMap));
        }

        return new Argument(
                super.deserializeTarget(map),
                block,
                items
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_HARVESTED_BLOCK = "block";
        public static final String KEY_ITEMS_HARVESTED = "items";

        BlockBean block;
        @NotNull
        List<ItemStackBean> items;

        public Argument(String target, BlockBean block, @NotNull List<ItemStackBean> items)
        {
            super(target);
            this.block = block;
            this.items = items;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensurePresent(KEY_HARVESTED_BLOCK, this.block);
                ensurePresent(KEY_HARVESTED_BLOCK + BlockBean.KEY_BLOCK_LOCATION, this.block.getLocation());
                if (!this.items.isEmpty())
                    throw new IllegalArgumentException("cannot specify items in action execute mode");
            }
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.block, arg.block)
                    && this.items.equals(arg.items);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_HARVESTED_BLOCK, this.block,
                    KEY_ITEMS_HARVESTED, this.items
            );
        }
    }
}
