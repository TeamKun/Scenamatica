package org.kunlab.scenamatica.interfaces.structures.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;

import java.util.List;

/**
 * シナリオが実行されるタイミングを表すインターフェースです。
 */
@TypeDoc(
        name = "トリガ",
        description = "シナリオが実行されるタイミングを表します。",
        properties = {
                @TypeProperty(
                        name = TriggerStructure.KEY_TYPE,
                        type = TriggerType.class,
                        description = "トリガの種類を指定します。"
                ),
                @TypeProperty(
                        name = TriggerStructure.KEY_BEFORE_THAT,
                        type = ScenarioStructure[].class,
                        description = "トリガが実行される前に実行されるシナリオを指定します。"
                ),
                @TypeProperty(
                        name = TriggerStructure.KEY_AFTER_THAT,
                        type = ScenarioStructure[].class,
                        description = "トリガが実行された後に実行されるシナリオを指定します。"
                ),
                @TypeProperty(
                        name = TriggerStructure.KEY_RUN_IF,
                        type = ActionStructure.class,
                        description = "トリガの実行条件を指定します。"
                )
        }

)
public interface TriggerStructure extends Structure
{
    public static final String KEY_TYPE = "type";
    public static final String KEY_BEFORE_THAT = "before";
    public static final String KEY_AFTER_THAT = "after";
    public static final String KEY_RUN_IF = "runif";

    /**
     * このトリガのタイプを取得します。
     *
     * @return タイプ
     */
    @NotNull
    TriggerType getType();

    /**
     * このトリガの引数を取得します。
     *
     * @return 引数
     */
    @Nullable
    TriggerArgument getArgument();

    /**
     * このトリガが実行される前に実行されるシナリオを取得します。
     *
     * @return シナリオ
     */
    @NotNull
    List<ScenarioStructure> getBeforeThat();

    /**
     * このトリガが実行された後に実行されるシナリオを取得します。
     *
     * @return シナリオ
     */
    @NotNull
    List<ScenarioStructure> getAfterThat();

    /**
     * このシナリオの実行条件を取得します。
     *
     * @return 実行条件
     */
    ActionStructure getRunIf();

}
