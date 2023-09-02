package org.kunlab.scenamatica.action.actions.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractEntityActionArgument extends AbstractActionArgument
{
    public static final String KEY_TARGET_ENTITY = "target";

    @Nullable
    protected final String targetSpecifier;
    @Getter
    @Nullable
    protected final EntityBean targetBean;

    public AbstractEntityActionArgument(@Nullable Object mayTarget)
    {
        if (mayTarget instanceof EntityBean)
        {
            this.targetSpecifier = null;
            this.targetBean = (EntityBean) mayTarget;
        }
        else
        {
            this.targetSpecifier = (String) mayTarget;
            this.targetBean = null;
        }
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractEntityActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractEntityActionArgument a = (AbstractEntityActionArgument) argument;
        return Objects.equals(this.targetSpecifier, a.targetSpecifier)
                || Objects.equals(this.targetBean, a.targetBean);
    }

    public boolean isSelectable()
    {
        return this.targetSpecifier != null;
    }

    public Entity selectTarget()
    {
        if (this.targetSpecifier == null)
            throw new IllegalStateException("Cannot select target from targetBean");

        return EntityUtils.getPlayerOrEntityOrThrow(this.targetSpecifier);
    }

    public String getTargetString()
    {
        return this.targetSpecifier;
    }

    public Object getTargetRaw()
    {
        if (this.targetSpecifier != null)
            return this.targetSpecifier;
        else
            return this.targetBean;
    }

    @Override
    public String getArgumentString()
    {
        if (this.targetSpecifier != null)
            return "target=" + this.targetSpecifier;
        else
            return "target=" + this.targetBean;

    }
}
