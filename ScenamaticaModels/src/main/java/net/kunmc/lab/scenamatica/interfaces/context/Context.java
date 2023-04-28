package net.kunmc.lab.scenamatica.interfaces.context;

import org.bukkit.World;

import java.util.List;

public interface Context
{
    World getStage();

    List<? extends Actor> getActors();
}
