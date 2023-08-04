package org.kunlab.scenamatica.action.actions.block;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

@Getter
public abstract class AbstractBlockActionArgument extends AbstractActionArgument
{
    public static final String KEY_BLOCK = "block";

    @NotNull  // TODO: Make this Nullable
    BlockBean block;

    public AbstractBlockActionArgument(@NotNull BlockBean block)
    {
        this.block = block;
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof AbstractBlockActionArgument))
            return false;

        AbstractBlockActionArgument arg = (AbstractBlockActionArgument) argument;

        return arg.block.equals(this.block);
    }

    @Override
    public String getArgumentString()
    {
        return buildArgumentString(
                KEY_BLOCK, this.block
        );
    }
}
