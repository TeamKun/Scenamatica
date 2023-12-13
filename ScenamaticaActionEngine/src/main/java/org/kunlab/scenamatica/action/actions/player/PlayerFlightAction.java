package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerFlightAction extends AbstractPlayerAction<PlayerFlightAction.Argument>
        implements Executable<PlayerFlightAction.Argument>, Requireable<PlayerFlightAction.Argument>, Watchable<PlayerFlightAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_flight";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        assert argument.flying != null;

        boolean flying = argument.flying;

        Player player = argument.getTarget(engine);
        PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(player, flying);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (!player.getAllowFlight())
            player.setAllowFlight(true);  // IllegalStateException 回避

        player.setFlying(flying);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerToggleFlightEvent;
        PlayerToggleFlightEvent e = (PlayerToggleFlightEvent) event;

        return argument.flying == null || argument.flying == e.isFlying();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleFlightEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map, serializer),
                MapUtils.getOrNull(map, Argument.KEY_FLYING)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        assert argument.flying != null;
        boolean expectState = argument.flying;

        return argument.getTarget(engine).isFlying() == expectState;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_FLYING = "flying";

        @Nullable
        Boolean flying;

        public Argument(PlayerSpecifier target, @Nullable Boolean flying)
        {
            super(target);
            this.flying = flying;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument) && this.flying == arg.flying;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            switch (type)
            {
                case ACTION_EXECUTE:
                case CONDITION_REQUIRE:
                    ensurePresent(Argument.KEY_FLYING, this.flying);
                    break;
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_FLYING, this.flying
            );
        }
    }
}
