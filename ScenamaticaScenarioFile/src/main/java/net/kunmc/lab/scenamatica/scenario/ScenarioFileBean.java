package net.kunmc.lab.scenamatica.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.scenario.beans.context.ContextBean;
import net.kunmc.lab.scenamatica.scenario.beans.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.scenario.beans.trigger.TriggerBean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * シナリオのファイルの情報を保持するクラスです。
 */
@Value
public class ScenarioFileBean implements Serializable
{
    /**
     * シナリオの名前です。
     * 人間でも読みやすい名前が望ましいです。
     */
    @NotNull
    String name;

    /**
     * シナリオのトリガを定義します。
     */
    @NotNull
    TriggerBean[] on;

    /**
     * シナリオの実行に必要な情報を定義します。
     */
    @Nullable
    ContextBean context;

    /**
     * シナリオを定義します。
     */
    @NotNull
    ScenarioBean[] scenario;
}
