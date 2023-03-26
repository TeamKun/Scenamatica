package net.kunmc.lab.scenamatica.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * アクションのタイプを表す列挙型です。
 */
@Getter
@RequiredArgsConstructor
public enum ActionType
{
    NONE("none");

    private static final String BEAN_SERIALIZER_METHOD = "serialize";
    private static final String BEAN_VALIDATOR_METHOD = "validate";
    private static final String BEAN_DESERIALIZER_METHOD = "deserialize";

    /**
     * アクションのキーです。
     */
    private final String key;
}
