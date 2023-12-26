package org.kunlab.scenamatica.action.actions.scenamatica;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.MilestoneReachedEvent;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.MilestoneEntry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

public class MilestoneAction extends AbstractScenamaticaAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "milestone";
    public static final InputToken<String> IN_NAME = ofInput(
            "name",
            String.class
    );
    public static final InputToken<Boolean> IN_REACHED = ofInput(
            "reached",
            Boolean.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        String name = argument.get(IN_NAME);
        if (argument.ifPresent(IN_REACHED, reached -> reached))
            engine.getManager().getMilestoneManager().reachMilestone(engine, name);
        else
            engine.getManager().getMilestoneManager().revokeMilestone(engine, name);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof MilestoneReachedEvent;
        MilestoneReachedEvent e = (MilestoneReachedEvent) event;

        boolean condition = !e.isCancelled();
        MilestoneEntry milestone = e.getMilestone();

        return argument.ifPresent(IN_NAME, name -> name.equalsIgnoreCase(milestone.getName()))
                && argument.ifPresent(IN_REACHED, reached -> reached == condition);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                MilestoneReachedEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        boolean isMilestoneReached = engine.getManager().getMilestoneManager().isReached(engine, argument.get(IN_NAME));
        boolean expected = argument.orElse(IN_REACHED, () -> true);
        return isMilestoneReached == expected;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_NAME, IN_REACHED);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_NAME);

        return board;
    }
}
