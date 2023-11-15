package org.kunlab.scenamatica.scenariofile.beans;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioOrder;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class ScenarioFileBeanImpl implements ScenarioFileBean
{
    public static final String KEY_SCENAMATICA_VERSION = "scenamatica";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TIMEOUT = "timeout";
    public static final String KEY_ORDER = "order";
    public static final String KEY_TRIGGERS = "on";
    public static final String KEY_RUN_IF = "runif";
    public static final String KEY_CONTEXT = "context";
    public static final String KEY_SCENARIO = "scenario";

    @NotNull
    Version scenamaticaVersion;
    @NotNull
    String name;
    @NotNull
    String description;
    long timeout;
    int order;
    @NotNull
    List<TriggerBean> triggers;
    @Nullable
    ActionBean runIf;
    @Nullable
    ContextBean context;
    @NotNull
    List<ScenarioBean> scenario;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioFileBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_SCENAMATICA_VERSION, bean.getScenamaticaVersion().toString());
        map.put(KEY_NAME, bean.getName());
        map.put(KEY_DESCRIPTION, bean.getDescription());

        if (bean.getTimeout() != DEFAULT_TIMEOUT_TICK)
            map.put(KEY_TIMEOUT, bean.getTimeout());
        if (bean.getOrder() != ScenarioOrder.NORMAL.getOrder())
            map.put(KEY_ORDER, bean.getOrder());

        map.put(KEY_TRIGGERS, bean.getTriggers().stream()
                .map(trigger -> serializer.serialize(trigger, TriggerBean.class))
                .collect(Collectors.toList())
        );
        map.put(KEY_SCENARIO, bean.getScenario().stream()
                .map(scenario -> serializer.serialize(scenario, ScenarioBean.class))
                .collect(Collectors.toList())
        );

        if (bean.getContext() != null)
            map.put(KEY_CONTEXT, serializer.serialize(bean.getContext(), ContextBean.class));
        if (bean.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(bean.getRunIf(), ActionBean.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkType(map, KEY_SCENAMATICA_VERSION, String.class);
        if (!Version.isValidVersionString((String) map.get(KEY_SCENAMATICA_VERSION)))
            throw new IllegalArgumentException("Invalid version string: " + map.get(KEY_SCENAMATICA_VERSION));

        MapUtils.checkType(map, KEY_NAME, String.class);
        MapUtils.checkType(map, KEY_DESCRIPTION, String.class);
        MapUtils.checkNumberIfContains(map, KEY_TIMEOUT);
        MapUtils.checkNumberIfContains(map, KEY_ORDER);

        if (map.containsKey(KEY_CONTEXT))
            serializer.validate(
                    MapUtils.checkAndCastMap(
                            map.get(KEY_CONTEXT),
                            String.class,
                            Object.class
                    ),
                    ContextBean.class
            );

        MapUtils.checkType(map, KEY_TRIGGERS, List.class);
        ((List<?>) map.get(KEY_TRIGGERS)).forEach(
                o -> serializer.validate(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                ), TriggerBean.class));

        MapUtils.checkType(map, KEY_SCENARIO, List.class);
        ((List<?>) map.get(KEY_SCENARIO)).forEach(o -> serializer.validate(MapUtils.checkAndCastMap(
                o,
                String.class,
                Object.class
        ), ScenarioBean.class));
    }

    @NotNull
    public static ScenarioFileBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

        Version scenamatica = Version.of((String) map.get(KEY_SCENAMATICA_VERSION));
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

        List<TriggerBean> triggers = ((List<?>) map.get(KEY_TRIGGERS)).stream()
                .map(o -> serializer.deserialize(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                ), TriggerBean.class))
                .collect(Collectors.toList());
        List<ScenarioBean> scenario = ((List<?>) map.get(KEY_SCENARIO)).stream()
                .map(o -> serializer.deserialize(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                ), ScenarioBean.class))
                .collect(Collectors.toList());

        ContextBean context = null;
        if (map.containsKey(KEY_CONTEXT))
            context = serializer.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_CONTEXT),
                    String.class,
                    Object.class
            ), ContextBean.class);

        ActionBean runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_RUN_IF),
                    String.class,
                    Object.class
            ), ActionBean.class);

        return new ScenarioFileBeanImpl(
                scenamatica,
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
