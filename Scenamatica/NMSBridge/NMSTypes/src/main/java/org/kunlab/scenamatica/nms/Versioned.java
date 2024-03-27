package org.kunlab.scenamatica.nms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API が利用可能なバージョンを指定します。
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.SOURCE)
public @interface Versioned
{
    /**
     * API が利用可能なバージョンを指定します。
     *
     * @return API が利用可能なバージョン
     */
    String value() default "";

    /**
     * API が利用可能になったバージョンを指定します。
     *
     * @return API が利用可能になったバージョン
     */
    String from() default "";

    /**
     * API が廃止されたバージョンを指定します。
     *
     * @return API が廃止されたバージョン
     */
    String to() default "";
}
