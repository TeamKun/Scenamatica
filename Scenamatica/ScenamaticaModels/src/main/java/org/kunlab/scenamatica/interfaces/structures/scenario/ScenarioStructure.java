package org.kunlab.scenamatica.interfaces.structures.scenario;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * シナリオの流れを定義します。
 */
public interface ScenarioStructure extends Structure
{

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
    ActionStructure getAction();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    ActionStructure getRunIf();

    /**
     * タイムアウトの時間を**チック**で定義します。
     * {@code -1} は無限に待つことを意味します。
     *
     * @return タイムアウトの時間
     */
    long getTimeout();

    /**
     * このシナリオの名前を取得します。
     *
     * @return 名前
     */
    String getName();
}
