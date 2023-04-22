package net.kunmc.lab.scenamatica.scenariofile.beans.trigger;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.utils.ScenarioConditionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
public class TriggerBeanImpl implements TriggerBean
{
    private static final String KEY_TYPE = "type";
    private static final String KEY_BEFORE_THAT = "before";
    private static final String KEY_AFTER_THAT = "after";
    private static final String KEY_RUN_IF = "runif";

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
    public static Map<String, Object> serialize(@NotNull TriggerBean bean)
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
                list.add(ScenarioBeanImpl.serialize(scenario));
            map.put(KEY_BEFORE_THAT, list);
        }
        if (!bean.getAfterThat().isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioBean scenario : bean.getAfterThat())
                list.add(ScenarioBeanImpl.serialize(scenario));
            map.put(KEY_AFTER_THAT, list);
        }

        if (bean.getRunIf() != null)
            map.put(KEY_RUN_IF, ActionBeanImpl.serialize(bean.getRunIf()));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_TYPE, String.class);
        if (TriggerType.fromKey((String) map.get(KEY_TYPE)) == null)
            throw new IllegalArgumentException("Invalid trigger type: " + map.get(KEY_TYPE));

        if (map.containsKey(KEY_BEFORE_THAT))
        {
            MapUtils.checkType(map, KEY_BEFORE_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                ScenarioBeanImpl.validate(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                ));
        }
        if (map.containsKey(KEY_AFTER_THAT))
        {
            MapUtils.checkType(map, KEY_AFTER_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                ScenarioBeanImpl.validate(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                ));
        }

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));
        if (type != null && type.getArgumentType() != null)
            type.validateArguments(map);

        if (map.containsKey(KEY_RUN_IF))
            MapUtils.checkType(map, KEY_RUN_IF, Map.class);
    }

    @NotNull
    public static TriggerBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));

        TriggerArgument argument = null;
        if (type != null)
            argument = type.deserialize(map);

        List<ScenarioBean> beforeThat = new LinkedList<>();
        if (map.containsKey(KEY_BEFORE_THAT))
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                beforeThat.add(ScenarioBeanImpl.deserialize(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                )));

        List<ScenarioBean> afterThat = new LinkedList<>();
        if (map.containsKey(KEY_AFTER_THAT))
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                afterThat.add(ScenarioBeanImpl.deserialize(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                )));

        ActionBean runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = ScenarioConditionUtils.parse(MapUtils.checkAndCastMap(
                    map.get(KEY_RUN_IF),
                    String.class,
                    Object.class
            ));

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
