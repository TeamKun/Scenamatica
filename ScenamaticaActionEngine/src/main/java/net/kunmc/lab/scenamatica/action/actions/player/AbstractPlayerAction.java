package net.kunmc.lab.scenamatica.action.actions.player;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractPlayerAction<A extends AbstractPlayerActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractPlayerAction<?>> getActions()
    {
        List<AbstractPlayerAction<?>> actions = new ArrayList<>();

        actions.add(new PlayerGameModeAction());
        actions.add(new PlayerAdvancementAction());
        actions.add(new PlayerAnimationAction());
        actions.add(new PlayerDeathAction());
        actions.add(new PlayerHotbarSlotAction());
        actions.add(new PlayerInteractBlockAction());
        actions.add(new PlayerLaunchProjectileAction());
        actions.add(new PlayerJoinAction());

        return actions;
    }

    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
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

    protected boolean isSameUUIDString(String uuid1, String uuid2)
    {
        if (uuid1 == null || uuid2 == null)
            return uuid1 == null && uuid2 == null;

        String normalizedUUID1 = uuid1.replace("-", "").toLowerCase();
        String normalizedUUID2 = uuid2.replace("-", "").toLowerCase();

        return normalizedUUID1.equals(normalizedUUID2);
    }
}
