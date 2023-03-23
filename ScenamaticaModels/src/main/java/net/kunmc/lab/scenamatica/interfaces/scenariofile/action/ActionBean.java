package net.kunmc.lab.scenamatica.interfaces.scenariofile.action;

import net.kunmc.lab.scenamatica.enums.ActionType;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * シナリオの動作の定義を表すインターフェースです。
 */
public interface ActionBean extends TriggerArgument
{
    String KEY_TYPE = "action";
    String KEY_ARGUMENTS = "with";

    /**
     * 動作の種類を定義します。
     *
     * @return 動作の種類
     */
    @NotNull
    ActionType getType();

    /**
     * 動作に必要な引数を定義します。
     *
     * @return 動作に必要な引数
     */
    @Nullable
    ActionArgument getArgument();
}
