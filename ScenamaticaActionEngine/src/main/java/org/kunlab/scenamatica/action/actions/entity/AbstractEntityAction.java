package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractEntityAction<A extends AbstractEntityActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractEntityAction<?>> getActions()
    {
        List<AbstractEntityAction<?>> actions = new ArrayList<>();

        actions.add(new EntityDamageAction());

        return actions;
    }

    public boolean checkMatchedEntityEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;

        return Bukkit.selectEntities(Bukkit.getConsoleSender(), argument.getTargetString())
                .stream().parallel()
                .anyMatch(entity -> Objects.equals(entity.getUniqueId(), e.getEntity().getUniqueId()));
    }

    protected String deserializeTarget(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractEntityActionArgument.KEY_TARGET_ENTITY);

        return map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY).toString();
    }
}
