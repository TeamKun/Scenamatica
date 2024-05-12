package org.kunlab.scenamatica.commons.utils;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;

public class ActionMetaUtils
{
    @NotNull
    public static Action getActionMetaData(Class<? extends org.kunlab.scenamatica.interfaces.action.Action> actionClass)
    {
        Action meta = actionClass.getAnnotation(Action.class);
        if (meta == null)
            throw new IllegalArgumentException("Action class " + actionClass.getName() + " has no @ActionMeta annotation.");

        return meta;
    }

    @NotNull
    public static String getActionName(Class<? extends org.kunlab.scenamatica.interfaces.action.Action> actionClass)
    {
        return getActionMetaData(actionClass).value();
    }

    @NotNull
    public static String getActionName(org.kunlab.scenamatica.interfaces.action.Action action)
    {
        return getActionName(action.getClass());
    }
}
