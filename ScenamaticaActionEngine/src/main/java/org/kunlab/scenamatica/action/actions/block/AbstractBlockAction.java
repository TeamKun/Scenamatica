package org.kunlab.scenamatica.action.actions.block;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.commons.utils.StructureUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

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

    public boolean checkMatchedBlockEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof BlockEvent))
            return false;

        BlockEvent e = (BlockEvent) event;

        return argument.block == null || StructureUtils.isSame(argument.block, e.getBlock(), engine);
    }

    public BlockStructure deserializeBlockOrNull(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        if (!map.containsKey(AbstractBlockActionArgument.KEY_BLOCK))
            return null;

        return serializer.deserialize(
                MapUtils.checkAndCastMap(map.get(AbstractBlockActionArgument.KEY_BLOCK)),
                BlockStructure.class
        );
    }

    protected Location getBlockLocationWithWorld(@NotNull BlockStructure block, @NotNull ScenarioEngine engine)
    {
        return Utils.assignWorldToLocation(block.getLocation().clone(), engine);
    }
}
