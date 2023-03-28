package net.kunmc.lab.scenamatica.interfaces.scenario.runtime;

public interface CompiledTriggerAction
{
    net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean getTrigger();

    java.util.List<CompiledScenarioAction<?>> getBeforeActions();

    java.util.List<CompiledScenarioAction<?>> getAfterActions();
}
