package org.kunlab.scenamatica.bookkeeper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * アドモニションの種類を表す列挙型です。
 */
@AllArgsConstructor
@Getter
public enum AdmonitionType
{
    /**
     * 注釈や追加事項, その他の情報を表します。
     */
    NOTE("note"),
    /**
     * ヒントやアドバイスを表します。
     */
    TIP("tip"),
    /**
     * 追加情報を表します。
     */
    INFORMATION("info"),
    /**
     * 警告を表します。
     */
    WARNING("warning"),
    /**
     * 危険を表します。
     */
    DANGER("danger");

    private final String name;
}
