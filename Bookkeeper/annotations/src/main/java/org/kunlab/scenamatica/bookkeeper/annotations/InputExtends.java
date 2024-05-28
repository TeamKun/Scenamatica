package org.kunlab.scenamatica.bookkeeper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * スーパーアクションの入力を拡張するためのアノテーションです。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface InputExtends
{
    /**
     * 拡張する入力フィールドを取得します。
     * @return 拡張する入力フィールド
     */
    InputDoc[] value();
}
