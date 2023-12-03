package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntitySpawnAction extends AbstractAction<EntitySpawnAction.Argument>
        implements Executable<EntitySpawnAction.Argument>, Watchable<EntitySpawnAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_spawn";

    public static <T extends Entity> T spawnEntity(EntityStructure structure, @Nullable Location spawnLoc, ScenarioEngine engine)
    {
        if (spawnLoc == null)
            spawnLoc = engine.getContext().getStage().getSpawnLocation();
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
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        EntityStructure structure = argument.getEntity();
        Location spawnLoc = structure.getLocation();

        spawnEntity(structure, spawnLoc, engine);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntitySpawnEvent))
            return false;

        EntitySpawnEvent e = (EntitySpawnEvent) event;
        return EntityUtils.tryCastMapped(argument.getEntity(), e.getEntity()).isAdequate(e.getEntity());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntitySpawnEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        if (!map.containsKey(Argument.KEY_ENTITY))
            return new Argument(null);

        return new Argument(
                serializer.deserialize(
                        MapUtils.checkAndCastMap(map.get(Argument.KEY_ENTITY)),
                        EntityStructure.class
                ));
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_ENTITY = "entity";

        EntityStructure entity;
        // CreatureSpawnEvent.SpawnReason reason は CreatureSpawnAction でつくる。

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.entity, arg.entity);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensurePresent(KEY_ENTITY, this.entity);
                EntityStructure structure = this.entity;
                ensurePresent(KEY_ENTITY + "." + EntityStructure.KEY_TYPE, structure.getType());
                ensureEquals(KEY_ENTITY + "." + EntityStructure.KEY_TYPE, structure.getType(), EntityType.UNKNOWN);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    KEY_ENTITY, this.entity
            );
        }
    }
}
