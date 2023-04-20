package net.kunmc.lab.scenamatica.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import net.kunmc.lab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.utils.PluginUtil;

import java.nio.file.Path;
import java.util.Collection;
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

            this.validateScenarios(pluginScenarios.values());

            this.scenarios.put(plugin.getName(), pluginScenarios);

            return true;
        }
        catch (InvalidScenarioFileException e)
        {
            this.registry.getLogger().warning("The plugin " + plugin.getName() + " has invalid scenario files: " + e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            return false;
        }
    }

    private void validateScenarios(Collection<? extends ScenarioFileBean> scenarios)
            throws InvalidScenarioFileException
    {
        Version baseVersion = Version.of(this.registry.getPlugin().getDescription().getVersion());

        for (ScenarioFileBean scenario : scenarios)
        {
            Version fileVersion = scenario.getScenamaticaVersion();

            // このScenamatica より新しいバージョンのファイルは読み込まない
            if (fileVersion.isNewerThan(baseVersion))
                throw new InvalidScenarioFileException(
                        "The scenario file " + scenario.getName() + " is newer than running Scenamatica daemon version." +
                                " (File version: " + fileVersion + ", Scenamatica version: " + baseVersion + ")");
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
