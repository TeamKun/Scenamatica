package net.kunmc.lab.scenamatica;

import lombok.Builder;
import lombok.Getter;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaEnvironment;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

@Getter
@Builder
public class Environment implements ScenamaticaEnvironment
{
    private final Plugin plugin;
    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

    public static Environment.EnvironmentBuilder builder(Plugin plugin)
    {
        return new Environment.EnvironmentBuilder()
                .plugin(plugin)
                .logger(plugin.getLogger());
    }
}
