package net.kunmc.lab.scenamatica.scenario.beans.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * シナリオの動作の定義を表すクラスです。
 */
@Value
public class ActionBean implements Serializable
{
    /**
     * 動作を定義します。
     */
    @NotNull
    Action<?> action;

    /**
     * 動作に必要な引数を定義します。
     */
    @Nullable
    ActionArgument argument;
}
