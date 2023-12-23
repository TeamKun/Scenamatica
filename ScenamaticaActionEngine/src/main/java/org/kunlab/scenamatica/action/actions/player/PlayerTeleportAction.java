package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerTeleportAction extends PlayerMoveAction<PlayerTeleportAction.Argument>
        implements Executable<PlayerTeleportAction.Argument>, Watchable<PlayerTeleportAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_teleport";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull PlayerTeleportAction.Argument argument)
    {
        Location toLoc = Utils.assignWorldToLocation(argument.getTo(), engine);
        PlayerTeleportEvent.TeleportCause cause = argument.getCause();
        if (cause == null)
            cause = PlayerTeleportEvent.TeleportCause.PLUGIN;

        argument.getTarget(engine).teleport(toLoc, cause);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        assert event instanceof PlayerTeleportEvent;
        PlayerTeleportEvent e = (PlayerTeleportEvent) event;

        return (argument.getCause() == null || argument.getCause() == e.getCause());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerTeleportEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {

        return new Argument(
                super.deserializeArgument(map, serializer),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_CAUSE, PlayerTeleportEvent.TeleportCause.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends PlayerMoveAction.Argument
    {
        public static final String KEY_CAUSE = "cause";

        PlayerTeleportEvent.TeleportCause cause;

        public Argument(PlayerMoveAction.Argument argument, PlayerTeleportEvent.TeleportCause cause)
        {
            super(argument.getTargetSpecifier(), argument.getFrom(), argument.getTo());

            this.cause = cause;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && this.cause == arg.cause;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_CAUSE, this.cause.name()
            );
        }
    }
}
