package net.kunmc.lab.scenamatica.action.actions.block;

import lombok.Getter;
import net.kunmc.lab.scenamatica.action.actions.AbstractActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockActionArgument extends AbstractActionArgument
{
    public static final String KEY_BLOCK = "block";

    @Getter
    @NotNull
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
