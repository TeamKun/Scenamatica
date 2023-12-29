package org.kunlab.scenamatica.reporter.packets.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.reporter.packets.test.AbstractTestPacket;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractActionPacket extends AbstractTestPacket
{
    public static final String KEY_ACTION = "action";

    private static final String GENRE = "action";

    @NotNull
    private final String actionName;

    public AbstractActionPacket(@NotNull String type, @NotNull UUID testID, @NotNull ActionResult action)

    {
        super(GENRE, type, testID);
        this.actionName = action.getActionName();
    }

    public AbstractActionPacket(@NotNull String type, @NotNull UUID testID, @NotNull CompiledAction action)

    {
        super(GENRE, type, testID);
        this.actionName = action.getExecutor().getName();
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();
        result.putAll(this.serializeAction(this.actionName));

        return result;
    }

    protected Map<String, Object> serializeAction(@NotNull String name)
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_ACTION, name);

        return result;
    }
}
