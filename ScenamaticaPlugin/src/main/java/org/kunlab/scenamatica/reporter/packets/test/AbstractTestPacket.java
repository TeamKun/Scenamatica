package org.kunlab.scenamatica.reporter.packets.test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.reporter.packets.AbstractRawPacket;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractTestPacket extends AbstractRawPacket
{
    private static final String KEY_TEST_ID = "testID";

    private static final String GENRE = "test";

    @Nullable
    private final UUID testID;

    public AbstractTestPacket(@NotNull String type, @Nullable UUID testID)
    {
        this(GENRE, type, testID);
    }

    public AbstractTestPacket(@NotNull String genre, @NotNull String type, @Nullable UUID testID)
    {
        super(genre, type);
        this.testID = testID;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        if (this.testID == null)
            result.put(KEY_TEST_ID, "<unassigned>");
        else
            result.put(KEY_TEST_ID, this.testID.toString());

        return result;
    }
}
