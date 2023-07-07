package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryArgument;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

public abstract class AbstractInventoryInteractArgument extends AbstractInventoryArgument
{
    public static final String KEY_TARGET_PLAYER = "target";

    @NotNull
    private final String target;

    public AbstractInventoryInteractArgument(@NotNull InventoryBean inventory, @NotNull String target)
    {
        super(inventory);
        this.target = target;
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!super.isSame(argument))
            return false;

        if (!AbstractInventoryInteractArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractInventoryInteractArgument a = (AbstractInventoryInteractArgument) argument;
        return Objects.equals(a.target, this.target);
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
                super.getArgumentString(),
                KEY_TARGET_PLAYER, this.target
        );
    }
}
