package org.kunlab.scenamatica.context.stage;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;

import java.io.IOException;

public interface StageWorldCreator
{
    World createWorld(WorldCreator creator) throws StageCreateFailedException;
}
