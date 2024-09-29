package org.kunlab.scenamatica.scenariofile;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileManager;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScenarioFileManagerImpl implements ScenarioFileManager
{
    private static final Method pluginGetFile;

    static
    {
        try
        {
            pluginGetFile = JavaPlugin.class.getDeclaredMethod("getFile");
            pluginGetFile.setAccessible(true);
        }
        catch (NoSuchMethodException var1)
        {
            throw new IllegalStateException(var1);
        }
    }

    @NotNull
    private final ScenamaticaRegistry registry;
    @NotNull
    private final Map<String, Map<String, ScenarioFileStructure>> scenarios;

    public ScenarioFileManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.scenarios = new HashMap<>();
    }

    private static Path getPluginFilePath(Plugin plugin)
    {
        try
        {
            return ((File) pluginGetFile.invoke(plugin)).toPath();
        }
        catch (ReflectiveOperationException var2)
        {
            throw new IllegalStateException(var2);
        }
    }

    @Override
    public @NotNull StructureSerializer getSerializer()
    {
        return StructureSerializerImpl.getInstance();
    }

    @Override
    public boolean loadPluginScenarios(@NotNull Plugin plugin)
    {
        boolean result;
        try
        {
            Path pluginJarPath = getPluginFilePath(plugin);
            Map<String, ScenarioFileStructure> pluginScenarios = ScenarioFileParser.loadAllFromJar(pluginJarPath);

            pluginScenarios.values().removeIf(scenario -> !this.canLoadScenario(scenario));

            this.scenarios.put(plugin.getName(), pluginScenarios);

            result = true;
        }
        catch (InvalidScenarioFileException e)
        {
            String message = e.getMessage();
            if (!(e.getCause() == null || Objects.equals(e.getMessage(), e.getCause().getMessage())))
                message += "(caused by " + e.getCause().getMessage() + ")";

            this.registry.getLogger().warning(LangProvider.get(
                    "scenario.file.parser.invalidScenarioFile",
                    MsgArgs.of("pluginName", plugin.getName())
                            .add("fileName", e.getFileName())
                            .add("message", message)
            ));
            result = false;
        }
        catch (IOException e)
        {
            this.registry.getExceptionHandler().report(e);
            result = false;
        }

        if (!result)
        {
            this.registry.getLogger().warning(LangProvider.get(
                    "scenario.file.manager.pluginLoadingFailed",
                    MsgArgs.of("pluginName", plugin.getName())
            ));
        }

        return result;
    }

    private boolean canLoadScenario(@NotNull ScenarioFileStructure scenario)
    {
        Version baseVersion = Version.of(this.registry.getPlugin().getDescription().getVersion());
        Version fileVersion = scenario.getScenamaticaVersion();
        if (scenario.getScenamaticaVersion().isNewerThan(baseVersion))
        {
            this.registry.getLogger().warning(LangProvider.get(
                    "scenario.file.parser.versionMismatch",
                    MsgArgs.of("scenarioName", scenario.getName())
                            .add("currentScenamaticaVersion", fileVersion)
                            .add("fileScenamaticaVersion", baseVersion)
            ));
            return false;
        }

        return true;
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
