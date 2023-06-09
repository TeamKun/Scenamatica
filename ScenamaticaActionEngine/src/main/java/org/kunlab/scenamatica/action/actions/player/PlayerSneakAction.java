package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerSneakAction extends AbstractPlayerAction<PlayerSneakAction.Argument>
        implements Requireable<PlayerSneakAction.Argument>, Watchable<PlayerSneakAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_sneak";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        assert argument.sneaking != null;

        boolean sneaking = argument.sneaking;

        Player player = argument.getTarget();
        PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(player, sneaking);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        player.setSneaking(sneaking);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerToggleSneakEvent;
        PlayerToggleSneakEvent e = (PlayerToggleSneakEvent) event;

        return argument.sneaking == null || argument.sneaking == e.isSneaking();
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        super.validateArgument(engine, type, argument);

        if (argument == null)
            return;

        switch (type)
        {
            case ACTION_EXECUTE:
            case CONDITION_REQUIRE:
                this.throwIfNotPresent(Argument.KEY_SNEAKING, argument.sneaking);
                break;
        }
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleSneakEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_SNEAKING)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        assert argument.sneaking != null;
        boolean expectState = argument.sneaking;

        return argument.getTarget().isSneaking() == expectState;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_SNEAKING = "sneaking";

        @Nullable
        Boolean sneaking;

        public Argument(@NotNull String target, @Nullable Boolean sneaking)
        {
            super(target);
            this.sneaking = sneaking;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument) &&
                    (this.sneaking == null || arg.sneaking == null || this.sneaking.equals(arg.sneaking));
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_SNEAKING, this.sneaking
            );
        }
    }
}
