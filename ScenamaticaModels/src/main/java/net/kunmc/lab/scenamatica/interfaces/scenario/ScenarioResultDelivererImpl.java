package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TestState;
import org.jetbrains.annotations.NotNull;

public interface ScenarioResultDelivererImpl
{
    void setResult(TestResult result);

    TestResult waitResult(@NotNull TestState state);

    void kill();
}
