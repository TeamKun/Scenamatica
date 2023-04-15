package net.kunmc.lab.scenamatica.action.actions.entity;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractEntityAction<A extends AbstractEntityActionArgument> extends AbstractAction<A>
{
    @Override
    public boolean isFired(@NotNull A argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;

        return Objects.equals(e.getEntity().getUniqueId(), argument.getTarget().getUniqueId());
    }

    protected String deserializeTarget(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractEntityActionArgument.KEY_TARGET_ENTITY);

        return map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY).toString();
    }
}
