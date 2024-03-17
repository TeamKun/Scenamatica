package org.kunlab.scenamatica.nms.types;

import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.supports.WorldMap;

public interface NMSMinecraftServer extends NMSWrapped
{
    // ====================[ FIELDS ]===================
    WorldMap getWorlds();
}
