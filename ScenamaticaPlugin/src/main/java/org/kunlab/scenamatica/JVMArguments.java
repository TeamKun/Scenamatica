package org.kunlab.scenamatica;

import org.jetbrains.annotations.NotNull;

public class JVMArguments
{
    public static final String KEY_ROOT = "org.kunlab.scenamatica.";

    public static final String KEY_IGNORE_TRIGGER_TYPES = KEY_ROOT + "trigger.ignores";
    public static final String IGNORE_TRIGGER_DELIMITER = ",";

    @NotNull
    public static String[] getIgnoreTriggerTypes()
    {
        String value = System.getProperty(KEY_IGNORE_TRIGGER_TYPES);
        if (value == null)
            return new String[0];

        return value.split(IGNORE_TRIGGER_DELIMITER);
    }
}
