package org.kunlab.scenamatica.selector.compiler.lexer;

public enum TokenType
{
    NONE,

    TYPE,
    LITERAL,
    NEGATE,

    PARAMETER_BEGIN,
    PARAMETER_KEY,
    PARAMETER_SEPARATOR,
    KEY_VALUE_SEPARATOR,
    PARAMETER_END,

    COLLECTIONS_BEGIN,
    COLLECTIONS_KEY,
    COLLECTIONS_END
}
