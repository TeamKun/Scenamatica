package net.kunmc.lab.scenamatica.scenariofile;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.utils.PluginUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ScenarioFileManagerImpl implements ScenarioFileManager
{
    @NotNull
    private final ScenamaticaRegistry registry;
    @NotNull
    private final Map<String, Map<String, ScenarioFileBean>> scenarios;

    public ScenarioFileManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.scenarios = new HashMap<>();
    }

    @Override
    public boolean loadPluginScenarios(@NotNull Plugin plugin)
    {
        try
        {
            Path pluginJarPath = PluginUtil.getFile(plugin).toPath();
            Map<String, ScenarioFileBean> pluginScenarios = ScenarioFileParser.loadAllFromJar(pluginJarPath);

            this.scenarios.put(plugin.getName(), pluginScenarios);

            return true;
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            return false;
        }
    }

    @Override
    @Nullable
    public Map<String, ScenarioFileBean> getPluginScenarios(@NotNull Plugin plugin)
    {
        if (!(this.scenarios.containsKey(plugin.getName()) || this.loadPluginScenarios(plugin)))
            return null;

        return this.scenarios.get(plugin.getName());
    }

    @Override
    @Nullable
    public ScenarioFileBean getScenario(@NotNull Plugin plugin, @NotNull String scenarioName)
    {
        Map<String, ScenarioFileBean> pluginScenarios = this.getPluginScenarios(plugin);

        if (pluginScenarios == null)
            return null;

        return pluginScenarios.get(scenarioName);
    }

    @Override
    public void unloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.scenarios.remove(plugin.getName());
    }

    @Override
    public boolean reloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.unloadPluginScenarios(plugin);
        return this.loadPluginScenarios(plugin);
    }
}
