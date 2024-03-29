package org.kunlab.scenamatica.action.actions.extend_v1_16_5.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.entity.AbstractGeneralEntityAction;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

@ActionMeta(value = "entity_move", supportsSince = MinecraftVersion.V1_16_5)
public class EntityMoveAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
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
    public static final String OUT_KEY_FROM = "from";
    public static final String OUT_KEY_TO = "to";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Location toLoc = Utils.assignWorldToLocation(ctxt.input(IN_TO), ctxt.getEngine());
        Entity entity = this.selectTarget(ctxt);

        this.makeOutputs(ctxt, entity, entity.getLocation(), toLoc);
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

        boolean result = ctxt.ifHasInput(IN_FROM, from -> from.isAdequate(e.getFrom()))
                && ctxt.ifHasInput(IN_TO, to -> to.isAdequate(e.getTo()));
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getFrom(), e.getTo());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @NotNull Location from, @NotNull Location to)
    {
        ctxt.output(OUT_KEY_FROM, from);
        ctxt.output(OUT_KEY_TO, to);
        super.makeOutputs(ctxt, entity);
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
