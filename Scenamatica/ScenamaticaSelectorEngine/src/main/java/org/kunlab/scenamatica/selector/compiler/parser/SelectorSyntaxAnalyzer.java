package org.kunlab.scenamatica.selector.compiler.parser;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.selector.compiler.SelectorSyntaxErrorException;
import org.kunlab.scenamatica.selector.compiler.lexer.SelectorToken;
import org.kunlab.scenamatica.selector.compiler.lexer.TokenType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class SelectorSyntaxAnalyzer
{
    public static SyntaxTree analyze(LinkedList<? extends SelectorToken> tokens)
    {
        return new SyntaxTree(
                SyntaxType.SELECTOR,
                null,
                analyzeApex(tokens)
        );
    }

    @Nullable
    private static SyntaxTree analyzeType(ListIterator<? extends SelectorToken> tokens)
    {
        int processed = 0;
        while (tokens.hasNext())
        {
            SelectorToken token = tokens.next();
            if (token.getType() == TokenType.TYPE)
            {
                if (!tokens.hasNext())
                    throw SelectorSyntaxErrorException.unexpectedEnd(token.getToken(), "type", token.getIndex());
                SelectorToken type = tokens.next();
                return new SyntaxTree(
                        SyntaxType.TYPE,
                        type.getToken(),
                        null
                );
            }
            else if (token.getType() == TokenType.PARAMETER_BEGIN)
            {
                tokens.previous();  // パラメータ開始までもどす。
                return null;
            }

            processed++;
        }

        // 最後まで見つからなかった。

        for (int i = 0; i < processed; i++)
            tokens.previous();

        return null;
    }

    private static SyntaxTree[] analyzeApex(LinkedList<? extends SelectorToken> tokens)
    {
        ListIterator<? extends SelectorToken> iterator = tokens.listIterator();
        SyntaxTree typeTree = analyzeType(iterator);
        SyntaxTree[] parameterTree = analyzeParameters(iterator);

        SyntaxTree[] result = Stream.of(new SyntaxTree[]{typeTree}, parameterTree)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .toArray(SyntaxTree[]::new);

        if (result.length == 0)
            return null;
        else
            return result;
    }

    private static SyntaxTree[] analyzeParameters(ListIterator<? extends SelectorToken> iterator)
    {
        if (!iterator.hasNext())
            return null;

        int i = 0;
        boolean found = false;
        while (iterator.hasNext())
        {
            i++;
            SelectorToken token = iterator.next();
            if (token.getType() == TokenType.PARAMETER_BEGIN)
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            for (int j = 0; j < i; j++)
                iterator.previous();
            return null;
        }


        return analyzeParameterBody(iterator);
    }

    private static SyntaxTree[] analyzeParameterBody(ListIterator<? extends SelectorToken> iterator)
    {
        LinkedList<SyntaxTree> results = new LinkedList<>();

        ConstructingParameter tempParm = new ConstructingParameter();
        Map<Integer, ConstructingCollection> tempNestedCollections = new HashMap<>();
        int depth = 0;
        TokenType phase = null;
        loop:
        while (iterator.hasNext())
        {
            SelectorToken token = iterator.next();
            switch (token.getType())
            {
                case PARAMETER_END:
                    break loop;
                case PARAMETER_SEPARATOR:
                    if (!(phase == TokenType.PARAMETER_KEY
                            || phase == TokenType.COLLECTIONS_KEY
                            || phase == TokenType.COLLECTIONS_BEGIN
                            || (depth == 0))
                    )
                        throw SelectorSyntaxErrorException.unexpectedDeclare(token.getToken(), "parameter value", token.getIndex());
                    break;
                case LITERAL:
                    if (phase == null)
                    {
                        tempParm.setKey(token.getToken());
                        phase = TokenType.PARAMETER_KEY;
                        break;
                    }

                    switch (phase)
                    {
                        case PARAMETER_KEY:
                            tempParm.setValue(new SyntaxTree(SyntaxType.VALUE, token.getToken(), null));
                            results.add(tempParm.construct());
                            phase = null;
                            break;
                        case COLLECTIONS_BEGIN:
                            ConstructingCollection collection = tempNestedCollections.get(depth);
                            if (collection.canOpenParameter())
                            {
                                collection.openParameter().setKey(token.getToken());
                                phase = TokenType.COLLECTIONS_KEY;
                            }
                            else
                                collection.add(token.getToken());

                            break;
                        case COLLECTIONS_KEY:
                            ConstructingCollection valueCollection = tempNestedCollections.get(depth);
                            assert valueCollection != null;
                            valueCollection.openParameter().setValue(new SyntaxTree(SyntaxType.VALUE, token.getToken(), null));
                            valueCollection.closeParameter();
                            phase = TokenType.COLLECTIONS_BEGIN;
                            break;
                    }

                    break;
                case COLLECTIONS_BEGIN:
                    if (!(phase == TokenType.COLLECTIONS_KEY
                            || phase == TokenType.PARAMETER_KEY
                            || phase == TokenType.COLLECTIONS_BEGIN))  // = Value
                        throw SelectorSyntaxErrorException.unexpectedDeclare(token.getToken(), "collections", token.getIndex());
                    tempNestedCollections.put(++depth, guessType(iterator));
                    phase = TokenType.COLLECTIONS_BEGIN;
                    break;
                case COLLECTIONS_END:
                    if (!(phase == TokenType.COLLECTIONS_BEGIN || phase == null))
                        throw SelectorSyntaxErrorException.unexpectedDeclare(token.getToken(), "collections end", token.getIndex());
                    else if (depth == 0)
                        throw SelectorSyntaxErrorException.unexpectedDeclare(token.getToken(), "collections end", token.getIndex());


                    SyntaxTree value = tempNestedCollections.get(depth).construct();
                    tempNestedCollections.remove(depth--);
                    if (depth >= 1)
                    {
                        ConstructingCollection collection = tempNestedCollections.get(depth);
                        assert collection != null;
                        if (collection.canOpenParameter())
                        {
                            collection.openParameter().setValue(value);
                            collection.closeParameter();
                        }
                        else
                        {
                            tempParm.setValue(value);
                            collection.add(value);
                        }

                        phase = TokenType.COLLECTIONS_BEGIN;
                    }
                    else
                    {
                        tempParm.setValue(value);
                        results.add(tempParm.construct());
                        phase = null;
                    }
                    break;
                case NEGATE:
                    if (phase == null)
                        throw SelectorSyntaxErrorException.unexpectedDeclare(token.getToken(), "negate", token.getIndex());
                    if (phase == TokenType.PARAMETER_KEY)
                        tempParm.setDoNegate(true);
                    break;
            }
        }

        if (phase != null)
            throw SelectorSyntaxErrorException.unexpectedEnd(iterator.toString(), "parameter", iterator.nextIndex());

        return results.toArray(new SyntaxTree[0]);
    }

    private static ConstructingCollection guessType(ListIterator<? extends SelectorToken> tokens)
    {
        int i = 0;
        ConstructingCollection result = null;
        int literalCount = 0;
        while (tokens.hasNext())
        {
            i++;
            SelectorToken token = tokens.next();
            if (token.getType() == TokenType.LITERAL)
            {
                literalCount++;
                continue;
            }

            if (literalCount < 2 && token.getType() == TokenType.KEY_VALUE_SEPARATOR)
            {
                result = new ConstructingMap();
                break;
            }

            if (token.getType() == TokenType.COLLECTIONS_BEGIN
                    || token.getType() == TokenType.COLLECTIONS_END)
            {
                result = new ConstructingList();
                break;
            }
        }

        for (int j = 0; j < i; j++)
            tokens.previous(); // 進めた分だけ戻す。

        if (result == null)
            result = new ConstructingList();

        return result;
    }

    private interface ConstructingCollection
    {
        void add(String key, String value);

        void add(String value);

        boolean canOpenParameter();

        ConstructingParameter openParameter();

        void closeParameter();

        void add(SyntaxTree value);

        void add(String key, SyntaxTree value);

        SyntaxTree construct();
    }

    @Data
    private static class ConstructingParameter
    {
        private String key;
        private SyntaxTree value;
        private boolean doNegate;

        public SyntaxTree construct()
        {
            if (this.key == null)
                throw new IllegalStateException("key is null");

            SyntaxTree value = this.value;
            if (this.doNegate)
                value = new SyntaxTree(SyntaxType.NEGATE, null, new SyntaxTree[]{value});

            SyntaxTree tree = new SyntaxTree(
                    SyntaxType.PROPERTY,
                    null,
                    new SyntaxTree[]{
                            new SyntaxTree(SyntaxType.KEY, this.key, null),
                            value
                    }
            );

            this.reset();
            return tree;
        }

        public void reset()
        {
            this.key = null;
            this.value = null;
        }
    }

    private static class ConstructingList implements ConstructingCollection
    {
        private final List<SyntaxTree> list;

        public ConstructingList()
        {
            this.list = new LinkedList<>();
        }

        @Override
        public void add(String key, String value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(String value)
        {
            this.list.add(new SyntaxTree(SyntaxType.VALUE, value, null));
        }

        @Override
        public boolean canOpenParameter()
        {
            return false;
        }

        @Override
        public ConstructingParameter openParameter()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(SyntaxTree value)
        {
            this.list.add(value);
        }

        @Override
        public void add(String key, SyntaxTree value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void closeParameter()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public SyntaxTree construct()
        {
            return new SyntaxTree(
                    SyntaxType.COLLECTION,
                    null,
                    this.list.toArray(new SyntaxTree[0])
            );
        }
    }

    private static class ConstructingMap implements ConstructingCollection
    {
        private final Map<String, SyntaxTree> map;

        private final ConstructingParameter tempParameter;

        public ConstructingMap()
        {
            this.map = new LinkedHashMap<>();
            this.tempParameter = new ConstructingParameter();
        }

        @Override
        public void add(String key, String value)
        {
            this.map.put(key, new SyntaxTree(SyntaxType.VALUE, value, null));
        }

        @Override
        public void add(String value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(SyntaxTree value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(String key, SyntaxTree value)
        {
            this.map.put(key, value);
        }

        @Override
        public boolean canOpenParameter()
        {
            return true;
        }

        @Override
        public ConstructingParameter openParameter()
        {
            return this.tempParameter;
        }

        @Override
        public void closeParameter()
        {
            this.map.put(this.tempParameter.getKey(), this.tempParameter.getValue());
            this.tempParameter.reset();
        }

        @Override
        public SyntaxTree construct()
        {
            String[] keys = this.map.keySet().toArray(new String[0]);
            SyntaxTree[] values = this.map.values().toArray(new SyntaxTree[0]);

            SyntaxTree[] kvList = new SyntaxTree[this.map.size()];
            for (int i = 0; i < this.map.size(); i++)
            {
                SyntaxTree key = new SyntaxTree(SyntaxType.KEY, keys[i], null);
                SyntaxTree value = values[i];
                kvList[i] = new SyntaxTree(SyntaxType.PROPERTY, null, new SyntaxTree[]{key, value});
            }

            return new SyntaxTree(SyntaxType.COLLECTION, null, kvList);
        }
    }
}
