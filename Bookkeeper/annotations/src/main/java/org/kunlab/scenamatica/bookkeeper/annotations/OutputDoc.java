package org.kunlab.scenamatica.bookkeeper.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * アクションの出力情報を定義します。
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface OutputDoc
{
    /**
     * 出力の名前を取得します。
     * @return 出力の名前
     */
    @NotNull
    String name();

    /**
     * 出力の説明を取得します。
     * @return 出力の説明
     */
    @NotNull
    String description();

    /**
     * 出力の対象メソッドを取得します。
     * @return 出力の対象メソッド
     */
    @NotNull
    ActionMethod[] target() default {ActionMethod.EXECUTE, ActionMethod.REQUIRE, ActionMethod.WATCH};

    /**
     * 出力の型を取得します。
     * @return 出力の型
     */
    @NotNull
    Class<?> type();

    /**
     * サポートする Minecraft バージョンの開始バージョンを返します。
     *
     * @return サポートする Minecraft バージョンの開始バージョン
     */
    @Nullable
    MCVersion supportsSince() default MCVersion.UNSET;

    /**
     * サポートする Minecraft バージョンの終了バージョンを返します。
     *
     * @return サポートする Minecraft バージョンの終了バージョン
     */
    MCVersion supportsUntil() default MCVersion.UNSET;

    /**
     * 文字列における最大文字数または, 数値における最大値を取得します。
     *
     *
     * @return 最大値
     */
    double max() default Integer.MAX_VALUE;

    /**
     * 文字列における最小文字数または, 数値における最小値を取得します。
     *
     * @return 最小値
     */
    double min() default Integer.MIN_VALUE;

}
