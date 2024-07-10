package org.kunlab.scenamatica.action;

import lombok.Value;
import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.LoadedAction;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

@Value
public class LoadedActionImpl<T extends org.kunlab.scenamatica.interfaces.action.Action> implements LoadedAction<T>
{
    Plugin owner;
    String name;

    boolean executable;
    boolean expectable;
    boolean requireable;

    MinecraftVersion availableSince;
    MinecraftVersion availableUntil;

    T instance;
    Class<? extends T> actionClass;

    private LoadedActionImpl(Plugin owner, T instance, Class<? extends T> actionClass)
    {
        Action meta = ActionMetaUtils.getActionMetaData(actionClass);

        this.owner = owner;
        this.name = meta.value();
        this.availableSince = meta.supportsSince();
        this.availableUntil = meta.supportsUntil();

        this.executable = instance instanceof Executable;
        this.expectable = instance instanceof Expectable;
        this.requireable = instance instanceof Requireable;
        this.instance = instance;
        this.actionClass = actionClass;

        if (!(this.executable || this.expectable || this.requireable))
            throw new IllegalArgumentException("Action " + this.name + " is not executable, expectable, or requireable, cannot be used.");
    }

    public static LoadedAction<?> of(Plugin owner, org.kunlab.scenamatica.interfaces.action.Action action)
    {
        return new LoadedActionImpl<>(
                owner,
                action,
                action.getClass()
        );
    }

    @Override
    public Executable asExecutable()
    {
        if (!this.executable)
            throw new IllegalStateException("Action " + this.name + " is not executable.");
        return (Executable) this.instance;
    }

    @Override
    public Expectable asExpectable()
    {
        if (!this.expectable)
            throw new IllegalStateException("Action " + this.name + " is not watchable.");
        return (Expectable) this.instance;
    }

    @Override
    public Requireable asRequireable()
    {
        if (!this.requireable)
            throw new IllegalStateException("Action " + this.name + " is not requireable.");
        return (Requireable) this.instance;
    }
}
