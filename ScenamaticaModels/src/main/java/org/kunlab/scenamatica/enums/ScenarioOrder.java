package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ScenarioOrder
{
    FIRST(Integer.MIN_VALUE),
    HIGHEST(-10000),
    HIGHER(-1000),
    HIGH(-100),
    NORMAL(0),
    LOW(100),
    LOWER(1000),
    LOWEST(10000),
    LAST(Integer.MAX_VALUE);

    private final int order;

    public static ScenarioOrder of(String name)
    {
        for (ScenarioOrder order : values())
        {
            if (order.name().equalsIgnoreCase(name))
                return order;
        }

        return null;
    }
}
