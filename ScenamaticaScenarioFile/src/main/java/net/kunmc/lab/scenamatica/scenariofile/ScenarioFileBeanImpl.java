package net.kunmc.lab.scenamatica.scenariofile;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.ContextBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.trigger.TriggerBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.utils.ScenarioConditionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class ScenarioFileBeanImpl implements ScenarioFileBean
{
    private static final String KEY_SCENAMATICA_VERSION = "scenamatica";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TRIGGERS = "on";
    private static final String KEY_RUN_IF = "runif";
    private static final String KEY_CONTEXT = "context";
    private static final String KEY_SCENARIO = "scenario";

    @NotNull
    Version scenamaticaVersion;
    @NotNull
    String name;
    @NotNull
    String description;
    @NotNull
    List<TriggerBean> triggers;
    @Nullable
    ActionBean runIf;
    @Nullable
    ContextBean context;
    @NotNull
    List<ScenarioBean> scenario;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioFileBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_SCENAMATICA_VERSION, bean.getScenamaticaVersion().toString());
        map.put(KEY_NAME, bean.getName());
        map.put(KEY_DESCRIPTION, bean.getDescription());
        map.put(KEY_TRIGGERS, bean.getTriggers().stream()
                .map(TriggerBeanImpl::serialize)
                .collect(Collectors.toList())
        );
        map.put(KEY_SCENARIO, bean.getScenario().stream()
                .map(ScenarioBeanImpl::serialize)
                .collect(Collectors.toList())
        );

        if (bean.getContext() != null)
            map.put(KEY_CONTEXT, ContextBeanImpl.serialize(bean.getContext()));
        if (bean.getRunIf() != null)
            map.put(KEY_RUN_IF, ActionBeanImpl.serialize(bean.getRunIf()));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_SCENAMATICA_VERSION, String.class);
        if (!Version.isValidVersionString((String) map.get(KEY_SCENAMATICA_VERSION)))
            throw new IllegalArgumentException("Invalid version string: " + map.get(KEY_SCENAMATICA_VERSION));

        MapUtils.checkType(map, KEY_NAME, String.class);
        MapUtils.checkType(map, KEY_DESCRIPTION, String.class);

        if (map.containsKey(KEY_CONTEXT))
            ContextBeanImpl.validate(MapUtils.checkAndCastMap(
                    map.get(KEY_CONTEXT),
                    String.class,
                    Object.class
            ));

        MapUtils.checkType(map, KEY_TRIGGERS, List.class);
        ((List<?>) map.get(KEY_TRIGGERS)).forEach(o -> TriggerBeanImpl.validate(MapUtils.checkAndCastMap(
                o,
                String.class,
                Object.class
        )));

        MapUtils.checkType(map, KEY_SCENARIO, List.class);
        ((List<?>) map.get(KEY_SCENARIO)).forEach(o -> ScenarioBeanImpl.validate(MapUtils.checkAndCastMap(
                o,
                String.class,
                Object.class
        )));
    }

    @NotNull
    public static ScenarioFileBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        Version scenamatica = Version.of((String) map.get(KEY_SCENAMATICA_VERSION));
        String name = (String) map.get(KEY_NAME);
        String description = (String) map.get(KEY_DESCRIPTION);
        List<TriggerBean> triggers = ((List<?>) map.get(KEY_TRIGGERS)).stream()
                .map(o -> TriggerBeanImpl.deserialize(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                )))
                .collect(Collectors.toList());
        List<ScenarioBean> scenario = ((List<?>) map.get(KEY_SCENARIO)).stream()
                .map(o -> ScenarioBeanImpl.deserialize(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                )))
                .collect(Collectors.toList());

        ContextBean context = null;
        if (map.containsKey(KEY_CONTEXT))
            context = ContextBeanImpl.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_CONTEXT),
                    String.class,
                    Object.class
            ));

        ActionBean runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = ScenarioConditionUtils.parse(MapUtils.checkAndCastMap(
                    map.get(KEY_RUN_IF),
                    String.class,
                    Object.class
            ));

        return new ScenarioFileBeanImpl(
                scenamatica,
                name,
                description,
                triggers,
                runIf,
                context,
                scenario
        );
    }
}
