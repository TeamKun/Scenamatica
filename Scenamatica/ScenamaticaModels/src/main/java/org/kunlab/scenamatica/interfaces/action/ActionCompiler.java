package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioCompilationErrorException;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;

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
     * @param runOn         アクションが実行されるタイミング
     * @param runAs         アクションが実行される権限
     * @param structure     アクションの情報
     * @param reportErrorTo アクションの実行に失敗した場合に呼び出されるコールバック
     * @param onSuccess     アクションの実行に成功した場合に呼び出されるコールバック
     * @return コンパイルされたアクション
     */
    CompiledAction compile(
            @NotNull ScenarioEngine engine,
            @NotNull ScenarioType scenarioType,
            @NotNull RunOn runOn,
            @NotNull RunAs runAs,
            @NotNull ActionStructure structure,
            @Nullable BiConsumer<CompiledAction, Throwable> reportErrorTo,
            @Nullable BiConsumer<ActionResult, ScenarioType> onSuccess)
            throws ScenarioCompilationErrorException;
}
