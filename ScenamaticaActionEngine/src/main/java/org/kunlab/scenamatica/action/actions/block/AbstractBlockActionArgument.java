package org.kunlab.scenamatica.action.actions.block;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@Getter
public abstract class AbstractBlockActionArgument extends AbstractActionArgument
{
    public static final String KEY_BLOCK = "block";

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

        return Objects.equals(this.block, arg.block);
    }

    @Override
    public String getArgumentString()
    {
        return buildArgumentString(
                KEY_BLOCK, this.block
        );
    }
}
