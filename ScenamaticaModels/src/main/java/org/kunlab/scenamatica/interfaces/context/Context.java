package org.kunlab.scenamatica.interfaces.context;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    List<? extends Actor> getActors();

    /**
     * エンティティです。
     */
    @NotNull
    List<? extends Entity> getEntities();
}
