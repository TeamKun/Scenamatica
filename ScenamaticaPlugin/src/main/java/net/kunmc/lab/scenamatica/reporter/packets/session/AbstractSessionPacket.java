package net.kunmc.lab.scenamatica.reporter.packets.session;

import net.kunmc.lab.scenamatica.reporter.packets.AbstractRawPacket;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSessionPacket extends AbstractRawPacket
{
    private static final String GENRE = "session";

    public AbstractSessionPacket(@NotNull String type)
    {
        super(GENRE, type);
    }
}
