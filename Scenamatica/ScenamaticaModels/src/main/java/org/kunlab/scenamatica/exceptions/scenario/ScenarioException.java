package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * シナリオに関する例外を表すクラスです。
 */
public class ScenarioException extends Exception
{
    @Getter
    @Nullable
    private final String scenarioName;

    public ScenarioException(@Nullable String scenarioName, String message)
    {
        super(message);
        this.scenarioName = scenarioName;
    }
}
