package org.kunlab.scenamatica.bookkeeper.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * アクションに付与するドキュメント情報を定義します。
 * これは, @Action アノテーションとは関係なく, 独立したモジュールに属します。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ActionDoc
{
    static final String ALLOWED = "$alloawed$";
    static final String UNALLOWED = "$unallowed$";

    /**
     * アクションの一意な名前を取得します。
     * <p>
     * アクションの名前は次の文字で構成される必要があります：
     *
     * <ul>
     *     <li>英小文字(a-z)</li>
     *     <li>数字(0-9)</li>
     *     <li>アンダースコア(_)</li>
     * </ul>
     * <p>
     * 単語間にはアンダースコアを使用してください。
     *
     * @return アクションの名前
     */
    @NotNull
    String name();

    /**
     * アクションの説明を取得します。
     *
     * @return アクションの説明
     */
    @NotNull
    String description() default "";

    /**
     * アクションが対応するイベントのクラスを取得します。
     *
     * @return アクションが対応するイベントのクラス
     */
    @NotNull
    Class<?>[] events() default {};

    /**
     * アクションが実行可能であることを示します。
     * 実行可能である場合は, {@link #ALLOWED} または, その説明を指定します。
     *
     * @return 実行可能である場合は, {@link #ALLOWED} または, その説明
     */
    @Nullable
    String executable() default ALLOWED;

    /**
     * アクションが監視可能であることを示します。
     * 監視可能である場合は, {@link #ALLOWED} または, その説明を指定します。
     *
     * @return 監視可能である場合は, {@link #ALLOWED} または, その説明
     */
    @Nullable
    String watchable() default ALLOWED;

    /**
     * アクションが要求可能であることを示します。
     * 要求可能である場合は, {@link #ALLOWED} または, その説明を指定します。
     *
     * @return 要求可能である場合は, {@link #ALLOWED} または, その説明
     */
    @Nullable
    String requireable() default ALLOWED;

    /**
     * サポートする Minecraft バージョンの開始バージョンを返します。
     *
     * @return サポートする Minecraft バージョンの開始バージョン
     */
    @NotNull
    MCVersion supportsSince() default MCVersion.UNSET;

    /**
     * サポートする Minecraft バージョンの終了バージョンを返します。
     *
     * @return サポートする Minecraft バージョンの終了バージョン
     */
    @NotNull
    MCVersion supportsUntil() default MCVersion.UNSET;

    /**
     * アクションの出力情報を取得します。
     *
     * @return アクションの出力情報
     */
    @NotNull
    OutputDoc[] outputs() default {};
}
