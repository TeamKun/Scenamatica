package org.kunlab.scenamatica.action.selector.compiler.lexer;

import org.kunlab.scenamatica.action.selector.compiler.SelectorSyntaxErrorException;

import java.util.LinkedList;

public class SelectorLexer
{
    public static final char TYPE_PREFIX = '@';
    public static final char PARAMETER_BEGIN = '[';
    public static final char PARAMETER_END = ']';
    public static final char PARAMETER_SEPARATOR = ',';
    public static final char COLLECTIONS_BEGIN = '{';
    public static final char COLLECTIONS_END = '}';
    public static final char KEY_VALUE_SEPARATOR = '=';
    public static final char STRING_QUOTE = '"';
    public static final char STRING_ESCAPE = '\\';
    public static final String[] IGNORED_CHARACTERS = {" ", "\t", "\n", "\r"};

    private static boolean isSpecialCharacter(char c)
    {
        return c == TYPE_PREFIX
                || c == PARAMETER_BEGIN
                || c == PARAMETER_END
                || c == PARAMETER_SEPARATOR
                || c == COLLECTIONS_BEGIN
                || c == COLLECTIONS_END
                || c == KEY_VALUE_SEPARATOR
                || c == STRING_QUOTE
                || c == STRING_ESCAPE;
    }

    private static TokenType convertSpecialCharToTokenType(char c)
    {
        if (c == TYPE_PREFIX)
            return TokenType.TYPE;
        else if (c == PARAMETER_BEGIN)
            return TokenType.PARAMETER_BEGIN;
        else if (c == PARAMETER_END)
            return TokenType.PARAMETER_END;
        else if (c == PARAMETER_SEPARATOR)
            return TokenType.PARAMETER_SEPARATOR;
        else if (c == COLLECTIONS_BEGIN)
            return TokenType.COLLECTIONS_BEGIN;
        else if (c == COLLECTIONS_END)
            return TokenType.COLLECTIONS_END;
        else if (c == KEY_VALUE_SEPARATOR)
            return TokenType.KEY_VALUE_SEPARATOR;
        else
            return TokenType.LITERAL;
    }

