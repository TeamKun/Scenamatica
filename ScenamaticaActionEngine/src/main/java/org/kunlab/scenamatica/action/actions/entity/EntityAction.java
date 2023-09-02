package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;
import java.util.Objects;

public class EntityAction extends AbstractEntityAction<EntityAction.Argument>
        implements Executable<EntityAction.Argument>, Requireable<EntityAction.Argument>
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

        Entity target = argument.selectTarget();
        EntityBean entityInfo = argument.getEntity();

        assert entityInfo != null;

        BeanUtils.applyEntityBeanData(entityInfo, target);
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        EntityBean entityInfo = argument.getEntity();
        Entity target = EntityUtils.getPlayerOrEntityOrNull(argument.getTargetString());

        if (target == null && entityInfo != null)
            throw new IllegalStateException("Cannot find entity with identifier " + argument.getTargetString());
        else if (target == null)
            return false;

        return entityInfo == null
                || BeanUtils.isSame(entityInfo, target, false);
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        EntityBean bean;
        if (map.containsKey(Argument.KEY_ENTITY))
            bean = serializer.deserializeEntity(
                    MapUtils.checkAndCastMap(
                            map.get(Argument.KEY_ENTITY),
                            String.class,
                            Object.class
                    )
            );
        else
            bean = null;

        return new Argument(
                super.deserializeTarget(map, serializer),
                bean
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument
    {
        private static final String KEY_ENTITY = "entity";

        EntityBean entity;

        public Argument(Object target, @Nullable EntityBean entity)
        {
            super(target);
            this.entity = entity;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!super.isSame(argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.entity, arg.entity);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (!this.isSelectable())
                throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid selector.");

            if (type == ScenarioType.ACTION_EXECUTE && this.entity == null)
                throw new IllegalArgumentException("Cannot execute action without entity argument.");
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ENTITY, this.entity
            );
        }
    }
}
