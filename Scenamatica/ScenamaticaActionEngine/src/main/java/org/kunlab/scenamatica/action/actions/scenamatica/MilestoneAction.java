package org.kunlab.scenamatica.action.actions.scenamatica;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.MilestoneReachedEvent;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.MilestoneEntry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

@Action("milestone")
public class MilestoneAction extends AbstractScenamaticaAction
        implements Executable, Watchable, Requireable
{
    public static final InputToken<String> IN_NAME = ofInput(
            "name",
            String.class
    );
    public static final InputToken<Boolean> IN_REACHED = ofInput(
            "reached",
            Boolean.class
    );
    public static final String KEY_OUT_NAME = "name";
    public static final String KEY_OUT_REACHED = "reached";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        ScenarioEngine engine = ctxt.getEngine();
        String name = ctxt.input(IN_NAME);
        boolean reached = ctxt.ifHasInput(IN_REACHED, r -> r);

        this.makeOutputs(ctxt, name, reached);
        if (reached)
            engine.getManager().getMilestoneManager().reachMilestone(engine, name);
        else
            engine.getManager().getMilestoneManager().revokeMilestone(engine, name);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        assert event instanceof MilestoneReachedEvent;
        MilestoneReachedEvent e = (MilestoneReachedEvent) event;

        boolean condition = !e.isCancelled();
        MilestoneEntry milestone = e.getMilestone();

        boolean result = ctxt.ifHasInput(IN_NAME, name -> name.equalsIgnoreCase(milestone.getName()))
                && ctxt.ifHasInput(IN_REACHED, reached -> reached == condition);
        if (result)
            this.makeOutputs(ctxt, milestone.getName(), condition);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull String milestoneName, boolean reached)
    {
        ctxt.output(KEY_OUT_NAME, milestoneName);
        ctxt.output(KEY_OUT_REACHED, reached);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                MilestoneReachedEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        boolean isMilestoneReached = ctxt.getEngine().getManager().getMilestoneManager().isReached(ctxt.getEngine(), ctxt.input(IN_NAME));
        boolean expected = ctxt.orElseInput(IN_REACHED, () -> true);
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
