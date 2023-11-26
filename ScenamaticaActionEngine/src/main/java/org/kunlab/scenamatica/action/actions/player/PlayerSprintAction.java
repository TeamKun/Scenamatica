package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerSprintAction extends AbstractPlayerAction<PlayerSprintAction.Argument>
        implements Executable<PlayerSprintAction.Argument>, Watchable<PlayerSprintAction.Argument>, Requireable<PlayerSprintAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_sprint";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        assert argument.sprinting != null;

        boolean sprinting = argument.sprinting;

        Player player = argument.getTarget();
        PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(player, sprinting);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        player.setSprinting(sprinting);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerToggleSprintEvent;
        PlayerToggleSprintEvent e = (PlayerToggleSprintEvent) event;

        return argument.sprinting == null || argument.sprinting == e.isSprinting();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleSprintEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_SPRINTING)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        assert argument.sprinting != null;
        boolean expectState = argument.sprinting;

        return argument.getTarget().isSprinting() == expectState;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_SPRINTING = "sprinting";

        @Nullable
        Boolean sprinting;

        public Argument(String target, @Nullable Boolean sprinting)
        {
            super(target);
            this.sprinting = sprinting;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument) &&
                    (this.sprinting == null || arg.sprinting == null || this.sprinting.equals(arg.sprinting));
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            switch (type)
            {
                case ACTION_EXECUTE:
                case CONDITION_REQUIRE:
                    ensurePresent(Argument.KEY_SPRINTING, this.sprinting);
                    break;
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_SPRINTING, this.sprinting
            );
        }
    }
}
