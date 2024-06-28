package org.kunlab.scenamatica.bookkeeper.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeProperty
{
    /**
     * プロパティの名前を取得します。
     *
     * @return プロパティの名前
     */
    @NotNull
    String name();

    /**
     * プロパティの説明を取得します。
     *
     * @return プロパティの説明
     */
    @NotNull
    String description();

    /**
     * プロパティの型を取得します。
     *
     * @return プロパティの型
     */
    Class<?> type();

    /**
     * プロパティのデフォルト値を取得します。
     *
     * @return プロパティのデフォルト値
     */
    String defaultValue() default "";

    /**
     * プロパティが必須であるかどうかを取得します。
     *
     * @return プロパティが必須であるかどうか
     */
    boolean required() default false;

    /**
     * プロパティのパターンを取得します。
     * <p>
     * パターンは, 正規表現で指定し, 型が String の場合にのみ有効です。
     *
     * @return プロパティのパターン
     */
    String pattern() default "";

    /**
     * 値の初期値を取得します。
     *
     * @return 値の初期値
     */
    String constValue() default "";

    /**
     * 文字列における最大文字数または, 数値における最大値を取得します。
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

    /**
     * 型プロパティのアドモ二ションを取得します。
     *
     * @return 型プロパティのアドモ二ション
     */
    @NotNull
    Admonition[] admonitions() default {};
}
