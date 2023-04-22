package net.kunmc.lab.scenamatica.action.actions.scenamatica;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.events.MilestoneReachedEvent;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MilestoneAction extends AbstractAction<MilestoneAction.MilestoneActionArgument> implements Requireable<MilestoneAction.MilestoneActionArgument>
{
    public static final String KEY_ACTION_NAME = "milestone";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable MilestoneActionArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        engine.getManager().getMilestoneManager().reachMilestone(engine, argument.getName());
    }

    @Override
    public boolean isFired(@NotNull MilestoneActionArgument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
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
    public MilestoneActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, MilestoneActionArgument.KEY_NAME);
        MapUtils.checkTypeIfContains(map, MilestoneActionArgument.KEY_REACHED, Boolean.class);

        String name = (String) map.get(MilestoneActionArgument.KEY_NAME);
        boolean reached = MapUtils.getOrDefault(map, MilestoneActionArgument.KEY_REACHED, true);

        return new MilestoneActionArgument(name, reached);
    }

    @Override
    public boolean isConditionFulfilled(@Nullable MilestoneActionArgument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        boolean isMilestoneReached = engine.getManager().getMilestoneManager().isReached(engine, argument.getName());

        return isMilestoneReached == argument.isReached();
    }

    @Value
    @AllArgsConstructor
    public static class MilestoneActionArgument implements ActionArgument
    {
        private static final String KEY_NAME = "name";
        private static final String KEY_REACHED = "reached";

        @NotNull
        String name;
        boolean reached;

        public MilestoneActionArgument(@NotNull String name)
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
            return "milestone=" + this.name + ", reached=" + this.reached;
        }
    }
}
