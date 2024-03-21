package org.kunlab.scenamatica.interfaces.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.List;

/**
 * シナリオのファイルの情報を表すインターフェースです。
 */
public interface ScenarioFileStructure extends Structure
{
    long DEFAULT_TIMEOUT_TICK = 20L * 60L * 5L;

    /**
     * 対応する Scenamatica のバージョンを取得します。
     *
     * @return Scenamatica のバージョン
     */
    @NotNull
    Version getScenamaticaVersion();

    /**
     * 対応する Minecraft のバージョンを取得します。
     *
     * @return Minecraft のバージョン
     */
    @NotNull
    List<Version> getMinecraftVersions();

    /**
     * このシナリオの名前を取得します。
     * 対象プラグインで一意である必要があります。
     * キャメルケースが推奨されています。
     *
     * @return 名前
     */
    @NotNull
    String getName();

    /**
     * このシナリオの説明を取得します。
     *
     * @return 説明
     */
    @Nullable
    String getDescription();

    /**
     * シナリオ全体のタイムアウトの時間を**チック**で定義します。
     *
     * @return タイムアウトの時間
     */
    long getTimeout();

    /**
     * 同一セッションでのシナリオの実行順序を定義します。
     * 値が小さいほど先に実行されます。
     * デフォルトでは {@link Integer#MAX_VALUE} が設定されています。
     *
     * @return 実行順序
     */
    int getOrder();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    @Nullable
    ActionStructure getRunIf();

    /**
     * このシナリオの実行タイミングを取得します。
     *
     * @return 実行タイミング
     */
    @NotNull
    List<TriggerStructure> getTriggers();

    /**
     * このシナリオの実行に必要な情報を取得します。
     *
     * @return 実行に必要な情報
     */
    @Nullable
    ContextStructure getContext();

    /**
     * シナリオを取得します。
     *
     * @return シナリオ
     */
    @NotNull
    List<ScenarioStructure> getScenario();
}
