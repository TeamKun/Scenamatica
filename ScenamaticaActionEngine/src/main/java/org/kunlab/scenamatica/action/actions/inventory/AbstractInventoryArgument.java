package org.kunlab.scenamatica.action.actions.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@Getter
@AllArgsConstructor
public abstract class AbstractInventoryArgument extends AbstractActionArgument
{
    public static final String KEY_INVENTORY = "inventory";

    @Nullable
    private final InventoryBean inventory;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractInventoryArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractInventoryArgument a = (AbstractInventoryArgument) argument;
        return Objects.equals(this.inventory, a.inventory);
    }

    @Override
    public String getArgumentString()
    {
        return "inventory=" + this.inventory;
    }
}
