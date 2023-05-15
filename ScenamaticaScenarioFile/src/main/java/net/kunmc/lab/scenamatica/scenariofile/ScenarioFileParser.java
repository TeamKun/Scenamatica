package net.kunmc.lab.scenamatica.scenariofile;

import lombok.*;
import net.kunmc.lab.scenamatica.exceptions.scenariofile.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.*;
import org.yaml.snakeyaml.*;

import javax.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

@AllArgsConstructor
public class ScenarioFileParser
{

    public static ScenarioFileBean fromMap(Map<String, Object> map, @Nullable String fileName)
            throws InvalidScenarioFileException
    {
        try
        {
            SchemaReplacer.resolveDefinitionMap(map);
            if (!map.containsKey("scenamatica"))
                throw new NotAScenarioFileException(fileName);

            return ScenarioFileBeanImpl.deserialize(map);
        }
        catch (IllegalArgumentException e)
        {
            String fileNameStr = fileName == null ? "": " in file " + fileName;

            throw new InvalidScenarioFileException(e.getMessage() + fileNameStr, e);
        }
    }

    public static ScenarioFileBean fromInputStream(InputStream inputStream, @Nullable String fileName)
            throws InvalidScenarioFileException
    {
        Yaml sYaml = new Yaml(new SimpleYamlConstructor());
        Map<String, Object> map = sYaml.load(inputStream);
        return fromMap(map, fileName);
    }

    public static ScenarioFileBean fromJar(Path jarPath, Path scenarioFilePath)
            throws IOException, InvalidScenarioFileException
    {
        try (ZipFile zip = new ZipFile(jarPath.toFile()))  // .jar を .zip としてみなす。
        {
            ZipEntry entry = zip.getEntry(scenarioFilePath.toString());
            if (entry == null)
                throw new FileNotFoundException("Scenario file not found in jar: " + scenarioFilePath);

            try (InputStream zis = zip.getInputStream(entry))
            {
                return fromInputStream(zis, scenarioFilePath.toString());
            }
        }
    }

    public static Map<String, ScenarioFileBean> loadAllFromJar(Path jarPath)
            throws IOException, InvalidScenarioFileException
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
                    ScenarioFileBean scenario = fromInputStream(zis, fileName);
                    if (map.containsKey(scenario.getName()))
                        throw new IllegalStateException(String.format(
                                "Duplicated scenario name: %s(%s, %s)",
                                scenario.getName(),
                                map.get(scenario.getName()).getDescription(),
                                scenario.getDescription()
                        ));

                    map.put(scenario.getName(), scenario);
                }
                catch (NotAScenarioFileException ignored)
                {
                } // plugin.yml などのファイルが混じっているので無視する。
            }
        }

        return map;
    }


}
