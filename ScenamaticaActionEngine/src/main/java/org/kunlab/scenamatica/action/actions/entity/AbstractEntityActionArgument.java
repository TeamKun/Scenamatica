package org.kunlab.scenamatica.action.actions.entity;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractEntityActionArgument extends AbstractActionArgument
{
    public static final String KEY_TARGET_ENTITY = "target";

    @Nullable
    private final EntityArgumentHolder entity;

    public AbstractEntityActionArgument(@Nullable EntityArgumentHolder target)
    {
        this.entity = target;
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractEntityActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        return Objects.equals(this.entity, ((AbstractEntityActionArgument) argument).entity);
    }

    private void ensureTargetIsSet()
    {
        if (this.entity == null)
            throw new IllegalStateException("Target is not set.");
    }

    public boolean isSelectable()
    {
        return this.entity != null && this.entity.isSelectable();
    }

    public Entity selectTarget()
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.selectTarget();
    }

    public String getTargetString()
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.getTargetString();
    }

    public Object getTargetRaw()
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.getTargetRaw();
    }

    @Override
    public String getArgumentString()
    {
        this.ensureTargetIsSet();
        assert this.entity != null;
        return this.entity.getArgumentString();
    }

    public EntityBean getTargetBean()
    {
        return this.entity == null ? null: this.entity.getTargetBean();
    }

    public boolean checkMatchedEntity(Entity entity)
    {
        if (this.entity == null)
            return true;
        return this.entity.checkMatchedEntity(entity);
    }

    public void throwIfNotSelectable()
    {
        if (!this.isSelectable())
            throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid selector.");

    }
}
