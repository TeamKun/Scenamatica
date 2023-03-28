package net.kunmc.lab.scenamatica.enums;

public enum TestResultCause
{
    PASSED,

    CONTEXT_PREPARATION_FAILED,
    ACTION_EXECUTION_FAILED,
    ACTION_EXPECTATION_TIMED_OUT,
    ILLEGAL_CONDITION,

    INTERNAL_ERROR,
    CANCELLED,
    SKIPPED
}
