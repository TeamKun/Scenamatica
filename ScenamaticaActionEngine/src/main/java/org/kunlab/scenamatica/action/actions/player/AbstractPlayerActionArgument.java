package org.kunlab.scenamatica.action.actions.player;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractPlayerActionArgument extends AbstractActionArgument
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

    public String getTargetSpecifier()
    {
        return this.target;
    }

    public Player getTarget()
    {
        return PlayerUtils.getPlayerOrThrow(this.target);
    }

    @Override
    public String getArgumentString()
    {
        return buildArgumentString(
                KEY_TARGET_PLAYER, this.target
        );
    }
}
