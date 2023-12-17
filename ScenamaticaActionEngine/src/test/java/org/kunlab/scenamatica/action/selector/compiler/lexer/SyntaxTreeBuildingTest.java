package org.kunlab.scenamatica.action.selector.compiler.lexer;

import org.junit.Test;
import org.kunlab.scenamatica.action.selector.compiler.parser.SelectorSyntaxAnalyzer;
import org.kunlab.scenamatica.action.selector.compiler.parser.SyntaxTree;
import org.kunlab.scenamatica.action.selector.compiler.parser.SyntaxType;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SyntaxTreeBuildingTest
{
    private static void assertTree(SyntaxTree expected, SyntaxTree actual)
    {
        if (expected == null)
        {
            assertNull(actual);
            return;
        }

        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getValue(), actual.getValue());
        if (expected.getChildren() == null)
        {
            assertNull(actual.getChildren());
            return;
        }

        assertEquals(expected.getChildren().length, actual.getChildren().length);
        for (int i = 0; i < expected.getChildren().length; i++)
            assertTree(expected.getChildren()[i], actual.getChildren()[i]);
    }

    @Test
    public void 空のツリーが正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<>();
        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, null);

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void タイプのみで正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("@", 0, TokenType.TYPE));
            this.add(new SelectorToken("a", 1, TokenType.LITERAL));
        }};
        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.TYPE, "a", null)
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void タイプと空引数のみで正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("@", 0, TokenType.TYPE));
            this.add(new SelectorToken("a", 1, TokenType.LITERAL));
            this.add(new SelectorToken("[", 2, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("]", 3, TokenType.PARAMETER_END));
        }};

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.TYPE, "a", null)
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void 空引数のみで正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("]", 1, TokenType.PARAMETER_END));
        }};

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, null);

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void タイプと引数のみで正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("@", 0, TokenType.TYPE));
            this.add(new SelectorToken("a", 1, TokenType.LITERAL));
            this.add(new SelectorToken("[", 2, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 3, TokenType.LITERAL));
            this.add(new SelectorToken("=", 6, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 7, TokenType.LITERAL));
            this.add(new SelectorToken("]", 10, TokenType.PARAMETER_END));
        }};

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.TYPE, "a", null),
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.VALUE, "bar", null)
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void 引数のみで正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
        {{
            this.add(new SelectorToken("[", 0, TokenType.PARAMETER_BEGIN));
            this.add(new SelectorToken("foo", 1, TokenType.LITERAL));
            this.add(new SelectorToken("=", 4, TokenType.KEY_VALUE_SEPARATOR));
            this.add(new SelectorToken("bar", 5, TokenType.LITERAL));
            this.add(new SelectorToken("]", 8, TokenType.PARAMETER_END));
        }};

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.VALUE, "bar", null)
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void 複数の引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.VALUE, "bar", null)
                }),
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "bar", null),
                        new SyntaxTree(SyntaxType.VALUE, "foo", null)
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void リストの引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.VALUE, "hoge", null),
                                new SyntaxTree(SyntaxType.VALUE, "fuga", null),
                                new SyntaxTree(SyntaxType.VALUE, "piyo", null)
                        })
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void 複数のリストの引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.VALUE, "hoge", null),
                                new SyntaxTree(SyntaxType.VALUE, "fuga", null),
                                new SyntaxTree(SyntaxType.VALUE, "piyo", null)
                        })
                }),
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "bar", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.VALUE, "foo", null),
                                new SyntaxTree(SyntaxType.VALUE, "bar", null),
                                new SyntaxTree(SyntaxType.VALUE, "baz", null)
                        })
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void リスト内リストの引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.VALUE, "hoge", null),
                                        new SyntaxTree(SyntaxType.VALUE, "fuga", null),
                                        new SyntaxTree(SyntaxType.VALUE, "piyo", null)
                                }),
                                new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.VALUE, "foo", null),
                                        new SyntaxTree(SyntaxType.VALUE, "bar", null),
                                        new SyntaxTree(SyntaxType.VALUE, "baz", null)
                                })
                        })
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void マップの引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "hoge", null),
                                        new SyntaxTree(SyntaxType.VALUE, "fuga", null)
                                }),
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "piyo", null),
                                        new SyntaxTree(SyntaxType.VALUE, "bar", null)
                                })
                        })
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void 複数のマップの引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "hoge", null),
                                        new SyntaxTree(SyntaxType.VALUE, "fuga", null)
                                }),
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "piyo", null),
                                        new SyntaxTree(SyntaxType.VALUE, "bar", null)
                                })
                        })
                }),
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "bar", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                                        new SyntaxTree(SyntaxType.VALUE, "bar", null)
                                }),
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "baz", null),
                                        new SyntaxTree(SyntaxType.VALUE, "foo", null)
                                })
                        })
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }

    @Test
    public void マップ内リストの引数で正常に構築されること()
    {
        final LinkedList<SelectorToken> tokens = new LinkedList<SelectorToken>()
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

        final SyntaxTree expected = new SyntaxTree(SyntaxType.SELECTOR, null, new SyntaxTree[]{
                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                        new SyntaxTree(SyntaxType.KEY, "foo", null),
                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "hoge", null),
                                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                                new SyntaxTree(SyntaxType.VALUE, "fuga", null),
                                                new SyntaxTree(SyntaxType.VALUE, "piyo", null)
                                        })
                                }),
                                new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{
                                        new SyntaxTree(SyntaxType.KEY, "bar", null),
                                        new SyntaxTree(SyntaxType.COLLECTION, null, new SyntaxTree[]{
                                                new SyntaxTree(SyntaxType.VALUE, "foo", null),
                                                new SyntaxTree(SyntaxType.VALUE, "bar", null)
                                        })
                                })
                        })
                })
        });

        SyntaxTree actual = SelectorSyntaxAnalyzer.analyze(tokens);
        assertTree(expected, actual);
    }
}
