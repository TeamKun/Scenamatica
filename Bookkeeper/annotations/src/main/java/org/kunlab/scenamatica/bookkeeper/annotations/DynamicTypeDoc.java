package org.kunlab.scenamatica.bookkeeper.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface DynamicTypeDoc
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
     * 振る舞いを取得します。
     *
     * @return 振る舞い
     */
    TypeDoc[] anyOf();
}
