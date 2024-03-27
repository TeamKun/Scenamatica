package org.kunlab.scenamatica.action;

import lombok.Getter;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionLoader;
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;

@Getter
public class ActionManagerImpl implements ActionManager
{
    private final ScenamaticaRegistry registry;

    private final ActionRunManager runManager;
    private final ActionLoader loader;
    private final ActionCompiler compiler;

    public ActionManagerImpl(ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.runManager = new ActionRunManagerImpl(registry.getTriggerManager(), registry.getExceptionHandler());
        this.loader = new ActionLoaderImpl(registry);
        this.compiler = new ActionCompilerImpl(this.loader);
    }

    @Override
    public void init()
    {
        this.runManager.init(this.registry.getPlugin());
        this.loader.init(this.registry.getPlugin());
    }

    @Override
    public void shutdown()
    {
        this.runManager.shutdown();
        this.loader.shutdown();
    }
}
