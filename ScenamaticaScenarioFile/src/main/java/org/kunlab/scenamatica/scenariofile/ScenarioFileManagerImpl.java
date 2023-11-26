package org.kunlab.scenamatica.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.utils.PluginUtil;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScenarioFileManagerImpl implements ScenarioFileManager
{
    @NotNull
    private final ScenamaticaRegistry registry;
    @NotNull
    private final Map<String, Map<String, ScenarioFileStructure>> scenarios;

    public ScenarioFileManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.scenarios = new HashMap<>();
    }

    @Override
    public @NotNull StructureSerializer getSerializer()
    {
        return StructureSerializerImpl.getInstance();
    }

    @Override
    public boolean loadPluginScenarios(@NotNull Plugin plugin)
    {
        try
        {
            Path pluginJarPath = PluginUtil.getFile(plugin).toPath();
            Map<String, ScenarioFileStructure> pluginScenarios = ScenarioFileParser.loadAllFromJar(pluginJarPath);

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

    private void validateScenarios(Collection<? extends ScenarioFileStructure> scenarios)
            throws InvalidScenarioFileException
    {
        Version baseVersion = Version.of(this.registry.getPlugin().getDescription().getVersion());

        for (ScenarioFileStructure scenario : scenarios)
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
    public Map<String, ScenarioFileStructure> getPluginScenarios(@NotNull Plugin plugin)
    {
        if (!(this.scenarios.containsKey(plugin.getName()) || this.loadPluginScenarios(plugin)))
            return null;

        return this.scenarios.get(plugin.getName());
    }

    @Override
    @Nullable
    public ScenarioFileStructure getScenario(@NotNull Plugin plugin, @NotNull String scenarioName)
    {
        Map<String, ScenarioFileStructure> pluginScenarios = this.getPluginScenarios(plugin);

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
