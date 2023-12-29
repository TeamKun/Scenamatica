package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * アクションをコンパイルするインタフェースです。
 */
public interface ActionCompiler
{
    /**
     * アクションをコンパイルします。
     *
     * @param engine        コンパイルに必要な情報を持つシナリオエンジン
     * @param scenarioType  アクションが属するシナリオの種類です。
     * @param structure     アクションの情報
     * @param reportErrorTo アクションの実行に失敗した場合に呼び出されるコールバック
     * @param onSuccess     アクションの実行に成功した場合に呼び出されるコールバック
     * @return コンパイルされたアクション
     */
    CompiledAction compile(
            @NotNull ScenarioEngine engine,
            @NotNull ScenarioType scenarioType,
            @NotNull ActionStructure structure,
            @Nullable BiConsumer<CompiledAction, Throwable> reportErrorTo,
            @Nullable BiConsumer<ActionResult, ScenarioType> onSuccess);

    /**
     * 登録されたアクションのリストを取得します。
     * 注：このリストは変更不可です。
     *
     * @return 登録されたアクションのリスト
     */
    @NotNull
    List<? extends Action> getRegisteredActions();

    /**
     * アクションのクラスからアクションのインスタンスを取得します。
     *
     * @param actionClass アクションのクラス
     * @return アクションのインスタンス
     * @throws IllegalArgumentException アクションが見つからない場合
     */
    @NotNull <T extends Action> T findAction(@NotNull Class<? extends T> actionClass);
}
