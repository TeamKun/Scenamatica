package org.kunlab.scenamatica.enums;

/**
 * マイルストーンのスコープを表す列挙型です。
 */
public enum MilestoneScope
{
    SCENARIO_GLOBAL,

    TRIGGER_BEFORE,
    TRIGGER_AFTER,

    MAIN_SCENARIO;

    public static MilestoneScope fromState(ScenarioState state)
    {
        switch (state)
        {
            case RUNNING_BEFORE:
                return TRIGGER_BEFORE;
            case RUNNING_MAIN:
                return MAIN_SCENARIO;
            case RUNNING_AFTER:
                return TRIGGER_AFTER;
            default:
                return SCENARIO_GLOBAL;
        }
    }
}
