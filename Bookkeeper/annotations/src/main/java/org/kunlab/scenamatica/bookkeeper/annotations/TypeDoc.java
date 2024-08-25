package org.kunlab.scenamatica.bookkeeper.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 型に付与するドキュメント情報を定義します。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeDoc
{
    /**
     * タイプの名前を取得します。
     *
     * @return タイプの名前
     */
    @NotNull
    String name();

    /**
     * タイプの説明を取得します。
     *
     * @return タイプの説明
     */
    @NotNull
    String description();

    /**
     * タイプのプロパティを取得します。
     *
     * @return タイプのプロパティ
     */
    @NotNull
    TypeProperty[] properties() default {};

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
    @Nullable
    MCVersion supportsUntil() default MCVersion.UNSET;

    /**
     * マッピングしているクラスを取得します。
     *
     * @return マッピングしているクラス
     */
    Class<?> mappingOf() default Object.class;

    /**
     * エイリアスとして使用するクラスを取得します。
     *
     * @return エイリアスとして使用するクラス
     */
    Class<?> extending() default Object.class;

    /**
     * 型のアドモ二ションを取得します。
     *
     * @return 型のアドモ二ション
     */
    @NotNull
    Admonition[] admonitions() default {};
}
