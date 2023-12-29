package org.kunlab.scenamatica.action.actions.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

public class EntityMoveAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_move";
    public static final InputToken<LocationStructure> IN_FROM = ofInput(
            "from",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final InputToken<LocationStructure> IN_TO = ofInput(
            "to",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final InputToken<Boolean> IN_USE_AI = ofInput(
            "ai",
            Boolean.class,
            true
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Location toLoc = Utils.assignWorldToLocation(ctxt.input(IN_TO), ctxt.getEngine());
        Entity entity = this.selectTarget(ctxt);

        if (ctxt.input(IN_USE_AI) && entity instanceof Mob)
        {
            Mob mob = (Mob) entity;
            boolean success = mob.getPathfinder().moveTo(toLoc);
            if (!success)
                throw new IllegalStateException("Failed to find path from " + entity.getLocation() + " to " + toLoc);
        }
        else
            entity.teleport(toLoc);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        assert event instanceof EntityMoveEvent;
        EntityMoveEvent e = (EntityMoveEvent) event;

        return ctxt.ifHasInput(IN_FROM, from -> from.isAdequate(e.getFrom()))
                && ctxt.ifHasInput(IN_TO, to -> to.isAdequate(e.getTo()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityMoveEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_FROM, IN_TO);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.register(IN_USE_AI)
                    .requirePresent(IN_TO)
                    .validator(b -> !b.isPresent(IN_FROM), "Cannot specify from in execute action.");
        return board;
    }
}
