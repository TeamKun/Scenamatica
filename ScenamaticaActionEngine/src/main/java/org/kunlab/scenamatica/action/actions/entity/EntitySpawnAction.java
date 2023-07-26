package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.Utils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntitySpawnAction extends AbstractAction<EntitySpawnAction.Argument>
        implements Watchable<EntitySpawnAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_spawn";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        EntityBean bean = argument.getEntity();
        Location spawnLoc = bean.getLocation();
        if (spawnLoc == null)
            spawnLoc = engine.getContext().getStage().getSpawnLocation();

        // World が指定されていない場合は、ステージのワールドを使う。
        spawnLoc = Utils.assignWorldToLocation(spawnLoc, engine);

        assert bean.getType() != null;
        assert bean.getType().getEntityClass() != null;  // null は EntityType.UNKNOWN なときなだけなのであり得ない。
        spawnLoc.getWorld().spawn(
                spawnLoc,
                bean.getType().getEntityClass(),
                e -> BeanUtils.applyEntityBeanData(bean, e)
        );
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntitySpawnEvent))
            return false;

        EntitySpawnEvent e = (EntitySpawnEvent) event;
        return BeanUtils.isSame(argument.getEntity(), e.getEntity(), false);
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (type == ScenarioType.ACTION_EXECUTE)
        {
            if (argument.getEntity() == null)
                throw new IllegalArgumentException("Entity cannot be null");

            EntityBean bean = argument.getEntity();
            if (bean.getType() == null)
                throw new IllegalArgumentException("Entity type cannot be null");
            else if (bean.getType() == EntityType.UNKNOWN)
                throw new IllegalArgumentException("Entity type cannot be UNKNOWN");
        }
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntitySpawnEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        if (!map.containsKey(Argument.KEY_ENTITY))
            return new Argument(null);

        return new Argument(
                serializer.deserializeEntity(
                        MapUtils.checkAndCastMap(
                                map.get(Argument.KEY_ENTITY),
                                String.class,
                                Object.class
                        )
                ));
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_ENTITY = "entity";

        EntityBean entity;
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
        public String getArgumentString()
        {
            return appendArgumentString(
                    KEY_ENTITY, this.entity
            );
        }
    }
}
