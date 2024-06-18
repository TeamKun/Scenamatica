package org.kunlab.scenamatica.bookkeeper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * カテゴリを示すアノテーションです。
 * カテゴリは, グループ化に使用されます。
 * 単一のクラスまたはその親クラスに適用して使用します。<br>
 * 複数の同名のカテゴリが存在する場合, そのカテゴリは一つのカテゴリとして扱われます。
 * その時のプロパティは inherit が true のものが優先されます。
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
    String id();

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
     * このカテゴリが継承するカテゴリの識別名です。
     *
     * @return 継承するカテゴリの識別名
     */
    String extendsOf() default "";

    /**
     * このカテゴリが継承するカテゴリのプロパティを継承するかどうかです。
     * 同一インスタンス内に２つ以上の同名のカテゴリが存在する場合, このプロパティが true ではない場合はエラーとなります。
     *
     * @return 継承する場合は true, それ以外は false
     */
    boolean inherit() default false;
}
