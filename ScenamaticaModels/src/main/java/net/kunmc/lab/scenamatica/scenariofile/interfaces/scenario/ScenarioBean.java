package net.kunmc.lab.scenamatica.scenariofile.interfaces.scenario;

import java.io.Serializable;

public interface ScenarioBean extends Serializable
{
    String KEY_TIMEOUT = "timeout";

    ScenarioType getType();

    ActionBean getAction();

    long getTimeout();
}
