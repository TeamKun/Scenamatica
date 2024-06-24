package org.kunlab.scenamatica.scenariofile.structures;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioOrder;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.VersionRange;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TypeDoc(
        name = "シナリオファイル",
        description = "シナリオファイルの構造を表します。",
        properties ={
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_SCENAMATICA_VERSION,
                        type = String.class,
                        pattern = "^v?(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)(?:\\.(?<patch>0|[1-9]\\d*))?(?:-(?<preRelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+(?<buildMetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$",
                        description = "シナリオテスト時に使用できる**最も低い** Scenamatica のバージョンを指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_MINECRAFT_VERSIONS,
                        type = VersionRange.class,
                        description = "このシナリオが対応する Minecraft のバージョンを指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_NAME,
                        type = String.class,
                        description = "シナリオの名前を指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_DESCRIPTION,
                        type = String.class,
                        description = "シナリオの説明を指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_TIMEOUT,
                        type = long.class,
                        description = "シナリオがタイムアウトするまでの時間をチックで指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_ORDER,
                        type = int.class,
                        description = "シナリオの実行順序を指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_TRIGGERS,
                        type = TriggerStructure[].class,
                        description = "シナリオが実行されるトリガーを指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_RUN_IF,
                        type = ActionStructure.class,
                        description = "シナリオが実行される条件を指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_CONTEXT,
                        type = ContextStructure.class,
                        description = "シナリオが実行される際のコンテキストを指定します。"
                ),
                @TypeProperty(
                        name = ScenarioFileStructure.KEY_SCENARIO,
                        type = ScenarioStructure[].class,
                        description = "シナリオの構造を指定します。"
                )
       }
)
@Value
public class ScenarioFileStructureImpl implements ScenarioFileStructure
{
    @NotNull
    Version scenamaticaVersion;
    @Nullable
    VersionRange minecraftVersions;
    @NotNull
    String name;
    @Nullable
    String description;
    long timeout;
    int order;
    @NotNull
    List<TriggerStructure> triggers;
    @Nullable
    ActionStructure runIf;
    @Nullable
    ContextStructure context;
    @NotNull
    List<ScenarioStructure> scenario;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioFileStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_SCENAMATICA_VERSION, structure.getScenamaticaVersion().toString());

        if (structure.getMinecraftVersions() != null)
            map.put(KEY_MINECRAFT_VERSIONS, serializer.serialize(structure.getMinecraftVersions(), VersionRange.class));

        map.put(KEY_NAME, structure.getName());
        MapUtils.putIfNotNull(map, KEY_DESCRIPTION, structure.getDescription());

        if (structure.getTimeout() != DEFAULT_TIMEOUT_TICK)
            map.put(KEY_TIMEOUT, structure.getTimeout());
        if (structure.getOrder() != ScenarioOrder.NORMAL.getOrder())
            map.put(KEY_ORDER, structure.getOrder());

        map.put(KEY_TRIGGERS, structure.getTriggers().stream()
                .map(trigger -> serializer.serialize(trigger, TriggerStructure.class))
                .collect(Collectors.toList())
        );
        map.put(KEY_SCENARIO, structure.getScenario().stream()
                .map(scenario -> serializer.serialize(scenario, ScenarioStructure.class))
                .collect(Collectors.toList())
        );

        if (structure.getContext() != null)
            map.put(KEY_CONTEXT, serializer.serialize(structure.getContext(), ContextStructure.class));
        if (structure.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(structure.getRunIf(), ActionStructure.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        MapUtils.checkType(map, KEY_SCENAMATICA_VERSION, String.class);
        if (!Version.isValidVersionString((String) map.get(KEY_SCENAMATICA_VERSION)))
            throw new IllegalArgumentException("Invalid version string: " + map.get(KEY_SCENAMATICA_VERSION));

        if (map.containsKey(KEY_MINECRAFT_VERSIONS))
            serializer.validate(
                    MapUtils.checkAndCastMap(map.get(KEY_MINECRAFT_VERSIONS)),
                    VersionRange.class
            );


        MapUtils.checkType(map, KEY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_DESCRIPTION, String.class);
        MapUtils.checkNumberIfContains(map, KEY_TIMEOUT);
        MapUtils.checkNumberIfContains(map, KEY_ORDER);

        if (map.containsKey(KEY_CONTEXT))
            serializer.validate(
                    MapUtils.checkAndCastMap(map.get(KEY_CONTEXT)),
                    ContextStructure.class
            );

        MapUtils.checkType(map, KEY_TRIGGERS, List.class);
        ((List<?>) map.get(KEY_TRIGGERS))
                .forEach(o -> serializer.validate(MapUtils.checkAndCastMap(o), TriggerStructure.class));

        MapUtils.checkType(map, KEY_SCENARIO, List.class);
        ((List<?>) map.get(KEY_SCENARIO))
                .forEach(o -> serializer.validate(MapUtils.checkAndCastMap(o), ScenarioStructure.class));
    }

    @NotNull
    public static ScenarioFileStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        Version scenamatica = Version.of((String) map.get(KEY_SCENAMATICA_VERSION));
        VersionRange minecraftVersions = null;
        if (map.containsKey(KEY_MINECRAFT_VERSIONS))
            minecraftVersions = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_MINECRAFT_VERSIONS)), VersionRange.class);

        String name = (String) map.get(KEY_NAME);
        String description = (String) map.get(KEY_DESCRIPTION);
        long timeout = MapUtils.getAsLongOrDefault(map, KEY_TIMEOUT, DEFAULT_TIMEOUT_TICK);

        int order = ScenarioOrder.NORMAL.getOrder();
        Object orderObject = map.get(KEY_ORDER);
        if (orderObject != null)
        {
            String orderString = String.valueOf(orderObject);
            ScenarioOrder enumOrder;
            if ((enumOrder = ScenarioOrder.of(orderString)) != null)
                order = enumOrder.getOrder();
            else
                try
                {
                    order = Integer.parseInt(orderString);
                }
                catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("Invalid order: " + orderString, e);
                }
        }

        List<TriggerStructure> triggers = ((List<?>) map.get(KEY_TRIGGERS)).stream()
                .map(o -> serializer.deserialize(MapUtils.checkAndCastMap(o), TriggerStructure.class))
                .collect(Collectors.toList());
        List<ScenarioStructure> scenario = ((List<?>) map.get(KEY_SCENARIO)).stream()
                .map(o -> serializer.deserialize(MapUtils.checkAndCastMap(o), ScenarioStructure.class))
                .collect(Collectors.toList());

        ContextStructure context = null;
        if (map.containsKey(KEY_CONTEXT))
            context = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_CONTEXT)), ContextStructure.class);

        ActionStructure runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_RUN_IF)), ActionStructure.class);

        return new ScenarioFileStructureImpl(
                scenamatica,
                minecraftVersions,
                name,
                description,
                timeout,
                order,
                triggers,
                runIf,
                context,
                scenario
        );
    }
}
