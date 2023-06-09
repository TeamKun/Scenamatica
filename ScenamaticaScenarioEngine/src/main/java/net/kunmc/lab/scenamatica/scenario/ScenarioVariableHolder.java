package net.kunmc.lab.scenamatica.scenario;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScenarioVariableHolder
{
    private final ScenamaticaRegistry registry;
    private final BeanSerializer serializer;
    private final Map<String, Object> values;

    public ScenarioVariableHolder(@NotNull ScenarioEngine engine)
    {
        this.registry = engine.getManager().getRegistry();
        this.serializer = this.registry.getScenarioFileManager().getSerializer();

        this.values = new HashMap<>();
    }

    private Map<String, Object> selectMap(String path)
    {
        Object obj = this.select(path);
        if (!(obj instanceof Map))
            throw new IllegalStateException("The path " + path + " is not a map.");

        // noinspection unchecked
        return (Map<String, Object>) obj;
    }

    private Object select(String path)
    {
        String[] paths = path.split("\\.");

        Map<String, Object> current = this.values;
        for (int i = 0; i < paths.length; i++)
        {
            String currentPath = paths[i];

            if (!current.containsKey(currentPath))
            {
                Map<String, Object> newMap = new ConcurrentHashMap<>();
                current.put(currentPath, newMap);
                current = newMap;
                continue;
            }

            Object obj = current.get(currentPath);
            if (i == paths.length - 1)
                return obj;

            // 末端でなく、かつ、Mapでない場合は取得できないので。
            if (!(obj instanceof Map))
                throw new IllegalStateException("The path " + currentPath + " is not a map.");
            // noinspection unchecked
            current = (Map<String, Object>) obj;
        }

        return current;  // 末端が Map だった場合。
    }

    public void put(String path, ScenarioEngine engine)
    {
        Map<String, Object> parent = this.selectMap(path);
        parent.putAll(engine.serialize());
    }

    public void put(String path, ScenarioFileBean bean)
    {
        Map<String, Object> parent = this.selectMap(path);
        parent.putAll(this.serializer.serializeScenarioFile(bean));
    }

    public void put(String path, Object value)
    {
        String[] paths = path.split("\\.");
        if (paths.length == 0)
            throw new IllegalArgumentException("The path is empty.");

        // 末尾を除いたパスを連結
        String[] parentPaths = new String[paths.length - 1];
        System.arraycopy(paths, 0, parentPaths, 0, parentPaths.length);
        String parentPath = String.join(".", parentPaths);

        // 末尾以外のキーより、親のMapを取得して、末尾のキーと値を追加
        Map<String, Object> parent = this.selectMap(parentPath);
        parent.put(paths[paths.length - 1], value);
    }
}
