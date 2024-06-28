package org.kunlab.scenamatica.bookkeeper.annotations;

import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Admonition は、警告や注意を表すアドモニションを表すアノテーションです。
 * アクションや入力, 出力に付与します。
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface Admonition
{
    /**
     * アドモニションの種類を指定します。
     *
     * @return アドモニションの種類
     */
    AdmonitionType value();

    /**
     * アドモニションのタイトルを指定します。
     *
     * @return アドモニションのタイトル
     */
    String title() default "";

    /**
     * アドモニションの内容を指定します。
     *
     * @return アドモニションの内容
     */
    String content();

    /**
     * アクション, その入力やその出力に付与する場合, シナリオのタイプによって表示するかどうかを指定します。
     *
     * @return 表示するシナリオのタイプ
     */
    ActionMethod on() default ActionMethod.UNSET;
}
