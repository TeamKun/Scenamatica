package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

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
    ACTION_EXPECT("expect", Watchable.class),
    /**
     * アクションを実行します。
     */
    ACTION_EXECUTE("execute", null),
    /**
     * 条件を**既に**満たしていることを期待します。
     */
    CONDITION_REQUIRE("require", Requireable.class),

    ;

    @NotNull
    private final String key;
    private final Class<?> markerInterface;

    public static ScenarioType fromKey(String key)
    {
        for (ScenarioType type : values())
            if (type.getKey().equals(key))
                return type;

        return null;
    }

    public boolean canPerformActionInType(@NotNull @SuppressWarnings("rawtypes") Class<? extends Action> clazz)
    {
        return this.markerInterface == null || this.markerInterface.isAssignableFrom(clazz);
    }

    public void validatePerformableActionType(@NotNull @SuppressWarnings("rawtypes") Class<? extends Action> clazz)
    {
        if (!this.canPerformActionInType(clazz))
            throw new IllegalArgumentException("Action type " + clazz.getName() + " cannot be performed in scenario type " + this.name());
    }
}
