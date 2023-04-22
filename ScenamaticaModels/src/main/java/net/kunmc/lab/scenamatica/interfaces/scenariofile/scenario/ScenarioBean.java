package net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * シナリオの流れを定義します。
 */
public interface ScenarioBean extends Serializable
{
    String KEY_TIMEOUT = "timeout";

    /**
     * このシナリオのタイプを取得します。
     *
     * @return タイプ
     */
    @NotNull
    ScenarioType getType();

    /**
     * このシナリオが実行するアクションを取得します。
     *
     * @return アクション
     */
    @NotNull
    ActionBean getAction();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    ActionBean getRunIf();

    /**
     * タイムアウトの時間を**チック**で定義します。
     * {@code -1} は無限に待つことを意味します。
     *
     * @return タイムアウトの時間
     */
    long getTimeout();
}
