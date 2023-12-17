package org.kunlab.scenamatica.action.selector.compiler.lexer;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SelectorLexerLexingTest
{
    private static void assertTokens(List<SelectorToken> expectedTokens, List<SelectorToken> actualTokens)
    {
        assertEquals(expectedTokens.size(), actualTokens.size());
        for (int i = 0; i < expectedTokens.size(); i++)
        {
            SelectorToken expectedToken = expectedTokens.get(i);
            SelectorToken actualToken = actualTokens.get(i);

            String exToken = expectedToken.getToken();
            String acToken = actualToken.getToken();
            assertEquals(expectedToken.getToken(), actualToken.getToken());
            assertEquals(exToken + ": " + expectedToken.getType(), acToken + ": " + actualToken.getType());
            assertEquals(exToken + ": " + expectedToken.getIndex(), acToken + ": " + actualToken.getIndex());
        }
    }

    @Test
    public void 空文字列で正常にトークン化されること()
    {
        final String testCase = "";
        final List<SelectorToken> expectedTokens = Collections.emptyList();

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void タイプのみで正常にトークン化されること()
    {
        final String testCase = "@a";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("@", 0, TokenType.TYPE));
            this.add(new SelectorToken("a", 1, TokenType.LITERAL));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void タイプと空引数のみで正常にトークン化されること()
    {
        final String testCase = "@a[]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("@", 0, TokenType.TYPE));
            this.add(new SelectorToken("a", 1, TokenType.LITERAL));
            this.add(new SelectorToken("[", 2, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("]", 3, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 空引数のみで正常にトークン化されること()
    {
        final String testCase = "[]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("]", 1, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void タイプと引数のみで正常にトークン化されること()
    {
        final String testCase = "@a[foo=bar]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("@", 0, TokenType.TYPE));
            this.add(new SelectorToken("a", 1, TokenType.LITERAL));
            this.add(new SelectorToken("[", 2, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 3, TokenType.LITERAL));
            this.add(new SelectorToken("=", 6, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 7, TokenType.LITERAL));
            this.add(new SelectorToken("]", 10, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 引数のみで正常にトークン化されること()
    {
        final String testCase = "[foo=bar]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 5, TokenType.LITERAL));
            this.add(new SelectorToken("]", 8, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 複数の引数で正常にトークン化されること()
    {
        final String testCase = "[foo=bar,bar=foo]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 5, TokenType.LITERAL));
            this.add(new SelectorToken(",", 8, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 9, TokenType.LITERAL));
            this.add(new SelectorToken("=", 12, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("foo", 13, TokenType.LITERAL));
            this.add(new SelectorToken("]", 16, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 引用符内の文字列で正常にトークン化されること()
    {
        final String testCase = "[foo=\"bar\"]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 6, TokenType.LITERAL));
            this.add(new SelectorToken("]", 10, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 引用符内のエスケープされた文字列で正常にトークン化されること()
    {
        final String testCase = "[foo=\"\\\"bar\\\"\"]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("\"bar\"", 8, TokenType.LITERAL));
            this.add(new SelectorToken("]", 14, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void リストの引数で正常にトークン化されること()
    {
        final String testCase = "[foo={hoge,fuga,piyo}]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 5, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("hoge", 6, TokenType.LITERAL));
            this.add(new SelectorToken(",", 10, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("fuga", 11, TokenType.LITERAL));
            this.add(new SelectorToken(",", 15, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("piyo", 16, TokenType.LITERAL));
            this.add(new SelectorToken("}", 20, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("]", 21, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 複数のリストの引数で正常にトークン化されること()
    {
        final String testCase = "[foo={hoge,fuga,piyo},bar={foo,bar,baz}]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 5, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("hoge", 6, TokenType.LITERAL));
            this.add(new SelectorToken(",", 10, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("fuga", 11, TokenType.LITERAL));
            this.add(new SelectorToken(",", 15, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("piyo", 16, TokenType.LITERAL));
            this.add(new SelectorToken("}", 20, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken(",", 21, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 22, TokenType.LITERAL));
            this.add(new SelectorToken("=", 25, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 26, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("foo", 27, TokenType.LITERAL));
            this.add(new SelectorToken(",", 30, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 31, TokenType.LITERAL));
            this.add(new SelectorToken(",", 34, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("baz", 35, TokenType.LITERAL));
            this.add(new SelectorToken("}", 38, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("]", 39, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void リスト内リストの引数で正常にトークン化されること()
    {
        final String testCase = "[foo={{hoge,fuga,piyo},{foo,bar,baz}}]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 5, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("{", 6, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("hoge", 7, TokenType.LITERAL));
            this.add(new SelectorToken(",", 11, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("fuga", 12, TokenType.LITERAL));
            this.add(new SelectorToken(",", 16, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("piyo", 17, TokenType.LITERAL));
            this.add(new SelectorToken("}", 21, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken(",", 22, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("{", 23, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("foo", 24, TokenType.LITERAL));
            this.add(new SelectorToken(",", 27, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 28, TokenType.LITERAL));
            this.add(new SelectorToken(",", 31, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("baz", 32, TokenType.LITERAL));
            this.add(new SelectorToken("}", 35, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("}", 36, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("]", 37, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void マップの引数で正常にトークン化されること()
    {
        final String testCase = "[foo={hoge=fuga,piyo=bar}]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 5, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("hoge", 6, TokenType.LITERAL));
            this.add(new SelectorToken("=", 10, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("fuga", 11, TokenType.LITERAL));
            this.add(new SelectorToken(",", 15, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("piyo", 16, TokenType.LITERAL));
            this.add(new SelectorToken("=", 20, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 21, TokenType.LITERAL));
            this.add(new SelectorToken("}", 24, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("]", 25, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void 複数のマップの引数で正常にトークン化されること()
    {
        final String testCase = "[foo={hoge=fuga,piyo=bar},bar={foo=bar,baz=foo}]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 5, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("hoge", 6, TokenType.LITERAL));
            this.add(new SelectorToken("=", 10, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("fuga", 11, TokenType.LITERAL));
            this.add(new SelectorToken(",", 15, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("piyo", 16, TokenType.LITERAL));
            this.add(new SelectorToken("=", 20, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 21, TokenType.LITERAL));
            this.add(new SelectorToken("}", 24, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken(",", 25, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 26, TokenType.LITERAL));
            this.add(new SelectorToken("=", 29, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 30, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("foo", 31, TokenType.LITERAL));
            this.add(new SelectorToken("=", 34, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 35, TokenType.LITERAL));
            this.add(new SelectorToken(",", 38, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("baz", 39, TokenType.LITERAL));
            this.add(new SelectorToken("=", 42, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("foo", 43, TokenType.LITERAL));
            this.add(new SelectorToken("}", 46, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("]", 47, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    public void マップ内リストの引数で正常にトークン化されること()
    {
        final String testCase = "[foo={hoge={fuga,piyo},bar={foo,bar}}]";
        final LinkedList<SelectorToken> expectedTokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 5, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("hoge", 6, TokenType.LITERAL));
            this.add(new SelectorToken("=", 10, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 11, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("fuga", 12, TokenType.LITERAL));
            this.add(new SelectorToken(",", 16, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("piyo", 17, TokenType.LITERAL));
            this.add(new SelectorToken("}", 21, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken(",", 22, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 23, TokenType.LITERAL));
            this.add(new SelectorToken("=", 26, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("{", 27, TokenType.COLLECTIONS_BEGIN));
            this.add(new SelectorToken("foo", 28, TokenType.LITERAL));
            this.add(new SelectorToken(",", 31, TokenType.PARAMETER_SEPARATOR));
            this.add(new SelectorToken("bar", 32, TokenType.LITERAL));
            this.add(new SelectorToken("}", 35, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("}", 36, TokenType.COLLECTIONS_END));
            this.add(new SelectorToken("]", 37, TokenType.PARAMETER_END));
        }};

        final LinkedList<SelectorToken> actualTokens = SelectorLexer.tokenize(testCase);
        assertTokens(expectedTokens, actualTokens);
    }
}
