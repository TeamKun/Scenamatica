package net.kunmc.lab.scenamatica.exceptions.scenario;

import lombok.*;

/**
 * シナリオが見つからないことを表す例外です。
 */
@Getter
public class ScenarioNotFoundException extends ScenarioException
{
    public ScenarioNotFoundException(String scenarioName)
    {
        super(scenarioName, "Scenario " + scenarioName + " is not found.");
    }
}
