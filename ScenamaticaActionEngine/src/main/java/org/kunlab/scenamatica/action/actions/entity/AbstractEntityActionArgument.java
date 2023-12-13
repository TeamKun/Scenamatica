package org.kunlab.scenamatica.action.actions.entity;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractEntityActionArgument<E extends Entity> extends AbstractActionArgument
{
    public static final String KEY_TARGET_ENTITY = "target";

    @Nullable
    private final EntitySpecifier<E> entity;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractEntityActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        return Objects.equals(this.entity, ((AbstractEntityActionArgument<?>) argument).entity);
    }

    private void ensureTargetIsSet()
    {
        if (this.entity == null)
            throw new IllegalStateException("Target is not set.");
    }

    public Entity selectTarget(Context context)
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.selectTarget(context);
    }

    public String getTargetString()
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.getSelectorString();
    }

    @Override
    public String getArgumentString()
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.getArgumentString();
    }

    public EntitySpecifier<E> getTargetHolder()
    {
        return this.entity;
    }

    public EntityStructure getTargetStructure()
    {
        return this.entity == null ? null: this.entity.getTargetStructure();
    }

    public boolean checkMatchedEntity(Entity entity)
    {
        if (this.entity == null)
            return true;
        return this.entity.checkMatchedEntity(entity);
    }

    public void ensureCanProvideTarget()
    {
        if (this.entity == null || !this.entity.canProvideTarget())
            throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid specifier.");
    }
}
