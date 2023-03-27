package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
@AllArgsConstructor
public class TestResult
{
    @NotNull
    UUID testID;
    @NotNull
    TestState state;
    @NotNull
    ResultType resultType;
    @NotNull
    String message;
    long startedAt;
    long finishedAt;

    @Nullable
    Action<?> failedAction;

    public TestResult(@NotNull UUID testID,
                      @NotNull TestState state,
                      @NotNull ResultType resultType,
                      @NotNull String message,
                      long startedAt)
    {
        this.testID = testID;
        this.state = state;
        this.resultType = resultType;
        this.message = message;
        this.startedAt = startedAt;
        this.finishedAt = System.currentTimeMillis();
        this.failedAction = null;
    }

    public TestResult(@NotNull UUID testID,
                      @NotNull TestState state,
                      @NotNull ResultType resultType,
                      @NotNull String message,
                      long startedAt,
                      @NotNull Action<?> failedAction)
    {
        this.testID = testID;
        this.state = state;
        this.resultType = resultType;
        this.message = message;
        this.startedAt = startedAt;
        this.finishedAt = System.currentTimeMillis();
        this.failedAction = failedAction;
    }

    enum ResultType
    {
        PASSED,

        CONTEXT_PREPARATION_FAILED,
        ACTION_EXECUTION_FAILED,
        ACTION_EXPECTATION_TIMED_OUT,
        ILLEGAL_CONDITION,
    }
}
