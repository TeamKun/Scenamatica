package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * アクションをコンパイルするインタフェースです。
 */
public interface ActionCompiler
{
    /**
     * アクションをコンパイルします。
     *
     * @param registry      コンパイルに必要な情報を持つレジストリ
     * @param engine        コンパイルに必要な情報を持つシナリオエンジン
     * @param bean          アクションの情報
     * @param reportErrorTo コンパイルに失敗したときに呼び出されるコールバック
     * @param onSuccess     コンパイルに成功したときに呼び出されるコールバック
     * @param <A>           アクションの引数の型
     * @return コンパイルされたアクション
     */
    <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                         @NotNull ScenarioEngine engine,
                                                         @NotNull ActionBean bean,
                                                         @Nullable BiConsumer<CompiledAction<?>, Throwable> reportErrorTo,
                                                         @Nullable Consumer<CompiledAction<?>> onSuccess);

    /**
     * 登録されたアクションのリストを取得します。
     * 注：このリストは変更不可です。
     *
     * @return 登録されたアクションのリスト
     */
    @NotNull
    List<? extends Action<?>> getRegisteredActions();

    /**
     * アクションのクラスからアクションのインスタンスを取得します。
     *
     * @param actionClass アクションのクラス
     * @return アクションのインスタンス
     * @throws IllegalArgumentException アクションが見つからない場合
     */
    @NotNull <T extends Action<?>> T findAction(@NotNull Class<? extends T> actionClass);
}
