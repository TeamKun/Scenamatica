package net.kunmc.lab.scenamatica.scenario.beans.scenario;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * シナリオの流れを定義します。
 */
@Value
public class ScenarioBean implements Serializable
{
    /**
     * シナリオの種類を記述します。
     */
    @NotNull
    ScenarioType type;

    /**
     * シナリオの動作を記述します。
     * これは, {@link #type} が {@link ScenarioType#ACTION_EXPECT} か {@link ScenarioType#ACTION_EXECUTE} の場合に必要です。
     */
    @Nullable
    ActionBean action;

    /**
     * 必要なコンディションを記述します。
     * これは, {@link #type} が {@link ScenarioType#CONDITION_REQUIRE} の場合に必要です。
     */
    @Nullable
    ConditionBean condition;
}
