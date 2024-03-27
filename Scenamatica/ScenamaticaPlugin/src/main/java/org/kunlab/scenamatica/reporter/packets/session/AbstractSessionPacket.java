package org.kunlab.scenamatica.reporter.packets.session;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.reporter.packets.AbstractRawPacket;

public abstract class AbstractSessionPacket extends AbstractRawPacket
{
    private static final String GENRE = "session";

    public AbstractSessionPacket(@NotNull String type)
    {
        super(GENRE, type);
    }
}
