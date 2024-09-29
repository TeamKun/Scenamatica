package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

@Getter
public class ScenarioCompilationErrorException extends Exception
{
    private final String scenarioName;
    @Nullable
    private final String actionName;

    public ScenarioCompilationErrorException(String message, String scenarioName, @Nullable String actionName)
    {
        super(message);
        this.scenarioName = scenarioName;
        this.actionName = actionName;
    }

    public ScenarioCompilationErrorException(ScenarioEngine engine, String message)
    {
        this(message, engine.getScenario().getName(), null);
    }

    public ScenarioCompilationErrorException(ScenarioEngine engine, String actionName, String message)
    {
        this(message, engine.getScenario().getName(), actionName);
    }

    public ScenarioCompilationErrorException(ScenarioEngine engine, Throwable cause)
    {
        this(cause.getMessage(), engine.getScenario().getName(), null);
        this.initCause(cause);
    }

    public ScenarioCompilationErrorException(ScenarioEngine engine, String actionName, Throwable cause)
    {
        this(cause.getMessage(), engine.getScenario().getName(), actionName);
        this.initCause(cause);
    }
}
