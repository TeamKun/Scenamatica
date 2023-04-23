package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.reporter.packets.test.AbstractTestPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractActionPacket extends AbstractTestPacket
{
    private static final String KEY_ACTION = "action";
    private static final String KEY_ARGUMENT = "argument";

    private static final String GENRE = "action";

    @NotNull
    private final CompiledAction<?> action;

    public AbstractActionPacket(@NotNull String type, @NotNull UUID testID, @NotNull CompiledAction<?> action)

    {
        super(GENRE, type, testID);
        this.action = action;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();
        result.putAll(this.serializeAction(this.action));

        return result;
    }

    protected Map<String, Object> serializeAction(@NotNull CompiledAction<?> action)
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_ACTION, action.getBean());
        result.put(KEY_ARGUMENT, action.getBean().getArguments());

        return result;
    }
}
