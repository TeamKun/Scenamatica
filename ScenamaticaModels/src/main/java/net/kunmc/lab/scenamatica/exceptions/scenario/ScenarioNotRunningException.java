package net.kunmc.lab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * シナリオが実行中でないことを表す例外です。
 */
@Getter
public class ScenarioNotRunningException extends ScenarioException
{
    @Nullable
    private final String scenarioName;

    public ScenarioNotRunningException(@NotNull String scenarioName)
    {
        super("Scenario " + scenarioName + " is not running.");
        this.scenarioName = scenarioName;
    }

    public ScenarioNotRunningException()
    {
        super("Scenario is not running.");
        this.scenarioName = null;
    }
}
