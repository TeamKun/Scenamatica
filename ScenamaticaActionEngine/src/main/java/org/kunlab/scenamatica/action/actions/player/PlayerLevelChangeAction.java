package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;
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
import java.util.Objects;

public class PlayerLevelChangeAction extends AbstractPlayerAction<PlayerLevelChangeAction.Argument>
        implements Watchable<PlayerLevelChangeAction.Argument>, Executable<PlayerLevelChangeAction.Argument>,
        Requireable<PlayerLevelChangeAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_level_change";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget(engine);
        player.setLevel(argument.getNewLevel());
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerLevelChangeEvent e = (PlayerLevelChangeEvent) event;

        return (argument.getOldLevel() == null || argument.getOldLevel() == e.getOldLevel())
                && (argument.getNewLevel() == null || argument.getNewLevel() == e.getNewLevel());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerLevelChangeEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map, serializer),
                MapUtils.getAsNumberOrNull(map, Argument.KEY_OLD_LEVEL, Number::intValue),
                MapUtils.getAsNumberOrNull(map, Argument.KEY_NEW_LEVEL, Number::intValue)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget(engine);
        return player.getLevel() == argument.getNewLevel();
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_OLD_LEVEL = "oldLevel";
        public static final String KEY_NEW_LEVEL = "level";

        Integer oldLevel;
        Integer newLevel;

        public Argument(PlayerSpecifier target, Integer oldLevel, Integer newLevel)
        {
            super(target);
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
                ensureNotPresent(KEY_OLD_LEVEL, this.oldLevel);
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.oldLevel, arg.oldLevel)
                    && Objects.equals(this.newLevel, arg.newLevel);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_OLD_LEVEL, this.oldLevel,
                    KEY_NEW_LEVEL, this.newLevel
            );
        }
    }
}
