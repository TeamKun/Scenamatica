package org.kunlab.scenamatica.reporter.packets.test;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.reporter.packets.AbstractRawPacket;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractTestPacket extends AbstractRawPacket
{
    private static final String KEY_TEST_ID = "testID";

    private static final String GENRE = "test";

    @NotNull
    private final UUID testID;

    public AbstractTestPacket(@NotNull String type, @NotNull UUID testID)
    {
        this(GENRE, type, testID);
    }

    public AbstractTestPacket(@NotNull String genre, @NotNull String type, @NotNull UUID testID)
    {
        super(genre, type);
        this.testID = testID;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_TEST_ID, this.testID);

        return result;
    }
}
