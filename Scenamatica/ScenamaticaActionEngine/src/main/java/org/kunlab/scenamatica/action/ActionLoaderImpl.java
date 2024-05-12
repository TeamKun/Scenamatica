package org.kunlab.scenamatica.action;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionLoader;
import org.kunlab.scenamatica.interfaces.action.LoadedAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActionLoaderImpl implements ActionLoader, Listener
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

    private final ScenamaticaRegistry registry;
    private final ExceptionHandler exceptionHandler;
    private final Logger logger;
    private final List<LoadedAction<?>> actions;
    private boolean isAlive;

    public ActionLoaderImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.exceptionHandler = registry.getExceptionHandler();
        this.logger = registry.getLogger();

        this.actions = new LinkedList<>();

        this.isAlive = true;
    }

    private static File getPluginFile(Plugin plugin)
    {
        try
        {
            return (File) pluginGetFile.invoke(plugin);
        }
        catch (ReflectiveOperationException var2)
        {
            throw new IllegalStateException(var2);
        }
    }

    private static boolean isConstructableActionClass(Class<?> clazz, Class<?> actionClazz)
    {
        if (clazz == null)
            return false;

        boolean isConstructable = !(clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation() || clazz.isPrimitive()
                || Modifier.isAbstract(clazz.getModifiers()));
        boolean isAction = actionClazz.isAssignableFrom(clazz);

        return isConstructable && isAction;
    }

    private static URL getJARURL(Plugin plugin)
    {
        File jarFile = getPluginFile(plugin);
        String jarFullPath = jarFile.getAbsolutePath();

        try
        {
            return new URL("jar:file:" + jarFullPath + "!/");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create URL for JAR file " + jarFullPath, e);
        }
    }

    private static boolean isCompatibleWithServer(Class<? extends org.kunlab.scenamatica.interfaces.action.Action> actionClass)
    {
        Action meta = ActionMetaUtils.getActionMetaData(actionClass);
        return MinecraftVersion.current().isInRange(meta.supportsSince(), meta.supportsUntil());
    }

    @Override
    public void init(@NotNull Plugin scenamatica)
    {
        Bukkit.getPluginManager().registerEvents(this, scenamatica);

        // 内部のアクションをロード
        this.loadActions(scenamatica);
    }

    @EventHandler
    public void onPluginEnabled(PluginEnableEvent event)
    {
        if (!this.isAlive || event.getPlugin().getName().equals(this.registry.getPlugin().getName()))
            return;
        this.loadActions(event.getPlugin());
    }

    @EventHandler
    public void onPluginDisabled(PluginDisableEvent event)
    {
        if (!this.isAlive || event.getPlugin().getName().equals(this.registry.getPlugin().getName()))
            return;
        this.unloadActions(event.getPlugin());
    }

    @Override
    public void shutdown()
    {
        this.isAlive = false;
    }

    @Override
    public void reloadActions()
    {
        this.unloadActionsInternal();
        this.loadActionsInternal();
    }

    @Override
    public void loadActions(Plugin plugin)
    {
        this.loadActionsInternal(plugin);
        this.registry.getScenarioManager().reloadPluginScenarios(plugin);
    }

    @Override
    public void unloadActions(Plugin plugin)
    {
        this.unloadActionsInternal(plugin);
    }

    private void loadActionsInternal()
    {
        this.logger.info("Loading actions, please wait...");

        List<Plugin> plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .filter(Plugin::isEnabled)
                .collect(Collectors.toList());

        // forEach は統合不可
        plugins.forEach(this::loadActionsInternal);

        plugins.forEach(this.registry.getScenarioManager()::reloadPluginScenarios);
    }

    @Override
    public <T extends org.kunlab.scenamatica.interfaces.action.Action> LoadedAction<T> getActionByName(@NotNull String name)
    {
        //noinspection unchecked
        return (LoadedAction<T>) this.actions.stream()
                .filter(action -> action.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public <T extends org.kunlab.scenamatica.interfaces.action.Action> LoadedAction<T> getActionByClass(@NotNull Class<T> clazz)
    {
        //noinspection unchecked
        return (LoadedAction<T>) this.actions.stream()
                .filter(action -> action.getActionClass() == clazz)
                .findFirst()
                .orElse(null);
    }

    private List<Class<? extends org.kunlab.scenamatica.interfaces.action.Action>> getActionClasses(Plugin plugin)
    {
        List<String> actionClassNames;
        try (ScenamaticaActionClassLoader cl = new ScenamaticaActionClassLoader(getJARURL(plugin)))
        {
            Collection<Class<?>> classes = cl.getAllClasses();
            Class<?> actionClass;
            if (cl.isScenamatica())
                actionClass = cl.getActionClass();
            else
                actionClass = org.kunlab.scenamatica.interfaces.action.Action.class;

            //noinspection unchecked
            actionClassNames = classes
                    .stream()
                    .filter(clazz -> isConstructableActionClass(clazz, actionClass))
                    .map(clazz -> (Class<? extends org.kunlab.scenamatica.interfaces.action.Action>) clazz)
                    .map(Class::getName)
                    .collect(Collectors.toList());

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // System.gc();

        ClassLoader cl = plugin.getClass().getClassLoader();
        List<Class<? extends org.kunlab.scenamatica.interfaces.action.Action>> actionClasses = new ArrayList<>();
        for (String actionClassName : actionClassNames)
        {
            try
            {
                //noinspection unchecked
                Class<? extends org.kunlab.scenamatica.interfaces.action.Action> actionClass = (Class<? extends org.kunlab.scenamatica.interfaces.action.Action>) cl.loadClass(actionClassName);
                if (!isCompatibleWithServer(actionClass))
                {
                    this.logger.log(Level.FINE, "Action " + actionClass.getName() + " is not compatible with the server version.");
                    continue;
                }

                actionClasses.add(actionClass);
            }
            catch (ClassNotFoundException e)
            {
                this.logger.warning("Failed to load class " + actionClassName + " from " + plugin.getName());
                this.exceptionHandler.report(e);
            }
        }

        return actionClasses;
    }

    private void loadActionsInternal(Plugin plugin)
    {
        List<Class<? extends org.kunlab.scenamatica.interfaces.action.Action>> actionClasses = this.getActionClasses(plugin);

        if (actionClasses.isEmpty())
            return;

        this.logger.info("Loading actions from " + plugin.getName() + "...");

        for (Class<? extends org.kunlab.scenamatica.interfaces.action.Action> actionClass : actionClasses)
        {
            org.kunlab.scenamatica.interfaces.action.Action constructedAction;
            try  // try を constructAction 内に移動させると, ネイティブクラスローダからの java.lang.NoClassDefFoundError の餌食になる。
            {
                constructedAction = this.constructAction(actionClass);
            }
            catch (ClassNotFoundException | NoClassDefFoundError e)
            {
                String eventPackage = "org.bukkit.event";
                String paperEventPackage = "io.papermc.paper.event";
                if (e.getMessage().contains(eventPackage.replace(".", "/"))
                        || e.getMessage().contains(paperEventPackage.replace(".", "/")))
                {
                    // 鯖のバージョンが低い
                    this.logger.info("Unable to load action " + actionClass.getName() + ", the server is not compatible with " + e.getMessage() + ".");
                    continue;
                }

                this.logger.warning("Failed to construct action " + actionClass.getName());
                this.exceptionHandler.report(e);
                continue;
            }
            catch (Throwable e)
            {
                this.logger.warning("Failed to construct action " + actionClass.getName());
                this.exceptionHandler.report(e);
                continue;
            }

            LoadedAction<?> loadedAction = LoadedActionImpl.of(plugin, constructedAction);
            this.actions.add(loadedAction);
        }
    }

    private org.kunlab.scenamatica.interfaces.action.Action constructAction(Class<? extends org.kunlab.scenamatica.interfaces.action.Action> actionClass) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Constructor<?>[] constructor = actionClass.getConstructors();
        for (Constructor<?> c : constructor)
        {
            if (c.getParameterCount() == 0)
                return (org.kunlab.scenamatica.interfaces.action.Action) c.newInstance();
            else if (c.getParameterCount() == 1 && c.getParameterTypes()[0].equals(ScenamaticaRegistry.class))
                return (org.kunlab.scenamatica.interfaces.action.Action) c.newInstance(this.registry);
        }

        this.logger.warning("Failed to construct action " + actionClass.getName() + ", no suitable constructor found.");

        return null;
    }

    private void unloadActionsInternal()
    {
        List<LoadedAction<?>> actionsByPlugin = new LinkedList<>(this.actions);
        actionsByPlugin.forEach(this::onUnloadAction);
        this.actions.clear();
    }

    private void unloadActionsInternal(Plugin plugin)
    {
        List<LoadedAction<?>> actionsByPlugin = new LinkedList<>(this.actions);
        actionsByPlugin.stream()
                .filter(action -> action.getOwner().equals(plugin))
                .forEach(this::onUnloadAction);
        this.actions.removeIf(action -> action.getOwner().equals(plugin));
    }

    private void onUnloadAction(@NotNull LoadedAction<?> action)
    {
        if (this.isAlive)
            return;

        List<ScenarioEngine> enginesByPlugin = this.registry.getScenarioManager().getEnginesFor(action.getOwner());
        for (ScenarioEngine engine : enginesByPlugin)
        {
            List<CompiledScenarioAction> actions = engine.getActions();
            boolean isActionToBeRemoveUsed = actions.stream()
                    .anyMatch(compiledAction -> ActionMetaUtils.getActionMetaData(compiledAction.getAction().getExecutor().getClass()).value().equals(action.getName()));
            if (isActionToBeRemoveUsed)
            {
                this.logger.warning("Action " + action.getName() + " is used in scenario " + engine.getScenario().getName() + ", the engine will be stopped and unloaded.");
                this.registry.getScenarioManager().invalidate(engine);
                return;
            }
        }
    }

    @Getter
    private class ScenamaticaActionClassLoader extends URLClassLoader
    {
        private boolean isScenamatica;
        private Class<?> actionClass;

        public ScenamaticaActionClassLoader(URL url)
        {
            super(new URL[]{url});
        }

        private File jarURLToFile(URL url)
        {
            String path = url.getPath();
            if (path.endsWith("!/"))
                path = path.substring(0, path.length() - 2);
            if (path.startsWith("file:"))
                path = path.substring(5);


            return Paths.get(path).toFile();

        }

        public Collection<Class<?>> getAllClasses()
        {
            URL[] urls = this.getURLs();
            URL pluginURL = urls[0];

            Collection<Class<?>> classes = new ArrayList<>();
            try (JarFile jar = new JarFile(this.jarURLToFile(pluginURL)))
            {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (!entryName.endsWith(".class")
                            || entryName.startsWith("META-INF")
                            || entryName.endsWith("package-info.class")
                            || entryName.endsWith("module-info.class"))
                        continue;

                    String className = entry.getName().replace('/', '.').replaceAll("\\.class$", "");
                    Class<?> clazz;
                    try
                    {
                        clazz = this.loadClass(className);
                    }
                    catch (ClassNotFoundException | NoClassDefFoundError ignored)
                    {
                        ActionLoaderImpl.this.logger.log(Level.FINE, "Failed to load class " + className + " from " + pluginURL.getPath());
                        continue;
                    }

                    if (clazz.getName().equals(org.kunlab.scenamatica.interfaces.action.Action.class.getName()))
                    {
                        this.isScenamatica = true;
                        this.actionClass = clazz;
                        continue;
                    }
                    classes.add(clazz);
                }
            }
            catch (IOException e)
            {
                ActionLoaderImpl.this.logger.warning("Failed to load classes from " + pluginURL.getPath());
                ActionLoaderImpl.this.exceptionHandler.report(e);
            }

            return classes;
        }
    }
}
