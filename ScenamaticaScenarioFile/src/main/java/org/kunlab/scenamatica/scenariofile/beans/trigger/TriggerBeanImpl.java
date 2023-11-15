package org.kunlab.scenamatica.scenariofile.beans.trigger;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
public class TriggerBeanImpl implements TriggerBean
{
    public static final String KEY_TYPE = "type";
    public static final String KEY_BEFORE_THAT = "before";
    public static final String KEY_AFTER_THAT = "after";
    public static final String KEY_RUN_IF = "runif";

    @NotNull
    TriggerType type;
    @Nullable
    TriggerArgument argument;
    @NotNull
    List<ScenarioBean> beforeThat;
    @NotNull
    List<ScenarioBean> afterThat;
    @Nullable
    ActionBean runIf;

    @NotNull
    public static Map<String, Object> serialize(@NotNull TriggerBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_TYPE, bean.getType().getKey());

        if (bean.getArgument() != null)
        {
            Map<String, Object> arguments = bean.getType().serializeArgument(bean.getArgument());
            if (arguments != null)
                map.putAll(arguments);
        }

        if (!bean.getBeforeThat().isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioBean scenario : bean.getBeforeThat())
                list.add(serializer.serialize(scenario, ScenarioBean.class));
            map.put(KEY_BEFORE_THAT, list);
        }
        if (!bean.getAfterThat().isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioBean scenario : bean.getAfterThat())
                list.add(serializer.serialize(scenario, ScenarioBean.class));
            map.put(KEY_AFTER_THAT, list);
        }

        if (bean.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(bean.getRunIf(), ActionBean.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkType(map, KEY_TYPE, String.class);
        if (TriggerType.fromKey((String) map.get(KEY_TYPE)) == null)
            throw new IllegalArgumentException("Invalid trigger type: " + map.get(KEY_TYPE));

        if (map.containsKey(KEY_BEFORE_THAT))
        {
            MapUtils.checkType(map, KEY_BEFORE_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                serializer.validate(
                        MapUtils.checkAndCastMap(obj),
                        ScenarioBean.class
                );
        }
        if (map.containsKey(KEY_AFTER_THAT))
        {
            MapUtils.checkType(map, KEY_AFTER_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                serializer.validate(
                        MapUtils.checkAndCastMap(obj),
                        ScenarioBean.class
                );
        }

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));
        if (type != null && type.getArgumentType() != null)
            type.validateArguments(map);

        if (map.containsKey(KEY_RUN_IF))
            MapUtils.checkType(map, KEY_RUN_IF, Map.class);
    }

    @NotNull
    public static TriggerBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));

        TriggerArgument argument = null;
        if (type != null)
            argument = type.deserialize(map);

        List<ScenarioBean> beforeThat = new LinkedList<>();
        if (map.containsKey(KEY_BEFORE_THAT))
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                beforeThat.add(serializer.deserialize(
                        MapUtils.checkAndCastMap(obj), ScenarioBean.class));

        List<ScenarioBean> afterThat = new LinkedList<>();
        if (map.containsKey(KEY_AFTER_THAT))
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                afterThat.add(serializer.deserialize(
                        MapUtils.checkAndCastMap(obj), ScenarioBean.class));

        ActionBean runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_RUN_IF)), ActionBean.class);

        assert type != null;  // validate() で検証済み
        return new TriggerBeanImpl(
                type,
                argument,
                beforeThat,
                afterThat,
                runIf
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TriggerBeanImpl)) return false;
        TriggerBeanImpl that = (TriggerBeanImpl) o;
        return this.type == that.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.type);
    }
}
