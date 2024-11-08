package org.kunlab.scenamatica.exceptions.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

public class ScenarioDefinitionErrorException extends ScenarioCompilationErrorException
{
    @NotNull
    private final YamlParsingException definitionException;

    public ScenarioDefinitionErrorException(String message, String scenarioName, @Nullable String actionName, @NotNull YamlParsingException definitionException)
    {
        super(message, scenarioName, actionName);
        this.definitionException = definitionException;
    }

    public ScenarioDefinitionErrorException(ScenarioEngine engine, String message, @NotNull YamlParsingException definitionException)
    {
        super(engine, message);
        this.definitionException = definitionException;
    }

    public ScenarioDefinitionErrorException(ScenarioEngine engine, String actionName, String message, @NotNull YamlParsingException definitionException)
    {
        super(engine, actionName, message);
        this.definitionException = definitionException;
    }

    public ScenarioDefinitionErrorException(ScenarioEngine engine, Throwable cause, @NotNull YamlParsingException definitionException)
    {
        super(engine, cause);
        this.definitionException = definitionException;
    }

    public ScenarioDefinitionErrorException(ScenarioEngine engine, String actionName, Throwable cause, @NotNull YamlParsingException definitionException)
    {
        super(engine, actionName, cause);
        this.definitionException = definitionException;
    }

    public String getShortMessage()
    {
        return this.definitionException.getShortMessage();
    }

    @Override
    public String getMessage()
    {
        return this.definitionException.getMessage();
    }
}
