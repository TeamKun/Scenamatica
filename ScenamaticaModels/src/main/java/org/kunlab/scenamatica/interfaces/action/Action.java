package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;

import java.util.Map;

/**
 * 動作のインタフェースです。
 */
public interface Action<A extends ActionArgument>
{
    /**
     * 動作のシリアライズ名を返します。
     *
     * @return 動作のシリアライズ名
     */
    String getName();

    /**
     * 引数をデシリアライズします。
     *
     * @param map        デシリアライズするマップ
     * @param serializer シリアライザ
     * @return デシリアライズされた引数
     */
    A deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer);
}
