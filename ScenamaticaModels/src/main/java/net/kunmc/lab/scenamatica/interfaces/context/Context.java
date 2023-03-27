package net.kunmc.lab.scenamatica.interfaces.context;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public interface Context
{
    World getStage();

    List<? extends Player> getActors();
}
