package org.kunlab.scenamatica.bookkeeper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 複数の出力ドキュメントを定義します。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface OutputDocs
{
    /**
     * 出力ドキュメントの配列を取得します。
     * @return 出力ドキュメントの配列
     */
    OutputDoc[] value();
}
