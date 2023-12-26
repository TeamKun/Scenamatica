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
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

public class EntitySpawnAction<E extends Entity> extends AbstractAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_spawn";
    public final InputToken<EntitySpecifier<E>> IN_ENTITY;

    public EntitySpawnAction(Class<E> entityclass, Class<? extends EntityStructure> structureClass)
    {
        this.IN_ENTITY = ofInput("entity", entityclass, structureClass)
                .validator(ScenarioType.ACTION_EXECUTE, EntitySpecifier::hasStructure, "Entity structure is not present")
                .validator(ScenarioType.ACTION_EXECUTE, specifier -> specifier.getTargetStructure().getType() != null, "Entity type is not present")
                .validator(ScenarioType.ACTION_EXECUTE, specifier -> specifier.getTargetStructure().getType() != EntityType.UNKNOWN, "Entity type is unknown");
    }

    public static <T extends Entity> T spawnEntity(EntityStructure structure, @Nullable LocationStructure locDef, ScenarioEngine engine)
    {
        Location spawnLoc = locDef == null ? null: locDef.create();
        if (spawnLoc == null)
            spawnLoc = engine.getContext().getStage().getWorld().getSpawnLocation();
        // World が指定されていない場合は、ステージのワールドを使う。
        spawnLoc = Utils.assignWorldToLocation(spawnLoc, engine);

        assert structure.getType() != null;
        // noinspection unchecked
        Class<T> entityClass = (Class<T>) structure.getType().getEntityClass();
        assert entityClass != null; // null は EntityType.UNKNOWN なときなだけなのであり得ない。

        return spawnLoc.getWorld().spawn(
                spawnLoc,
                entityClass,
                entity -> EntityUtils.tryCastMapped(structure, entity).applyTo(entity)
        );
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        EntityStructure structure = argument.get(this.IN_ENTITY).getTargetStructure();
        assert structure != null;
        LocationStructure spawnLoc = structure.getLocation();

        spawnEntity(structure, spawnLoc, engine);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntitySpawnEvent))
            return false;

        EntitySpawnEvent e = (EntitySpawnEvent) event;

        return argument.ifPresent(this.IN_ENTITY, specifier -> specifier.checkMatchedEntity(e.getEntity()));
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
