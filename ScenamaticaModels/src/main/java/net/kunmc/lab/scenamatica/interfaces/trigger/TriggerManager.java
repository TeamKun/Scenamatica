package net.kunmc.lab.scenamatica.interfaces.trigger;

import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * トリガを管理するインタフェースです。
 */
public interface TriggerManager
{
    /**
     * トリガを追加します。
     *
     * @param scenarioName シナリオ名
     * @param trigger      トリガ
     */
    void addTrigger(@NotNull String scenarioName, List<? extends TriggerBean> trigger);

    /**
     * トリガを実行します。
     *
     * @param plugin       プラグイン
     * @param scenarioName シナリオ名
     * @param type         トリガタイプ
     * @param argument     トリガ引数
     */
    void performTriggerFire(@NotNull Plugin plugin,
                            @NotNull String scenarioName,
                            @NotNull TriggerType type,
                            @Nullable TriggerArgument argument
    ) throws ContextPreparationException, ScenarioException;
}
