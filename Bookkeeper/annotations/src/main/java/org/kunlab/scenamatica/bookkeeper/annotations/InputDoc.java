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
 * 入力フィールドに付与するドキュメント情報を定義します。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface InputDoc
{
    /**
     * 入力フィールドの名前を取得します。
     *
     * @return 入力フィールドの名前
     */
    @NotNull
    String name();

    /**
     * 入力フィールドの説明を取得します。
     *
     * @return 入力フィールドの説明
     */
    @NotNull
    String description() default "";

    /**
     * 入力フィールドが必須であるメソッドを取得します。
     *
     * @return 入力フィールドが必須であるメソッド
     */
    @NotNull
    ActionMethod[] requiredOn() default {};

    /**
     * 入力フィールドが利用可能であるメソッドを取得します。
     *
     * @return 入力フィールドが利用可能であるメソッド
     */
    @NotNull
    ActionMethod[] availableFor() default {};

    /**
     * 入力フィールドの型を取得します。
     * 指定しない場合は string として扱われます。
     * <p>
     * Array を指定する場合は, 別途 {@link ArrayItems} を付与してください。
     *
     * @return 入力フィールドの型
     * @see ArrayItems
     */
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
     * プレイヤがアクタである必要があることを示します。
     *
     * @return プレイヤがアクタである必要があるかどうか
     */
    boolean requiresActor() default false;

    /**
     * 入力のアドモ二ションを取得します。
     *
     * @return 入力のアドモ二ション
     */
    @NotNull
    Admonition[] admonitions() default {};

    /**
     * 「アクション」を引数として取る場合に, そのアクションの種類を取得します。
     *
     * @return アクションの種類
     */
    ActionMethod actionKindOf() default ActionMethod.UNSET;
}
