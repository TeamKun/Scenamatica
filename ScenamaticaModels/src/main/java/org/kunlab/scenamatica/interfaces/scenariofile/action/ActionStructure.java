package org.kunlab.scenamatica.interfaces.scenariofile.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * シナリオの動作の定義を表すインターフェースです。
 */
public interface ActionStructure extends Structure
{
    String KEY_TYPE = "action";
    String KEY_ARGUMENTS = "with";

    /**
     * 動作の種類を定義します。
     *
     * @return 動作の種類
     */
    @NotNull
    String getType();

    /**
     * 動作に必要な引数を定義します。
     *
     * @return 動作に必要な引数
     */
    @Nullable
    Map<String, Object> getArguments();
}
