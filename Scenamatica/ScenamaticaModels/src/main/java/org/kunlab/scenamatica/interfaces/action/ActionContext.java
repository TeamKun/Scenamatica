package org.kunlab.scenamatica.interfaces.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ActionResultCause;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * アクション実行時のコンテキストを表すクラスです。
 */
public interface ActionContext
{
    /**
     * コンテキストの ID を取得します。
     *
     * @return コンテキストの ID
     */
    UUID getContextID();

    /**
     * 指定された名前を取得します。
     *
     * @return 名前
     */
    @Nullable
    String getScenarioName();

    /**
     * 名前を設定します。
     *
     * @param name 名前
     */
    void setScenarioName(@Nullable String name);

    /**
     * アクションを成功として終了します。
     */
    void success();

    /**
     * これ以降のアクションの実行をスキップします。
     */
    void halt();

    /**
     * これ以降のアクションの実行をスキップしません。
     */
    void noHalt();

    /**
     * このアクションはスキップされたとして終了します。
     */
    void skip();

    /**
     * このアクションがスキップされたかどうかを取得します。
     *
     * @return スキップされたかどうか
     */
    boolean isSkipped();

    /**
     * アクションを失敗として終了し、これ以降のアクションの実行をスキップします。
     *
     * @see #fail(ActionResultCause)
     * @see #halt()
     */
    void fail();

    /**
     * アクションを失敗として終了し、これ以降のアクションの実行をスキップします。
     *
     * @param cause 失敗の原因
     */
    void fail(@NotNull ActionResultCause cause);

    /**
     * アクションを失敗として終了し、これ以降のアクションの実行をスキップします。
     * 失敗の理由には {@link ActionResultCause#INTERNAL_ERROR} が設定されます。
     *
     * @param err 失敗の原因
     */
    void fail(@NotNull Throwable err);

    /**
     * アクションを失敗として終了し、これ以降のアクションの実行をスキップします。
     *
     * @param cause 失敗の原因
     * @param err   失敗の原因
     */
    void fail(@NotNull ActionResultCause cause, @Nullable Throwable err);

    /**
     * アクションを失敗として終了し、これ以降のアクションの実行をスキップします。
     *
     * @return 失敗の原因
     */
    @Nullable
    Throwable getError();

    /**
     * アクションの実行結果を出力します。
     *
     * @param key   キー
     * @param value 値
     */
    void output(String key, Object value);

    /**
     * アクションの実行結果を出力します。
     *
     * @param kvPairs キーと値のペア
     */
    void outputs(Object... kvPairs);

    /**
     * アクションの実行結果を出力します。
     */
    void commitOutput();

    /**
     * 成功状態を持っているかどうかを取得します。
     *
     * @return 成功状態を持っているかどうか
     */
    boolean hasSuccess();

    /**
     * アクションの入力を取得します。
     *
     * @param token トークン
     * @param <T>   値の型
     * @return 値
     */
    <T> T input(InputToken<T> token);

    /**
     * アクションの入力があるかどうかを取得します。
     *
     * @param token トークン
     * @param <T>   値の型
     * @return 値
     */
    <T> boolean hasInput(InputToken<T> token);

    /**
     * アクションの入力を取得します。
     *
     * @param token        トークン
     * @param defaultValue デフォルト値
     * @param <T>          値の型
     * @return 値
     */
    <T> T orElseInput(InputToken<? extends T> token, @NotNull Supplier<? extends T> defaultValue);

    /**
     * アクションの入力があった場合に、指定された処理を実行します。
     *
     * @param token    トークン
     * @param consumer 適用する Consumer
     * @param <T>      値の型
     * @return 適用結果
     */
    <T> boolean runIfHasInput(@NotNull InputToken<T> token, @NotNull Consumer<? super T> consumer);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     *
     * @param mapper       適用する関数
     * @param defaultValue 値が null の場合に返す値
     * @param token        トークン
     * @param <T>          値の型
     * @param <U>          適用結果の型
     * @return 適用結果
     */
    <T, U> U ifHasInput(@NotNull InputToken<T> token, @NotNull Function<? super T, ? extends U> mapper, @Nullable U defaultValue);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     *
     * @param predicate    適用する Predicate
     * @param defaultValue 値が null の場合に返す値
     * @param token        トークン
     * @param <T>          値の型
     * @return 適用結果
     */
    <T> boolean ifHasInput(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate, boolean defaultValue);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     * null の場合は true を返します。
     *
     * @param predicate 適用する Predicate
     * @param token     トークン
     * @param <T>       値の型
     * @return 適用結果
     */
    <T> boolean ifHasInput(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate);

    /**
     * シリアライザを取得します。
     *
     * @return シリアライザ
     */
    StructureSerializer getSerializer();

    /**
     * 入力値のホルダーを取得します。
     *
     * @param token トークン
     * @param <T>   値の型
     * @return ホルダー
     */
    <T> InputValueHolder<T> getHolder(InputToken<T> token);

    /**
     * アクションを実行するエンジンを取得します。
     *
     * @return エンジン
     */
    ScenarioEngine getEngine();

    /**
     * アクションの入力を取得します。
     *
     * @return 入力
     */
    InputBoard getInput();

    /**
     * アクションの出力を取得します。
     *
     * @return 出力
     */
    Map<String, Object> getOutput();

    /**
     * アクションの実行結果を取得します。
     *
     * @return 実行結果
     */
    boolean isSuccess();

    /**
     * アクションの実行結果の原因を取得します。
     *
     * @return 原因
     */
    ActionResultCause getCause();

    /**
     * アクションの実行をスキップするかどうかを取得します。
     *
     * @return スキップするかどうか
     */
    boolean isHalt();

    /**
     * アクターを取得します。
     *
     * @param bukkitEntity Bukkit のエンティティ
     * @return アクター
     */
    Optional<Actor> getActor(@NotNull Player bukkitEntity);

    /**
     * アクターを取得します。
     *
     * @param bukkitEntity Bukkit のエンティティ
     * @return アクター
     * @throws IllegalArgumentException アクターが存在しない場合
     */
    @NotNull
    Actor getActorOrThrow(@NotNull Player bukkitEntity);

    /**
     * コンテキストを取得します。
     *
     * @return コンテキスト
     */
    Context getContext();

    /**
     * ロガーを取得します。
     *
     * @return ロガー
     */
    Logger getLogger();

    /**
     * アクションを取得します。
     *
     * @param actionClass アクションのクラス
     * @param <T>         アクションの型
     * @return アクション
     */
    <T extends Action> T findAction(Class<T> actionClass);

    /**
     * あたらしい入力をもとに、新しいコンテキストを生成します。
     *
     * @param input 新しい入力
     * @return 新しいコンテキスト
     */
    ActionContext renew(InputBoard input);

    /**
     * アクションの実行結果に変換します。
     *
     * @param action アクション
     * @return 実行結果
     */
    ActionResult createResult(@NotNull CompiledAction action);

    /**
     * アクションの実行結果を出力するかどうかを設定します。
     *
     * @param doOutput 出力するかどうか
     */
    void doOutput(boolean doOutput);

    /**
     * アクションの実行結果を出力するかどうかを取得します。
     *
     * @return 出力するかどうか
     */
    boolean doOutput();

    /**
     * アクションがどこで実行されるかを取得します。
     *
     * @return 実行される場所
     */
    RunOn getRunOn();

    /**
     * アクションが誰によって実行されるかを取得します。
     *
     * @return 実行されるアクター
     */
    RunAs getRunAs();

    /**
     * リセットします。
     */
    void reset();

    String[] getUnresolvedReferences();
}
