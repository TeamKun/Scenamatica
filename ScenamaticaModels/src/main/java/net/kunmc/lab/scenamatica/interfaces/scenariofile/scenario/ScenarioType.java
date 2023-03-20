package net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * シナリオの種類です。
 */
@Getter
@AllArgsConstructor
public enum ScenarioType
{
    /**
     * アクションが起きることを期待し, 起きなかった場合は失敗とします。
     */
    ACTION_EXPECT("expect"),
    /**
     * アクションを実行します。
     */
    ACTION_EXECUTE("execute"),
    /**
     * 条件を**既に**満たしていることを期待します。
     */
    CONDITION_REQUIRE("require"),

    ;

    private final String key;

    public static ScenarioType fromKey(String key)
    {
        for (ScenarioType type : values())
            if (type.getKey().equals(key))
                return type;

        return null;
    }
}
