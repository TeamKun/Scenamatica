package net.kunmc.lab.scenamatica.scenariofile;

import lombok.*;
import net.kunmc.lab.scenamatica.exceptions.scenariofile.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.*;
import org.apache.commons.lang.*;
import org.yaml.snakeyaml.*;

import javax.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

@AllArgsConstructor
public class ScenarioFileParser
{
    private static final String[] SCENARIO_FILE_EXTENSIONS = {
            ".yml",
            ".yaml"
    };

    public static ScenarioFileBean fromMap(Map<String, Object> map, @Nullable String fileName)
            throws InvalidScenarioFileException
    {
        try
        {
            DefinitionsMapper.resolveReferences(map);  // Map 内の参照を書き換える。
            if (!map.containsKey("scenamatica"))  // シナリオファイルには scenamatica キーが最上位に必須。
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

    public static ScenarioFileBean fromJar(Path jarPath, Path inJarPath)
            throws IOException, InvalidScenarioFileException
    {
        try (ZipFile zip = new ZipFile(jarPath.toFile()))  // .jar を .zip としてみなす。
        {
            ZipEntry entry = zip.getEntry(inJarPath.toString());
            if (entry == null)
                throw new FileNotFoundException("Scenario file not found in jar: " + inJarPath);

            try (InputStream zis = zip.getInputStream(entry))
            {
                return fromInputStream(zis, inJarPath.toString());
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

                String inZipPath = entry.getName();
                if (!isScenarioFile(inZipPath))
                    continue;

                try (InputStream zis = zip.getInputStream(entry))
                {
                    ScenarioFileBean scenario = fromInputStream(zis, inZipPath);
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

    private static boolean isScenarioFile(String inZipPath)
    {
        return Arrays.stream(SCENARIO_FILE_EXTENSIONS)
                .anyMatch(ext -> StringUtils.endsWithIgnoreCase(inZipPath, ext));
    }
}
