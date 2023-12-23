package org.kunlab.scenamatica.action.actions.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntitySpawnAction<T extends EntitySpawnAction.Argument> extends AbstractAction<T>
        implements Executable<T>, Watchable<T>
{
    public static final String KEY_ACTION_NAME = "entity_spawn";

    public static <T extends Entity> T spawnEntity(EntityStructure structure, @Nullable LocationStructure locDef, ScenarioEngine engine)
    {
        Location spawnLoc = locDef == null ? null: locDef.create();
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
    public void execute(@NotNull ScenarioEngine engine, @NotNull T argument)
    {
        EntityStructure structure = argument.getEntity().getTargetStructure();
        assert structure != null;
        LocationStructure spawnLoc = structure.getLocation();

        spawnEntity(structure, spawnLoc, engine);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntitySpawnEvent))
            return false;

        EntitySpawnEvent e = (EntitySpawnEvent) event;

        EntitySpecifier<?> entity = argument.getEntity();
        return (!entity.canProvideTarget() || entity.checkMatchedEntity(e.getEntity()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntitySpawnEvent.class
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return (T) new Argument(
                serializer.tryDeserializeEntitySpecifier(map.get(Argument.KEY_ENTITY))
        );
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Getter
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_ENTITY = "entity";

        @NotNull
        private final EntitySpecifier<?> entity;
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
                if (!this.entity.hasStructure())
                    throw new IllegalArgumentException("Entity structure is not present");
                EntityStructure structure = this.entity.getTargetStructure();
                ensurePresent(KEY_ENTITY + "." + EntityStructure.KEY_TYPE, structure.getType());
                ensureNotEquals(KEY_ENTITY + "." + EntityStructure.KEY_TYPE, structure.getType(), EntityType.UNKNOWN);
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
