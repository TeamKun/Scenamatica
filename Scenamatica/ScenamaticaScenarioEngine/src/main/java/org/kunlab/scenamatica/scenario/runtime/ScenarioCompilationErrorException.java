package org.kunlab.scenamatica.scenario.runtime;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class ScenarioCompilationErrorException extends RuntimeException
{
    private final String scenarioName;
    @Nullable
    private final String actionName;

    public ScenarioCompilationErrorException(String scenarioName)
    {
        this.scenarioName = scenarioName;
        this.actionName = null;
    }

    public ScenarioCompilationErrorException(String message, String scenarioName)
    {
        super("An error has occurred while compiling scenario " + scenarioName + ": " + message);
        this.scenarioName = scenarioName;
        this.actionName = null;
    }

    public ScenarioCompilationErrorException(Throwable cause, String scenarioName)
    {
        super("An error has occurred while compiling scenario " + scenarioName, cause);
        this.scenarioName = scenarioName;
        this.actionName = null;
    }

    public ScenarioCompilationErrorException(String message, String scenarioName, String actionName)
    {
        super("An error has occurred while compiling scenario " + scenarioName + " (action " + actionName + "): " + message);
        this.scenarioName = scenarioName;
        this.actionName = actionName;
    }

    public ScenarioCompilationErrorException(Throwable cause, String scenarioName, String actionName)
    {
        super("An error has occurred while compiling scenario " + scenarioName + " (action " + actionName + ")", cause);
        this.scenarioName = scenarioName;
        this.actionName = actionName;
    }
}
