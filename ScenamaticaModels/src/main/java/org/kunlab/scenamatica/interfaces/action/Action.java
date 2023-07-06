package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;

import java.util.Map;

/**
 * 動作のインタフェースです。
 */
public interface Action<A extends ActionArgument>
{
    /**
     * 動作のシリアライズ名を返します。
     *
     * @return 動作のシリアライズ名
     */
    String getName();

    /**
     * 動作を実行します。
     *
     * @param engine   シナリオエンジン
     * @param argument 動作の引数
     */
    void execute(@NotNull ScenarioEngine engine, @Nullable A argument);

    /**
     * 引数をデシリアライズします。
     *
     * @param map        デシリアライズするマップ
     * @param serializer シリアライザ
     * @return デシリアライズされた引数
     */
    A deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer);

    /**
     * 引数が正しいかチェックします。
     *
     * @param engine   シナリオエンジン
     * @param type     シナリオの種類
     * @param argument 引数
     */
    void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable A argument);
}
