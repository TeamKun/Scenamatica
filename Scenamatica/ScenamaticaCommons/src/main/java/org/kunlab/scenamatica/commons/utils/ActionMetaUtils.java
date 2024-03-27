package org.kunlab.scenamatica.commons.utils;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.interfaces.action.Action;

public class ActionMetaUtils
{
    @NotNull
    public static ActionMeta getActionMetaData(Class<? extends Action> actionClass)
    {
        ActionMeta meta = actionClass.getAnnotation(ActionMeta.class);
        if (meta == null)
            throw new IllegalArgumentException("Action class " + actionClass.getName() + " has no @ActionMeta annotation.");

        return meta;
    }

    @NotNull
    public static String getActionName(Class<? extends Action> actionClass)
    {
        return getActionMetaData(actionClass).value();
    }

    @NotNull
    public static String getActionName(Action action)
    {
        return getActionName(action.getClass());
    }
}
