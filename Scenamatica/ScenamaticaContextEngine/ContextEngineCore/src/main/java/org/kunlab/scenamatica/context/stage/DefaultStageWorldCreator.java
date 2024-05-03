package org.kunlab.scenamatica.context.stage;

import org.bukkit.World;
import org.bukkit.WorldCreator;

public class DefaultStageWorldCreator implements StageWorldCreator
{
    @Override
    public World createWorld(WorldCreator creator)
    {
        return creator.createWorld();
    }
}
