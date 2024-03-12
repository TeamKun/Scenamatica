package org.kunlab.scenamatica.nms;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * NMS の型の相互運用をサポートします。
 */
public interface TypeSupport
{
    /**
     * 対応する NMS の型に変換します。
     *
     * @param <T>   NMS の型
     * @param <U>   型
     * @param value 値
     * @param clazz NMS の型
     */
    @Contract("null, _ -> null")
    <T, U extends NMSElement> T toNMS(@Nullable U value, @NotNull Class<T> clazz);

    /**
     * NMS を 対応する型に変換します。
     *
     * @param <T>      型
     * @param nmsValue NMS の値
     * @param clazz    値
     */
    @Contract("null, _ -> null")
    <T extends NMSElement> T fromNMS(@Nullable Object nmsValue, @NotNull Class<T> clazz);
}
