package org.kunlab.scenamatica.interfaces.context;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * コンテキスト情報を保持するクラスです。
 */
public interface Context
{
    /**
     * ステージです。
     */
    World getStage();

    /**
     * アクターです。
     */
    @Nullable
    List<? extends Actor> getActors();

    /**
     * エンティティです。
     */
    @Nullable
    List<? extends Entity> getEntities();
}
