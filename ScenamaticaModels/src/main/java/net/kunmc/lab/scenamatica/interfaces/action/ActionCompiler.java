package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param bean          アクションの情報
     * @param reportErrorTo コンパイルに失敗したときに呼び出されるコールバック
     * @param onSuccess     コンパイルに成功したときに呼び出されるコールバック
     * @param <A>           アクションの引数の型
     * @return コンパイルされたアクション
     */
    <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                         @NotNull ActionBean bean,
                                                         @Nullable BiConsumer<CompiledAction<A>, Throwable> reportErrorTo,
                                                         @Nullable Consumer<CompiledAction<A>> onSuccess);
}
