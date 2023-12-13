package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
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

        Entity target = argument.selectTarget(engine.getContext());
        EntityStructure entityInfo = argument.getEntity();

        if (!(entityInfo instanceof Mapped<?>))
            throw new IllegalStateException("Cannot check matched entity for non-mapped entity");

        // noinspection rawtypes
        Mapped mapped = (Mapped) entityInfo;

        if (!mapped.canApplyTo(target))
            throw new IllegalStateException("Cannot apply entity info of " + entityInfo + " to " + target);


        // noinspection unchecked  // checked above
        mapped.applyTo(target);
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        EntityStructure entityInfo = argument.getEntity();
        Entity target = EntityUtils.getPlayerOrEntityOrNull(argument.getTargetString());

        if (target == null && entityInfo != null)
            throw new IllegalStateException("Cannot find entity with identifier " + argument.getTargetString());
        else if (target == null)
            return false;

        return entityInfo == null || EntityUtils.tryCastMapped(entityInfo, target).isAdequate(target);
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        EntityStructure structure;
        if (map.containsKey(Argument.KEY_ENTITY))
            structure = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_ENTITY)),
                    EntityStructure.class
            );
        else
            structure = null;

        return new Argument(
                super.deserializeTarget(map, serializer),
                structure
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Entity>
    {
        public static final String KEY_ENTITY = "entity";

        EntityStructure entity;

        public Argument(EntitySpecifier<Entity> target, @Nullable EntityStructure entity)
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
            this.ensureCanProvideTarget();
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
