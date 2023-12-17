package org.kunlab.scenamatica.action.selector.compiler.lexer;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SelectorToken
{
    String token;
    int index;
    TokenType type;

    public SelectorToken(TokenType type, String token)
    {
        this(token, -1, type);
    }

    public boolean isNone()
    {
        return this.token == null;
    }

    @Override
    public String toString()
    {
        return String.format("%s(%d-%d): %s", this.type, this.index, this.index + this.token.length(), this.token);
    }
}
