package org.kunlab.scenamatica.interfaces.structures.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * シナリオの動作の定義を表すインターフェースです。
 */
@TypeDoc(
        name = "アクション",
        description = "シナリオのアクションを表します。",
        properties = {
                @TypeProperty(
                        name = ActionStructure.KEY_TYPE,
                        type = String.class,
                        description = "アクションの種類を指定します。"
                ),
                @TypeProperty(
                        name = ActionStructure.KEY_ARGUMENTS,
                        type = Map.class,
                        description = "アクションに使用する引数を定義します。"
                )
        }
)
public interface ActionStructure extends Structure
{
    String KEY_TYPE = "action";
    String KEY_ARGUMENTS = "with";

    /**
     * 動作の種類を定義します。
     *
     * @return 動作の種類
     */
    @NotNull
    String getType();

    /**
     * 動作に必要な引数を定義します。
     *
     * @return 動作に必要な引数
     */
    @Nullable
    Map<String, Object> getArguments();
}
