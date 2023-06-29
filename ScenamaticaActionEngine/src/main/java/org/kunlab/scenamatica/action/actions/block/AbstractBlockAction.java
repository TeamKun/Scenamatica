package org.kunlab.scenamatica.action.actions.block;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.Utils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBlockAction<A extends AbstractBlockActionArgument>
        extends AbstractAction<A>
{
    public static List<? extends AbstractBlockAction<?>> getActions()
    {
        List<AbstractBlockAction<?>> actions = new ArrayList<>();

        actions.add(new BlockBreakAction());
        actions.add(new BlockPlaceAction());

        return actions;
    }

    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof BlockEvent))
            return false;

        BlockEvent e = (BlockEvent) event;

        return BeanUtils.isSame(argument.block, e.getBlock(), engine);
    }

    public BlockBean deserializeBlock(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return serializer.deserializeBlock(MapUtils.checkAndCastMap(
                        map.get(AbstractBlockActionArgument.KEY_BLOCK),
                        String.class,
                        Object.class
                )
        );
    }

    protected Location getBlockLocationWithWorld(@NotNull BlockBean block, @NotNull ScenarioEngine engine)
    {
        return Utils.assignWorldToLocation(block.getLocation().clone(), engine);
    }
}
