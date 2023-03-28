package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.action.Action;

import java.util.UUID;

public interface TestResult
{
    UUID getTestID();

    TestState getState();

    TestResultCause getTestResultCause();

    String getMessage();

    long getStartedAt();

    long getFinishedAt();

    Action<?> getFailedAction();
}
