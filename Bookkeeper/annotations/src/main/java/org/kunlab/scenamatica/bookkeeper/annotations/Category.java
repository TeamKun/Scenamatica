package org.kunlab.scenamatica.bookkeeper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * カテゴリを示すアノテーションです。
 * カテゴリは, グループ化に使用されます。
 * 単一のクラスまたはその親クラスに適用して使用します。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Category
{
    /**
     * カテゴリの識別名です。
     *
     * @return カテゴリの識別名
     */
    String id() default "";

    /**
     * カテゴリの名前です。
     *
     * @return カテゴリの名前
     */
    String name() default "";

    /**
     * カテゴリの説明です。
     *
     * @return カテゴリの説明
     */
    String description() default "";

    /**
     * 継承するカテゴリが付与されたクラス・インタフェースです。
     */
    Class<?> inherit() default Object.class;
}
