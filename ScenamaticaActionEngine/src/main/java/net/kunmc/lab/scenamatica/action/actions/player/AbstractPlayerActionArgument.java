package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
    public String toString()
    {
        return "target=" + this.target;
    }
}
