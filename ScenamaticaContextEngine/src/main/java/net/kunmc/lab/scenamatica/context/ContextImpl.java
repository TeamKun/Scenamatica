package net.kunmc.lab.scenamatica.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import org.bukkit.World;

import java.util.List;

/**
 * コンテキスト情報を保持するクラスです。
 */
@Value
public class ContextImpl implements Context
{
    /**
     * ステージです。
     */
    World stage;
    /**
     * アクターです。
     */
    List<Actor> actors;
}
