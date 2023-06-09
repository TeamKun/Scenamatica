package org.kunlab.scenamatica.interfaces.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;

import java.io.Serializable;
import java.util.List;

/**
 * シナリオのファイルの情報を表すインターフェースです。
 */
public interface ScenarioFileBean extends Serializable
{
    long DEFAULT_TIMEOUT_TICK = 20L * 60L * 5L;
    int DEFAULT_ORDER = Integer.MAX_VALUE;

    /**
     * 対応する Scenamatica のバージョンを取得します。
     *
     * @return Scenamatica のバージョン
     */
    @NotNull
    Version getScenamaticaVersion();

    /**
     * このシナリオの名前を取得します。
     * 対象プラグインで一意である必要があります。
     * キャメルケースが推奨されています。
     *
     * @return 名前
     */
    @NotNull
    String getName();

    /**
     * このシナリオの説明を取得します。
     *
     * @return 説明
     */
    @NotNull
    String getDescription();

    /**
     * シナリオ全体のタイムアウトの時間を**チック**で定義します。
     *
     * @return タイムアウトの時間
     */
    long getTimeout();

    /**
     * 同一セッションでのシナリオの実行順序を定義します。
     * 値が小さいほど先に実行されます。
     * デフォルトでは {@link Integer#MAX_VALUE} が設定されています。
     *
     * @return 実行順序
     */
    int getOrder();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    @Nullable
    ActionBean getRunIf();

    /**
     * このシナリオの実行タイミングを取得します。
     *
     * @return 実行タイミング
     */
    @NotNull
    List<TriggerBean> getTriggers();

    /**
     * このシナリオの実行に必要な情報を取得します。
     *
     * @return 実行に必要な情報
     */
    @Nullable
    ContextBean getContext();

    /**
     * シナリオを取得します。
     *
     * @return シナリオ
     */
    @NotNull
    List<ScenarioBean> getScenario();
}
