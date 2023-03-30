package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
@AllArgsConstructor
public class TestResultImpl implements TestResult
{
    @NotNull
    UUID testID;
    @NotNull
    TestState state;
    @NotNull
    TestResultCause testResultCause;
    long startedAt;
    long finishedAt;

    @Nullable
    Action<?> failedAction;

    public TestResultImpl(@NotNull UUID testID,
                          @NotNull TestState state,
                          @NotNull TestResultCause resultType,
                          long startedAt)
    {
        this.testID = testID;
        this.state = state;
        this.testResultCause = resultType;
        this.startedAt = startedAt;
        this.finishedAt = System.currentTimeMillis();
        this.failedAction = null;
    }
}
