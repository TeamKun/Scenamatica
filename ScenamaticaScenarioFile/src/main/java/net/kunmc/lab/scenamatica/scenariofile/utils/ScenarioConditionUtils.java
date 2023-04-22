package net.kunmc.lab.scenamatica.scenariofile.utils;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;

import java.util.Map;

@UtilityClass
public class ScenarioConditionUtils
{
    public static ActionBean parse(Map<String, Object> map)
    {
        if (!map.containsKey(ActionBean.KEY_TYPE))
            map.put(ActionBean.KEY_TYPE, ScenarioType.CONDITION_REQUIRE.getKey());
        else if (!ScenarioType.CONDITION_REQUIRE.getKey().equalsIgnoreCase((String) map.get(ActionBean.KEY_TYPE)))
            // require 以外のアクションは許容しない。(シナリオを実行するかどうかのチェックに使うため)
            throw new IllegalArgumentException("Invalid action type, only require type is allowed here.");

        return ActionBeanImpl.deserialize(map);
    }
}
