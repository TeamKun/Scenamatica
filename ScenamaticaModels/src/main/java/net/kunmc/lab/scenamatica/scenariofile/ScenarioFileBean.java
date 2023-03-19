package net.kunmc.lab.scenamatica.scenariofile;

import net.kunmc.lab.scenamatica.scenariofile.interfaces.context.ContextBean;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.trigger.TriggerBean;

import java.io.Serializable;
import java.util.List;

/**
 * シナリオのファイルの情報を表すインターフェースです。
 */
public interface ScenarioFileBean extends Serializable
{
    /**
     * このシナリオの名前を取得します。
     * 人間でも読みやすい名前が望ましいです。
     *
     * @return 名前
     */
    String getName();

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
