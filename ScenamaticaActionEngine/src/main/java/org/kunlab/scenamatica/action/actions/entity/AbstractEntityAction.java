package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractEntityAction<A extends AbstractEntityActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractAction<?>> getActions()
    {
        List<AbstractAction<?>> actions = new ArrayList<>();

        actions.add(new EntityAction());
        actions.add(new EntityDamageAction());
        actions.add(new EntitySpawnAction());  // AbstractEntityAction を継承してない(引数都合)

        return actions;
    }

    public boolean checkMatchedEntityEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;

        return EntityUtils.selectEntities(argument.getTargetString())
                .stream().parallel()
                .anyMatch(entity -> Objects.equals(entity.getUniqueId(), e.getEntity().getUniqueId()));
    }

    protected String deserializeTarget(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractEntityActionArgument.KEY_TARGET_ENTITY);

        return map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY).toString();
    }
}
