package org.kunlab.scenamatica.commons.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;

import java.util.UUID;

@UtilityClass
public class LogUtils
{
    @NotNull
    public static String gerScenarioPrefix(@Nullable UUID testID, @Nullable ScenarioFileBean scenario)
    {
        String withScenarioName = scenario == null ? "":
                ChatColor.BOLD.toString() + ChatColor.AQUA + "TEST-" + StringUtils.substring(scenario.getName(), 0, 8);
        String withTestID = testID == null ? "":
                ChatColor.RESET.toString() + ChatColor.WHITE + "/" + ChatColor.GRAY + testID.toString().substring(0, 4);

        String withPrefix = withScenarioName.isEmpty() && withTestID.isEmpty() ? "": ChatColor.WHITE + "[" +
                withScenarioName +
                withTestID +
                ChatColor.WHITE + "] ";

        return withPrefix + ChatColor.RESET;
    }
}
