package org.kunlab.scenamatica.context.stage;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface StageWorldDestroyer
{
    void destroyWorld(@NotNull World world);
}
