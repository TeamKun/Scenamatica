package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.player.bucket.AbstractPlayerBucketAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractPlayerAction<A extends AbstractPlayerActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractPlayerAction<?>> getActions()
    {
        List<AbstractPlayerAction<?>> actions = new ArrayList<>(AbstractPlayerBucketAction.getActions());
        actions.add(new PlayerGameModeAction());
        actions.add(new PlayerAdvancementAction());
        actions.add(new PlayerAnimationAction());
        actions.add(new PlayerChatAction());
        actions.add(new PlayerDeathAction());
        actions.add(new PlayerDropItemAction());
        actions.add(new PlayerFlightAction());
        actions.add(new PlayerHotbarSlotAction());
        actions.add(new PlayerInteractBlockAction());
        actions.add(new PlayerItemBreakAction());
        actions.add(new PlayerItemConsumeAction());
        actions.add(new PlayerJoinAction());
        actions.add(new PlayerKickAction());
        actions.add(new PlayerLaunchProjectileAction());
        actions.add(new PlayerLevelChangeAction());
        actions.add(new PlayerMoveAction<>());
        actions.add(new PlayerQuitAction());
        actions.add(new PlayerRespawnAction());
        actions.add(new PlayerSneakAction());
        actions.add(new PlayerSprintAction());
        actions.add(new PlayerTeleportAction());

        return actions;
    }

    public boolean checkMatchedPlayerEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof PlayerEvent))
            return false;
        else if (argument.getTargetSpecifier() == null)
            return true;

        PlayerEvent e = (PlayerEvent) event;
        Player target = argument.getTarget();

        return Objects.equals(e.getPlayer().getUniqueId(), target.getUniqueId());
    }

    protected String deserializeTarget(Map<String, Object> map)
    {
        return (String) map.get(AbstractPlayerActionArgument.KEY_TARGET_PLAYER);
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
