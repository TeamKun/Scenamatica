package org.kunlab.scenamatica.action;

import lombok.Value;
import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.LoadedAction;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

@Value
public class LoadedActionImpl<T extends Action> implements LoadedAction<T>
{
    Plugin owner;
    String name;

    boolean executable;
    boolean watchable;
    boolean requireable;

    MinecraftVersion availableSince;
    MinecraftVersion availableUntil;

    T instance;
    Class<? extends T> actionClass;

    private LoadedActionImpl(Plugin owner, T instance, Class<? extends T> actionClass)
    {
        ActionMeta meta = ActionMetaUtils.getActionMetaData(actionClass);

        this.owner = owner;
        this.name = meta.value();
        this.availableSince = meta.supportsSince();
        this.availableUntil = meta.supportsUntil();

        this.executable = instance instanceof Executable;
        this.watchable = instance instanceof Watchable;
        this.requireable = instance instanceof Requireable;
        this.instance = instance;
        this.actionClass = actionClass;

        if (!(this.executable || this.watchable || this.requireable))
            throw new IllegalArgumentException("Action " + this.name + " is not executable, watchable, or requireable, cannot be used.");
    }

    public static LoadedAction<?> of(Plugin owner, Action action)
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
    public Watchable asWatchable()
    {
        if (!this.watchable)
            throw new IllegalStateException("Action " + this.name + " is not watchable.");
        return (Watchable) this.instance;
    }

    @Override
    public Requireable asRequireable()
    {
        if (!this.requireable)
            throw new IllegalStateException("Action " + this.name + " is not requireable.");
        return (Requireable) this.instance;
    }
}
