package net.kunmc.lab.scenamatica.exceptions.scenario;

import net.kunmc.lab.scenamatica.enums.TriggerType;

/**
 * 指定されたシナリオのトリガが見つかれない場合にスローされる例外です。
 */
public class TriggerNotFoundException extends ScenarioException
{
    private final TriggerType trigger;

    public TriggerNotFoundException(TriggerType trigger)
    {
        super("Scenario trigger not found: " + trigger);
        this.trigger = trigger;
    }
}
