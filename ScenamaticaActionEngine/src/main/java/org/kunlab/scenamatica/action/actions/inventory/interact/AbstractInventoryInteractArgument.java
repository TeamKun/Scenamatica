package org.kunlab.scenamatica.action.actions.inventory.interact;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@Getter
public abstract class AbstractInventoryInteractArgument extends AbstractInventoryArgument
{
    public static final String KEY_TARGET_PLAYER = "target";

    @NotNull
    private final PlayerSpecifier targetSpecifier;

    public AbstractInventoryInteractArgument(@Nullable InventoryStructure inventory, @NotNull PlayerSpecifier targetSpecifier)
    {
        super(inventory);
        this.targetSpecifier = targetSpecifier;
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!super.isSame(argument))
            return false;

        if (!AbstractInventoryInteractArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractInventoryInteractArgument a = (AbstractInventoryInteractArgument) argument;
        return Objects.equals(a.targetSpecifier, this.targetSpecifier);
    }

    @Override
    public String getArgumentString()
    {
        return buildArgumentString(
                super.getArgumentString(),
                KEY_TARGET_PLAYER, this.targetSpecifier
        );
    }
}
