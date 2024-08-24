package org.kunlab.scenamatica.interfaces.structures.scenario;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * シナリオの流れを定義します。
 */
@TypeDoc(
        name = "シナリオ",
        description = "シナリオの流れを定義します。",
        properties = {
                @TypeProperty(
                        name = ScenarioStructure.KEY_SCENARIO_TYPE,
                        type = ScenarioType.$Doc.class,
                        description = "シナリオのタイプを指定します。"
                ),
                @TypeProperty(
                        name = ScenarioStructure.KEY_SCENARIO_NAME,
                        type = String.class,
                        description = "シナリオの名前を指定します。"
                ),
                @TypeProperty(
                        name = ScenarioStructure.KEY_RUN_IF,
                        type = ActionStructure.class,
                        description = "シナリオの実行条件を指定します。"
                ),
                @TypeProperty(
                        name = ScenarioStructure.KEY_TIMEOUT,
                        type = long.class,
                        description = "シナリオがタイムアウトするまでの時間をチックで指定します。"
                )
        },
        extending = ActionStructure.class
)
public interface ScenarioStructure extends Structure
{
    public static final String KEY_SCENARIO_TYPE = "type";
    public static final String KEY_SCENARIO_NAME = "name";
    public static final String KEY_RUN_IF = "runif";
    public static final String KEY_TIMEOUT = "timeout";

    /**
     * このシナリオのタイプを取得します。
     *
     * @return タイプ
     */
    @NotNull
    ScenarioType getType();

    /**
     * このシナリオが実行するアクションを取得します。
     *
     * @return アクション
     */
    @NotNull
    ActionStructure getAction();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    ActionStructure getRunIf();

    /**
     * タイムアウトの時間を**チック**で定義します。
     * {@code -1} は無限に待つことを意味します。
     *
     * @return タイムアウトの時間
     */
    long getTimeout();

    /**
     * このシナリオの名前を取得します。
     *
     * @return 名前
     */
    String getName();
}
