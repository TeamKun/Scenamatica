package org.kunlab.scenamatica.action.actions.entity;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

@AllArgsConstructor
public abstract class AbstractEntityActionArgument extends AbstractActionArgument
{
    public static final String KEY_TARGET_ENTITY = "target";

    @NotNull
    private final String target;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractEntityActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractEntityActionArgument a = (AbstractEntityActionArgument) argument;
        return this.isSameTarget(a);
    }

    protected boolean isSameTarget(AbstractEntityActionArgument argument)
    {
        return this.target.equalsIgnoreCase(argument.target);
    }

    public Entity getTarget()
    {
        return EntityUtils.getPlayerOrEntityOrThrow(this.target);
    }

    public String getTargetString()
    {
        return this.target;
    }

    @Override
    public String getArgumentString()
    {
        return "target=" + this.target;
    }
}
