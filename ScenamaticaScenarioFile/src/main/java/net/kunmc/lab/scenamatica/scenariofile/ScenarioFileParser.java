package net.kunmc.lab.scenamatica.scenariofile;

import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@AllArgsConstructor
public class ScenarioFileParser
{

    public static ScenarioFileBean fromMap(Map<String, Object> map)
    {
        SchemaReplacer.resolveSchemaMap(map);
        return ScenarioFileBeanImpl.deserialize(map);
    }

    public static ScenarioFileBean fromInputStream(InputStream inputStream)
    {
        Yaml sYaml = new Yaml();
        Map<String, Object> map = sYaml.load(inputStream);
        return fromMap(map);
    }

    public static ScenarioFileBean fromJar(Path jarPath, Path scenarioFilePath) throws IOException
    {
        try (ZipFile zip = new ZipFile(jarPath.toFile()))  // .jar を .zip としてみなす。
        {
            ZipEntry entry = zip.getEntry(scenarioFilePath.toString());
            if (entry == null)
                throw new IllegalArgumentException("Scenario file not found in jar: " + scenarioFilePath);

            try (InputStream zis = zip.getInputStream(entry))
            {
                return fromInputStream(zis);
            }
        }
    }

    public static Map<String, ScenarioFileBean> loadAllFromJar(Path jarPath) throws IOException
    {
        Map<String, ScenarioFileBean> map = new HashMap<>();

        try (ZipFile zip = new ZipFile(jarPath.toFile()))  // .jar を .zip としてみなす。
        {
            Iterator<? extends ZipEntry> it = zip.stream().iterator();

            while (it.hasNext())
            {
                ZipEntry entry = it.next();
                if (entry.isDirectory())
                    continue;

                String fileName = entry.getName();
                if (!fileName.endsWith(".yml") && !fileName.endsWith(".yaml"))
                    continue;

                try (InputStream zis = zip.getInputStream(entry))
                {
                    ScenarioFileBean scenario = fromInputStream(zis);
                    map.put(scenario.getName(), scenario);
                }
                catch (IllegalArgumentException ignored)
                {
                }  // シナリオファイルではないときに発生するので, 握りつぶして良い。
            }
        }

        return map;
    }


}
