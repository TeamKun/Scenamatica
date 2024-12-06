package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public enum ScenarioResultCause
{
    PASSED,

    CONTEXT_PREPARATION_FAILED,
    ACTION_EXECUTION_FAILED(ActionResultCause.EXECUTION_FAILED),
    ACTION_EXPECTATION_JUMPED(ActionResultCause.EXECUTION_JUMPED),
    SCENARIO_TIMED_OUT(ActionResultCause.TIMED_OUT),
    ILLEGAL_CONDITION(ActionResultCause.UNEXPECTED_CONDITION),
    UNRESOLVED_REFERENCES(ActionResultCause.UNRESOLVED_REFERENCES),

    RUN_TIMED_OUT,  // これは, 全体のタイムアウトの方。 <=> SCENARIO_TIMED_OUT はシナリオのタイムアウト
    INTERNAL_ERROR(ActionResultCause.INTERNAL_ERROR),
    CANCELLED,
    SKIPPED;

    @Nullable
    private final ActionResultCause actionCause;

    public boolean isOK()
    {
        return this == PASSED || this == SKIPPED || this == CANCELLED;
    }

    public boolean isFailure()
    {
        return !this.isOK();
    }

    public boolean isSkipped()
    {
        return this == SKIPPED;
    }

    public boolean isCancelled()
    {
        return this == CANCELLED;
    }

    public boolean isError()
    {
        return this == INTERNAL_ERROR;
    }
}
