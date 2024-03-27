package org.kunlab.scenamatica.enums;

public enum ScenarioResultCause
{
    PASSED,

    CONTEXT_PREPARATION_FAILED,
    ACTION_EXECUTION_FAILED,
    ACTION_EXPECTATION_JUMPED,
    SCENARIO_TIMED_OUT,
    ILLEGAL_CONDITION,

    RUN_TIMED_OUT,
    INTERNAL_ERROR,
    CANCELLED,
    SKIPPED;

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
