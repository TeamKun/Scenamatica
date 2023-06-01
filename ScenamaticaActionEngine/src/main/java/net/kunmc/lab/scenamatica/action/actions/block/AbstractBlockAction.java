package net.kunmc.lab.scenamatica.action.actions.block;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.utils.BeanUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

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

}
