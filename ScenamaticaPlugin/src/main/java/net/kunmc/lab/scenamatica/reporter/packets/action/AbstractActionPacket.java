package net.kunmc.lab.scenamatica.reporter.packets.action;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.reporter.packets.test.AbstractTestPacket;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractActionPacket extends AbstractTestPacket
{
    private static final String KEY_ACTION = "action";

    private static final String GENRE = "action";

    @NotNull
    private final ActionBean action;

    public AbstractActionPacket(@NotNull String type, @NotNull UUID testID, @NotNull ActionBean action)

    {
        super(GENRE, type, testID);
        this.action = action;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_ACTION, ActionBeanImpl.serialize(this.action));

        return result;
    }
}
