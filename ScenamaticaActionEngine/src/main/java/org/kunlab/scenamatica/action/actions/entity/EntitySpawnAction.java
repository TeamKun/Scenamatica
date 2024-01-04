package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

public class EntitySpawnAction<E extends Entity> extends AbstractAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_spawn";
    public static final String KEY_OUT_ENTITY = "entity";
    public final InputToken<EntitySpecifier<E>> IN_ENTITY;

    private final Class<E> entityClass;
    private final Class<? extends EntityStructure> structureClass;

    public EntitySpawnAction(Class<E> entityclass, Class<? extends EntityStructure> structureClass)
    {
        this.entityClass = entityclass;
        this.structureClass = structureClass;
        this.IN_ENTITY = ofInput("entity", entityclass, structureClass)
                .validator(ScenarioType.ACTION_EXECUTE, EntitySpecifier::hasStructure, "Entity structure is not present")
                .validator(ScenarioType.ACTION_EXECUTE, specifier -> specifier.getTargetStructure().getType() != null, "Entity type is not present")
                .validator(ScenarioType.ACTION_EXECUTE, specifier -> specifier.getTargetStructure().getType() != EntityType.UNKNOWN, "Entity type is unknown");
    }

    public <T extends Entity> T spawnEntity(ActionContext ctxt, EntityStructure structure, @Nullable LocationStructure locDef)
    {
        Location spawnLoc = locDef == null ? null: locDef.create();
        if (spawnLoc == null)
            spawnLoc = ctxt.getContext().getStage().getWorld().getSpawnLocation();
        // World が指定されていない場合は、ステージのワールドを使う。
        spawnLoc = Utils.assignWorldToLocation(spawnLoc, ctxt.getEngine());

        assert structure.getType() != null;
        // noinspection unchecked
        Class<T> entityClass = (Class<T>) structure.getType().getEntityClass();
        assert entityClass != null; // null は EntityType.UNKNOWN なときなだけなのであり得ない。

        return spawnLoc.getWorld().spawn(
                spawnLoc,
                entityClass,
                entity -> {
                    EntityUtils.tryCastMapped(structure, entity).applyTo(entity);
                    this.makeOutputs(ctxt, entity);
                }
        );
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        EntityStructure structure = ctxt.input(this.IN_ENTITY).getTargetStructure();
        assert structure != null;
        LocationStructure spawnLoc = structure.getLocation();

        this.spawnEntity(ctxt, structure, spawnLoc);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity)
    {
        ctxt.output(KEY_OUT_ENTITY, entity);
        ctxt.commitOutput();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof EntitySpawnEvent))
            return false;

        EntitySpawnEvent e = (EntitySpawnEvent) event;

        boolean result = ctxt.ifHasInput(this.IN_ENTITY, specifier -> specifier.checkMatchedEntity(e.getEntity()));
        if (result)
            this.makeOutputs(ctxt, e.getEntity());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntitySpawnEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, this.IN_ENTITY);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(this.IN_ENTITY);

        return board;
    }
}
