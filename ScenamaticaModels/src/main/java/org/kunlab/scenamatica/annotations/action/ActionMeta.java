package org.kunlab.scenamatica.annotations.action;

import org.kunlab.scenamatica.enums.MinecraftVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * アクションに付与するアノテーションです。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionMeta
{
    /**
     * アクションの名前を返します。
     *
     * @return アクションの名前
     */
    String value();

    /**
     * サポートする Minecraft バージョンの開始バージョンを返します。
     *
     * @return サポートする Minecraft バージョンの開始バージョン
     */
    MinecraftVersion supportsSince() default MinecraftVersion.V1_13;

    /**
     * サポートする Minecraft バージョンの終了バージョンを返します。
     *
     * @return サポートする Minecraft バージョンの終了バージョン
     */
    MinecraftVersion supportsUntil() default MinecraftVersion.V1_20_4;
}
