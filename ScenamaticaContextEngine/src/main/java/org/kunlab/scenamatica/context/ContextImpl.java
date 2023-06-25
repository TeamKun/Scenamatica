package org.kunlab.scenamatica.context;

import lombok.Value;
import org.bukkit.World;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.Context;

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
