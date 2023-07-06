package org.kunlab.scenamatica.action.actions.inventory.players;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractInventoryActionByPlayerAction<A extends AbstractInventoryActionByPlayerArgument>
        extends AbstractInventoryAction<A>
{
    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        Player target = argument.getTarget();

        return Objects.equals(this.getPlayerFromEvent(event).getUniqueId(), target.getUniqueId());
    }

    protected abstract Player getPlayerFromEvent(Event event);

    protected String deserializeTarget(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractInventoryActionByPlayerArgument.KEY_TARGET_PLAYER);

        return map.get(AbstractInventoryActionByPlayerArgument.KEY_TARGET_PLAYER).toString();
    }
}
