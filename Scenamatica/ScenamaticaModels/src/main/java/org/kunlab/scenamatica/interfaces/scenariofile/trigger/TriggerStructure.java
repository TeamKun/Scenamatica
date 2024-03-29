package org.kunlab.scenamatica.interfaces.scenariofile.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;

import java.util.List;

/**
 * シナリオが実行されるタイミングを表すインターフェースです。
 */
public interface TriggerStructure extends Structure
{
    /**
     * このトリガーのタイプを取得します。
     *
     * @return タイプ
     */
    @NotNull
    TriggerType getType();

    /**
     * このトリガーの引数を取得します。
     *
     * @return 引数
     */
    @Nullable
    TriggerArgument getArgument();

    /**
     * このトリガーが実行される前に実行されるシナリオを取得します。
     *
     * @return シナリオ
     */
    @NotNull
    List<ScenarioStructure> getBeforeThat();

    /**
     * このトリガーが実行された後に実行されるシナリオを取得します。
     *
     * @return シナリオ
     */
    @NotNull
    List<ScenarioStructure> getAfterThat();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    ActionStructure getRunIf();

}