    public static LinkedList<SelectorToken> tokenize(String parametersString)
    {
        LinkedList<SelectorToken> tokens = new LinkedList<>();

        TokenType previousTokenType = TokenType.NONE;
        TokenBuffer processingToken = new TokenBuffer();
        boolean isInsideString = false;
        boolean isEscaped = false;
        int depth = 0;
        int ignoredCharacterCount = 0;
        for (int i = 0; i < parametersString.length(); i++)
        {
            char c = parametersString.charAt(i);

            if (isIgnoredCharacter(c))
            {
                ignoredCharacterCount++;
                // 文字列リテラルの中の場合は拾う。それ以外は破棄。
                if (isInsideString)
                    processingToken.append(c);
                else if (!processingToken.isEmpty())
                    throw SelectorSyntaxErrorException.unexpectedToken(parametersString, processingToken.pop(), i);
                continue;
            }
            else if (!isSpecialCharacter(c))  // 特殊文字でない場合はそのまま拾う
            {
                processingToken.append(c);
                continue;
            }

            if (isEscaped)  // エスケープされている場合は通常の文字として拾う
            {
                processingToken.append(c);
                isEscaped = false;
                continue;
            }

            if (c == STRING_ESCAPE)
            {
                isEscaped = true;
                continue;
            }

            if (!processingToken.isEmpty())  // 特殊文字ではなく、かつバッファが空でない場合はバッファを吐き出す
            {
                if (previousTokenType == TokenType.TYPE
                        || previousTokenType == TokenType.PARAMETER_BEGIN
                        || previousTokenType == TokenType.COLLECTIONS_BEGIN
                        || previousTokenType == TokenType.PARAMETER_SEPARATOR
                        || previousTokenType == TokenType.KEY_VALUE_SEPARATOR)
                {
                    String token = processingToken.pop();
                    tokens.add(new SelectorToken(token, i - token.length() + ignoredCharacterCount, TokenType.LITERAL));
                }
                else
                    throw SelectorSyntaxErrorException.unexpectedToken(parametersString, processingToken.pop(), i);
                previousTokenType = TokenType.LITERAL;
            }

            if (isInsideString)
            {
                if (c == STRING_QUOTE)
                    isInsideString = false;  // pop は, 上の if で行われる
                else
                    processingToken.append(c);

                continue;
            }
            else if (c == STRING_QUOTE)
            {
                isInsideString = true;
                continue;
            }

            processingToken.append(c);

            TokenType tokenType = convertSpecialCharToTokenType(c);
            assert tokenType != TokenType.NONE;

            switch (tokenType)
            {
                case TYPE:
                    if (previousTokenType == TokenType.NONE)
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.TYPE));
                    else
                        throw SelectorSyntaxErrorException.unexpectedDeclare(parametersString, "type", i);
                    break;
                case PARAMETER_BEGIN:
                    if (previousTokenType == TokenType.NONE  // type が省略されている場合
                            || previousTokenType == TokenType.LITERAL)
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.PARAMETER_BEGIN));
                    else
                        throw SelectorSyntaxErrorException.unexpectedDeclare(parametersString, "parameter", i);

                    depth++;
                    break;
                case PARAMETER_END:
                    if (previousTokenType == TokenType.PARAMETER_BEGIN  // parameter が省略されている場合
                            || previousTokenType == TokenType.LITERAL  // parameter の値が省略されている場合
                            || previousTokenType == TokenType.COLLECTIONS_END
                            || previousTokenType == TokenType.PARAMETER_SEPARATOR)
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.PARAMETER_END));
                    else
                        throw SelectorSyntaxErrorException.unexpectedEnd(parametersString, "parameter", i);

                    depth--;
                    break;  // END が出たので、強制終了
                case PARAMETER_SEPARATOR:
                    if (previousTokenType == TokenType.LITERAL
                            || previousTokenType == TokenType.COLLECTIONS_END)
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.PARAMETER_SEPARATOR));
                    else
                        throw SelectorSyntaxErrorException.unexpectedToken(parametersString, processingToken.pop(), i);
                    break;
                case COLLECTIONS_BEGIN:
                    if (previousTokenType == TokenType.PARAMETER_SEPARATOR
                            || previousTokenType == TokenType.KEY_VALUE_SEPARATOR
                            || previousTokenType == TokenType.COLLECTIONS_BEGIN) // ネストしたコレクションの場合
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.COLLECTIONS_BEGIN));
                    else
                        throw SelectorSyntaxErrorException.unexpectedDeclare(parametersString, "collections", i);

                    depth++;
                    break;
                case COLLECTIONS_END:
                    if (previousTokenType == TokenType.LITERAL
                            || previousTokenType == TokenType.PARAMETER_SEPARATOR  // , が冗長になっている場合
                            || previousTokenType == TokenType.COLLECTIONS_BEGIN  // 空のコレクションの場合
                            || (previousTokenType == TokenType.COLLECTIONS_END && depth > 1)) // ネストしたコレクションの場合
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.COLLECTIONS_END));
                    else
                        throw SelectorSyntaxErrorException.unexpectedEnd(parametersString, "collections", i);

                    depth--;
                    break;
                case KEY_VALUE_SEPARATOR:
                    if (previousTokenType == TokenType.LITERAL)
                        tokens.add(new SelectorToken(processingToken.pop(), i, TokenType.KEY_VALUE_SEPARATOR));
                    else
                        throw SelectorSyntaxErrorException.unexpectedToken(parametersString, processingToken.pop(), i);
            }

            previousTokenType = tokenType;
        }
        if (previousTokenType == TokenType.TYPE)  // @<type> のみの場合
            tokens.add(new SelectorToken(processingToken.pop(), parametersString.length() - 1, TokenType.LITERAL));


        if (isInsideString)
            throw SelectorSyntaxErrorException.reachedEnd(parametersString, "inside string", parametersString.length());
        else if (isEscaped)
            throw SelectorSyntaxErrorException.reachedEnd(parametersString, "escaping", parametersString.length());
        else if (depth > 0)
            throw SelectorSyntaxErrorException.reachedEnd(parametersString, "inside collection", parametersString.length());
        else if (!processingToken.isEmpty())
            throw SelectorSyntaxErrorException.unexpectedToken(parametersString, processingToken.pop(), parametersString.length());

        return tokens;
    }

    private static boolean isIgnoredCharacter(char c)
    {
        for (String ignoredCharacter : IGNORED_CHARACTERS)
            if (ignoredCharacter.charAt(0) == c)
                return true;

        return false;
    }

    private static class TokenBuffer
    {
        private StringBuilder buffer;

        public TokenBuffer()
        {
            this.buffer = new StringBuilder();
        }

        public void append(char c)
        {
            this.buffer.append(c);
        }

        public String pop()
        {
            String result = this.buffer.toString();
            this.buffer = new StringBuilder();
            return result;
        }

        public boolean isEmpty()
        {
            return this.buffer.length() == 0;
        }
    }

}
