package net.kunmc.lab.scenamatica.action.actions.entity;

import lombok.*;
import net.kunmc.lab.scenamatica.action.utils.*;
import net.kunmc.lab.scenamatica.interfaces.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.*;

@AllArgsConstructor
public abstract class AbstractEntityActionArgument implements ActionArgument
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

    @Override
    public String getArgumentString()
    {
        return "target=" + this.target;
    }
}
