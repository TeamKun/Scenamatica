package net.kunmc.lab.scenamatica.interfaces.trigger;

import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * トリガを管理するインタフェースです。
 */
public interface TriggerManager
{
    /**
     * シナリオのトリガを登録します。
     *
     * @param engine エンジン
     */
    void bakeTriggers(@NotNull ScenarioEngine engine);

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
    ) throws ScenarioException;

    /**
     * トリガを登録解除します。
     *
     * @param engine シナリオエンジン
     */
    void unregisterTrigger(@NotNull ScenarioEngine engine);
}
