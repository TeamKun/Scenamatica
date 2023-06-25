package org.kunlab.scenamatica;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.ScenamaticaEnvironment;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.settings.ActorSettings;

import java.util.logging.Logger;

@Getter
@Builder
public class Environment implements ScenamaticaEnvironment
{
    private final Plugin plugin;
    private final Logger logger;
    private final ExceptionHandler exceptionHandler;
    private final TestReporter testReporter;
    private final ActorSettings actorSettings;
    private final boolean verbose;

    public static Environment.EnvironmentBuilder builder(Plugin plugin)
    {
        return new Environment.EnvironmentBuilder()
                .plugin(plugin)
                .logger(plugin.getLogger());
    }
}
