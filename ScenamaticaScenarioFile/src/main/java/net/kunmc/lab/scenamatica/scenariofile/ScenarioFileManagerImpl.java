package net.kunmc.lab.scenamatica.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.*;
import net.kunmc.lab.scenamatica.exceptions.scenariofile.*;
import net.kunmc.lab.scenamatica.interfaces.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.*;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.*;
import org.kunlab.kpm.utils.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
        catch (IOException e)
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

            // この Scenamatica より新しいバージョンのファイルは読み込まない
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
