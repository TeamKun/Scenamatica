package net.kunmc.lab.scenamatica.exceptions.scenario;

import lombok.*;
import net.kunmc.lab.scenamatica.enums.*;

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
