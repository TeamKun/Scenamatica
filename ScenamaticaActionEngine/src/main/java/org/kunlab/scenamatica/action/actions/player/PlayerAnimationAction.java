package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerAnimationAction extends AbstractPlayerAction<PlayerAnimationAction.Argument>
        implements Watchable<PlayerAnimationAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_animation";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();

        PlayerUtils.getActorOrThrow(engine, player)
                .playAnimation(argument.type);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerAnimationEvent;

        PlayerAnimationEvent e = (PlayerAnimationEvent) event;

        return e.getAnimationType() == argument.type;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerAnimationEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkEnumName(map, Argument.KEY_ACTION_TYPE, PlayerAnimationType.class);

        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getAsEnum(map, Argument.KEY_ACTION_TYPE, PlayerAnimationType.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ACTION_TYPE = "type";

        @NotNull
        PlayerAnimationType type;

        public Argument(@NotNull String target, @NotNull PlayerAnimationType type)
        {
            super(target);
            this.type = type;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSameTarget(arg)
                    && this.type == arg.type;
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_TARGET_PLAYER, this.getTargetSpecifier(),
                    KEY_ACTION_TYPE, this.type
            );
        }
    }
}
