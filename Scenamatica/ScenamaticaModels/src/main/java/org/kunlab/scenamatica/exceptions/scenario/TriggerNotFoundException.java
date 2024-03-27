package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.kunlab.scenamatica.enums.TriggerType;

/**
 * 指定されたシナリオのトリガが見つかれない場合にスローされる例外です。
 */
public class TriggerNotFoundException extends ScenarioException
{
    @Getter
    private final TriggerType trigger;

    public TriggerNotFoundException(String scenario, TriggerType trigger)
    {
        super(scenario, "Scenario trigger not found: " + trigger);
        this.trigger = trigger;
    }
}
