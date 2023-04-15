package net.kunmc.lab.scenamatica.action.actions.player;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractPlayerAction<A extends AbstractPlayerActionArgument> extends AbstractAction<A>
{
    @Override
    public boolean isFired(@NotNull A argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        if (!(event instanceof PlayerEvent))
            return false;

        PlayerEvent e = (PlayerEvent) event;
        Player target = argument.getTarget();

        return Objects.equals(e.getPlayer().getUniqueId(), target.getUniqueId());
    }

    protected String deserializeTarget(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, AbstractPlayerActionArgument.KEY_TARGET_PLAYER);

        return map.get(AbstractPlayerActionArgument.KEY_TARGET_PLAYER).toString();
    }
}
