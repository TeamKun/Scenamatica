package net.kunmc.lab.scenamatica.scenario.beans.trigger;

import lombok.Value;
import net.kunmc.lab.scenamatica.scenario.beans.scenario.ActionBean;
import net.kunmc.lab.scenamatica.scenario.beans.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * シナリオがトリガーされるタイミングを表すクラスです。
 */
@Value
public class TriggerBean implements Serializable
{
    /**
     * トリガーの種類です。殆どの場合は {@link TriggerType#ON_ACTION} です。
     */
    @NotNull
    TriggerType type;

    /**
     * 種類が {@link TriggerType#ON_ACTION} だった場合に, 動作を格納します。
     */
    @Nullable
    ActionBean action;

    /**
     * 本シナリオを実行する前に実行するシナリオを格納します。
     */
    @Nullable
    ScenarioBean beforeThat;

    /**
     * 本シナリオを実行した後に実行するシナリオを格納します。
     */
    @Nullable
    ScenarioBean afterThat;
}
