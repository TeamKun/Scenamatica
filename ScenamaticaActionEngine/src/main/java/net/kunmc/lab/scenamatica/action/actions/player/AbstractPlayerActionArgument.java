package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.*;
import net.kunmc.lab.scenamatica.action.utils.*;
import net.kunmc.lab.scenamatica.interfaces.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.*;

import java.util.*;

@AllArgsConstructor
public abstract class AbstractPlayerActionArgument implements ActionArgument
{
    public static final String KEY_TARGET_PLAYER = "target";

    @NotNull
    private final String target;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractPlayerActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractPlayerActionArgument a = (AbstractPlayerActionArgument) argument;
        return Objects.equals(this.target, a.target);
    }

    protected boolean isSameTarget(AbstractPlayerActionArgument argument)
    {
        return this.target.equalsIgnoreCase(argument.target);
    }

    public Player getTarget()
    {
        return PlayerUtils.getPlayerOrThrow(this.target);
    }

    @Override
    public String getArgumentString()
    {
        return "target=" + this.target;
    }
}
