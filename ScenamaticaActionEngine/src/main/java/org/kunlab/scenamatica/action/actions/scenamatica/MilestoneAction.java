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
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MilestoneAction extends AbstractScenamaticaAction<MilestoneAction.Argument>
        implements Requireable<MilestoneAction.Argument>, Watchable<MilestoneAction.Argument>
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

        engine.getManager().getMilestoneManager().reachMilestone(engine, argument.getName());
    }

    @Override
    public boolean isFired(@NotNull MilestoneAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof MilestoneReachedEvent;

        MilestoneReachedEvent e = (MilestoneReachedEvent) event;

        boolean expectedCondition = argument.isReached();
        boolean actualCondition = !e.isCancelled();

        return expectedCondition == actualCondition;
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
        MapUtils.checkContainsKey(map, Argument.KEY_NAME);
        MapUtils.checkTypeIfContains(map, Argument.KEY_REACHED, Boolean.class);

        String name = (String) map.get(Argument.KEY_NAME);
        boolean reached = MapUtils.getOrDefault(map, Argument.KEY_REACHED, true);

        return new Argument(name, reached);
    }

    @Override
    public boolean isConditionFulfilled(@Nullable MilestoneAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        boolean isMilestoneReached = engine.getManager().getMilestoneManager().isReached(engine, argument.getName());

        return isMilestoneReached == argument.isReached();
    }

    @Value
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        private static final String KEY_NAME = "name";
        private static final String KEY_REACHED = "reached";

        @NotNull
        String name;
        boolean reached;

        public Argument(@NotNull String name)
        {
            this.name = name;
            this.reached = true;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return false;
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
