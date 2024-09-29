package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

/**
 * シナリオの種類です。
 */
@Getter
@AllArgsConstructor
public enum ScenarioType
{
    /**
     * アクションを実行します。
     */
    ACTION_EXECUTE("execute", Executable.class),
    /**
     * アクションが起きることを期待し, 起きなかった場合は失敗とします。
     */
    ACTION_EXPECT("expect", Expectable.class),
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

    public boolean canPerformActionInType(@NotNull Class<? extends Action> clazz)
    {
        return this.markerInterface.isAssignableFrom(clazz);
    }

    public enum $Doc
    {
        execute,
        expect,
        require,
    }
}
