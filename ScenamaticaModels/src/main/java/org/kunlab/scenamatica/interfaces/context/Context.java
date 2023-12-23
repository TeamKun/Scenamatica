package org.kunlab.scenamatica.interfaces.context;

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
    Stage getStage();

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

    /**
     * ステージが存在するかどうかを返します。
     *
     * @return ステージが存在するかどうか
     */
    boolean hasStage();

    /**
     * アクターが存在するかどうかを返します。
     *
     * @return アクターが存在するかどうか
     */
    boolean hasActors();

    /**
     * エンティティが存在するかどうかを返します。
     *
     * @return エンティティが存在するかどうか
     */
    boolean hasEntities();

    /**
     * このコンテキストを破棄します。
     */
    void destroy();
}
