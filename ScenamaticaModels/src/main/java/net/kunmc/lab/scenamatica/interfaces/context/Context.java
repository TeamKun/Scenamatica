package net.kunmc.lab.scenamatica.interfaces.context;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.bukkit.World;

import java.util.List;

/**
 * シナリオのコンテキストを表すインターフェースです。
 */
public interface Context
{
    /**
     * ステージを取得します。
     *
     * @return ステージ
     */
    World getStage();

    /**
     * アクターのリストを取得します。
     *
     * @return アクターのリスト
     */
    List<? extends Actor> getActors();

    /**
     * コンテキストの Bean を取得します。
     *
     * @return コンテキストの Bean
     */
    ContextBean getBean();
}
