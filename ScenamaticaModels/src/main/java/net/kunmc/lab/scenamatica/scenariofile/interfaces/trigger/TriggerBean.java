package net.kunmc.lab.scenamatica.scenariofile.interfaces.trigger;

import net.kunmc.lab.scenamatica.scenariofile.interfaces.scenario.ScenarioBean;

import java.io.Serializable;
import java.util.List;

/**
 * シナリオが実行されるタイミングを表すインターフェースです。
 */
public interface TriggerBean extends Serializable
{
    /**
     * このトリガーのタイプを取得します。
     *
     * @return タイプ
     */
    KeyedTriggerType getType();

    /**
     * このトリガーの引数を取得します。
     *
     * @return 引数
     */
    TriggerArgument getArgument();

    /**
     * このトリガーが実行される前に実行されるシナリオを取得します。
     *
     * @return シナリオ
     */
    List<ScenarioBean> getBeforeThat();

    /**
     * このトリガーが実行された後に実行されるシナリオを取得します。
     *
     * @return シナリオ
     */
    List<ScenarioBean> getAfterThat();
}
