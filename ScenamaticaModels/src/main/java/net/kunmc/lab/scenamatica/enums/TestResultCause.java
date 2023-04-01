package net.kunmc.lab.scenamatica.enums;

public enum TestResultCause
{
    PASSED,

    CONTEXT_PREPARATION_FAILED,
    ACTION_EXECUTION_FAILED,
    ACTION_EXPECTATION_JUMPED,
    SCENARIO_TIMED_OUT,
    ILLEGAL_CONDITION,

    INTERNAL_ERROR,
    CANCELLED,
    SKIPPED;

    public boolean isFailure()
    {
        return this != PASSED && this != SKIPPED && this != CANCELLED;
    }

    public boolean isSkipped()
    {
        return this == SKIPPED;
    }

    public boolean isCancelled()
    {
        return this == CANCELLED;
    }
}
