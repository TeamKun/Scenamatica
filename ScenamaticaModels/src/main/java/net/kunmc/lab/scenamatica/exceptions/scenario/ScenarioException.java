package net.kunmc.lab.scenamatica.exceptions.scenario;

import lombok.*;
import org.jetbrains.annotations.*;

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
