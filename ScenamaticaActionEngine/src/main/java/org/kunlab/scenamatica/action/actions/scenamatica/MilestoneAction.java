package org.kunlab.scenamatica.action.actions.scenamatica;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.events.MilestoneReachedEvent;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.MilestoneEntry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MilestoneAction extends AbstractScenamaticaAction<MilestoneAction.Argument>
        implements Executable<MilestoneAction.Argument>, Watchable<MilestoneAction.Argument>, Requireable<MilestoneAction.Argument>
{
    public static final String KEY_ACTION_NAME = "milestone";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable MilestoneAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (argument.getReached() == null || argument.getReached())
            engine.getManager().getMilestoneManager().reachMilestone(engine, argument.getName());
        else
            engine.getManager().getMilestoneManager().revokeMilestone(engine, argument.getName());
    }

    @Override
    public boolean isFired(@NotNull MilestoneAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof MilestoneReachedEvent;
        MilestoneReachedEvent e = (MilestoneReachedEvent) event;

        Boolean expectedCondition = argument.getReached();
        boolean condition = !e.isCancelled();

        String expectedMilestoneName = argument.getName();
        MilestoneEntry milestone = e.getMilestone();

        return (expectedMilestoneName == null || expectedMilestoneName.equals(milestone.getName()))
                && (expectedCondition == null || expectedCondition == condition);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                MilestoneReachedEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                MapUtils.getOrNull(map, Argument.KEY_NAME),
                MapUtils.getOrNull(map, Argument.KEY_REACHED)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable MilestoneAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        boolean isMilestoneReached = engine.getManager().getMilestoneManager().isReached(engine, argument.getName());
        return isMilestoneReached == (argument.getReached() == null || argument.getReached());
    }

    @Value
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        private static final String KEY_NAME = "name";
        private static final String KEY_REACHED = "reached";

        String name;
        Boolean reached;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.name, arg.name)
                    && Objects.equals(this.reached, arg.reached);
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_NAME, this.name,
                    KEY_REACHED, this.reached
            );
        }
    }
}
