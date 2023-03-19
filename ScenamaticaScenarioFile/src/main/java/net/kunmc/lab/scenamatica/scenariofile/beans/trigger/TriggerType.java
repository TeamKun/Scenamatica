package net.kunmc.lab.scenamatica.scenariofile.beans.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * シナリオのトリガーの種類を表す列挙型です。
 */
@Getter
@AllArgsConstructor
public enum TriggerType
{
    MANUAL_DISPATCH("manual_dispatch"),
    ON_ACTION("action"),
    SCHEDULE("schedule"),

    ;

    private final String key;

    public static TriggerType fromKey(String key)
    {
        for (TriggerType type : values())
            if (type.getKey().equals(key))
                return type;

        return null;
    }
}
