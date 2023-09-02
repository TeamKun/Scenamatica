package org.kunlab.scenamatica;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.function.Consumer;

/**
 * Scenamatica の API です。
 */
public class ScenamaticaAPI
{
    /**
     * Scenamatica を利用できる場合に、指定された処理を実行します。
     *
     * @param consumer 実行する処理
     */
    public static void runIfAvailable(Consumer<? super ScenamaticaRegistry> consumer)
    {
        ScenamaticaRegistry registry = ScenamaticaBridge.retrieveRegistry();
        if (registry != null)
            consumer.accept(registry);
    }

    /**
     * Scenamatica を利用できる場合に、マイルストーンを達成したとしてマークします。
     *
     * @param plugin マイルストーンを達成したプラグイン
     * @param name   マイルストーンの名前
     */
    public static void reachMilestone(@NotNull Plugin plugin, @NotNull String name)
    {
        runIfAvailable(registry -> {
            if (registry.getScenarioManager().getCurrentScenario() == null)  // NoClassDefFoundError 回避
                return;
            else if (!registry.getScenarioManager().getCurrentScenario().getPlugin().getName().equals(plugin.getName()))
                return;
            registry.getScenarioManager().getMilestoneManager().reachMilestone(
                    registry.getScenarioManager().getCurrentScenario(),
                    name
            );
        });
    }
}
