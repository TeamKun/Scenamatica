package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;

public class EntityAction extends AbstractEntityAction<EntityAction.Argument> implements Requireable<EntityAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Entity target = argument.getTarget();
        EntityBean entityInfo = argument.getEntity();

        BeanUtils.applyEntityBeanData(entityInfo, target);
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Entity target = argument.getTarget();
        EntityBean entityInfo = argument.getEntity();

        return BeanUtils.isSame(entityInfo, target, false);
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        if (map.containsKey(Argument.KEY_ENTITY))
            throw new IllegalArgumentException("Argument map contains invalid key: " + Argument.KEY_ENTITY);

        return new Argument(serializer.deserializeEntity(
                MapUtils.checkAndCastMap(
                        map.get(Argument.KEY_ENTITY),
                        String.class,
                        Object.class
                )
        ));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument
    {
        private static final String KEY_ENTITY = "entity";

        @NotNull
        EntityBean entity;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return this.entity.equals(arg.entity);
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
