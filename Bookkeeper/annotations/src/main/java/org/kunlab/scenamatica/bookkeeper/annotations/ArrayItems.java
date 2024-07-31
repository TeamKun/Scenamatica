package org.kunlab.scenamatica.bookkeeper.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface ArrayItems
{
    /**
     * 配列の要素を取得します。
     *
     * @return 配列の要素
     */
    @NotNull
    InputDoc[] value();
}
