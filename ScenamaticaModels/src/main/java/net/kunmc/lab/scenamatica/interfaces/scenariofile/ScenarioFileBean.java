package net.kunmc.lab.scenamatica.interfaces.scenariofile;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;

import java.io.Serializable;
import java.util.List;

/**
 * シナリオのファイルの情報を表すインターフェースです。
 */
public interface ScenarioFileBean extends Serializable
{
    /**
     * このシナリオの名前を取得します。
     * 対象プラグインで一意である必要があります。
     * キャメルケースが推奨されています。
     *
     * @return 名前
     */
    String getName();

    /**
     * このシナリオの説明を取得します。
     *
     * @return 説明
     */
    String getDescription();

    /**
     * このシナリオの実行タイミングを取得します。
     *
     * @return 実行タイミング
     */
    List<TriggerBean> getTriggers();

    /**
     * このシナリオの実行に必要な情報を取得します。
     *
     * @return 実行に必要な情報
     */
    ContextBean getContext();

    /**
     * シナリオを取得します。
     *
     * @return シナリオ
     */
    List<ScenarioBean> getScenario();
}
