package org.kunlab.scenamatica.scenariofile;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.exceptions.scenariofile.NotAScenarioFileException;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@AllArgsConstructor
public class ScenarioFileParser
{
    public static final Logger LOGGER = LoggerFactory.getLogger(ScenarioFileParser.class);

    private static final String[] SCENARIO_FILE_EXTENSIONS = {
            ".yml",
            ".yaml"
    };

    public static ScenarioFileStructure fromMap(Map<String, Object> map, @Nullable String fileName)
            throws InvalidScenarioFileException
    {
        try
        {
            DefinitionsMapper.resolveReferences(map);  // Map 内の参照を書き換える。
            if (!map.containsKey("scenamatica"))  // シナリオファイルには scenamatica キーが最上位に必須。
                throw new NotAScenarioFileException(fileName);

            return StructureSerializerImpl.getInstance().deserialize(map, ScenarioFileStructure.class);
        }
        catch (IllegalArgumentException e)
        {
            String fileNameStr = fileName == null ? "": " in file " + fileName;

            throw new InvalidScenarioFileException(e.getMessage() + fileNameStr, e);
        }
    }

    public static ScenarioFileStructure fromInputStream(InputStream inputStream, @Nullable String fileName)
            throws InvalidScenarioFileException
    {
        Yaml sYaml = new Yaml(new SimpleYamlConstructor());
        Map<String, Object> map = sYaml.load(inputStream);
        return fromMap(map, fileName);
    }

    public static ScenarioFileStructure fromJar(Path jarPath, Path inJarPath)
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

    public static Map<String, ScenarioFileStructure> loadAllFromJar(Path jarPath)
            throws IOException, InvalidScenarioFileException
    {
        Map<String, ScenarioFileStructure> map = new HashMap<>();

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
                    ScenarioFileStructure scenario = fromInputStream(zis, inZipPath);
                    if (shouldIgnore(scenario))
                        continue;
                    if (map.containsKey(scenario.getName()))
                    {
                        String descA = map.get(scenario.getName()).getDescription();
                        String descB = scenario.getDescription();
                        throw new IllegalStateException(String.format(
                                "Duplicated scenario name: %s(%s, %s)",
                                scenario.getName(),
                                descA == null ? "N/A": descA,
                                descB == null ? "N/A": descB
                        ));
                    }

                    map.put(scenario.getName(), scenario);
                }
                catch (NotAScenarioFileException ignored)
                {
                } // plugin.yml などのファイルが混じっているので無視する。
            }
        }

        return map;
    }

    private static boolean shouldIgnore(ScenarioFileStructure scenario)
    {
        VersionRange supportVersionRange = scenario.getMinecraftVersions();
        if (supportVersionRange == null)
            return true;

        Version runningVersion = Version.of(Bukkit.getServer().getMinecraftVersion());

        boolean shouldIgnore = !supportVersionRange.isInRange(runningVersion);
        if (shouldIgnore)
            LOGGER.debug("Ignoring scenario file {} due to unsupported Minecraft version: {}", scenario.getName(), runningVersion);

        return shouldIgnore;
    }

    private static boolean isScenarioFile(String inZipPath)
    {
        return Arrays.stream(SCENARIO_FILE_EXTENSIONS)
                .anyMatch(ext -> StringUtils.endsWithIgnoreCase(inZipPath, ext));
    }
}
