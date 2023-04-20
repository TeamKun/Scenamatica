package net.kunmc.lab.scenamatica.interfaces.scenario.runtime;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * コンパイルされたシナリオアクションを表すインターフェースです。
 *
 * @param <A> アクションの引数
 */
public interface CompiledScenarioAction<A extends ActionArgument>
{
    /**
     * アクションを実行します。
     *
     * @param engine   シナリオエンジン
     * @param manager  アクションマネージャー
     * @param listener 実行結果を受け取るリスナー
     */
    void execute(@NotNull ScenarioEngine engine, @NotNull ActionManager manager, @NotNull ScenarioActionListener listener);

    /**
     * コンパイル前のアクションを取得します。
     *
     * @return コンパイル前のアクション
     */
    @NotNull
    ScenarioBean getBean();

    /**
     * シナリオの種類を取得します。
     *
     * @return シナリオの種類
     */
    @NotNull
    ScenarioType getType();

    /**
     * アクションを取得します。
     *
     * @return アクション
     */
    @NotNull
    Action<A> getAction();

    /**
     * アクションの引数を取得します。
     *
     * @return アクションの引数
     */
    @Nullable
    A getArgument();
}
