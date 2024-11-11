package org.kunlab.scenamatica.scenariofile;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YAMLTypeMismatchException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlMissingKeyValueException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlValueParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class StructuredYamlNodeImpl implements StructuredYamlNode
{
    private static final int DEFAULT_TOP_BOTTOM_OFFSET = 3;

    private final StructuredYamlNodeImpl root;

    private final Map<Object, StructuredYamlNode> childrenCache;

    @Nullable
    private final String fileName;
    @Nullable
    private final String fileContent;
    @Nullable
    private final String keyName;
    @NotNull
    private final Node parentNode;
    @Nullable
    private Node thisNode;

    private StructuredYamlNodeImpl(StructuredYamlNodeImpl root, @Nullable String keyName, @NotNull Node parentNode, @Nullable Node thisNode)
    {
        this.root = root;
        this.keyName = keyName;
        this.parentNode = parentNode;
        this.thisNode = thisNode;

        this.fileName = null;
        this.fileContent = null;

        this.childrenCache = new HashMap<>();
    }

    private StructuredYamlNodeImpl(@NotNull Node thisNode, @Nullable String fileName, @Nullable String fileContent)
    {
        this.root = this;
        this.thisNode = thisNode;
        this.fileName = fileName;
        this.fileContent = fileContent;

        this.keyName = null;
        this.parentNode = thisNode;

        this.childrenCache = new HashMap<>();
    }

    private static StructuredYamlNode createNullNode(StructuredYamlNodeImpl root, String keyName, Node parentNode)
    {
        return new StructuredYamlNodeImpl(root, keyName, parentNode, null);
    }

    public static StructuredYamlNode fromYamlString(@NotNull String fileName, @NotNull String yamlString)
    {
        ParserImpl parser = new ParserImpl(new StreamReader(yamlString));

        Composer composer = new Composer(parser, new Resolver());

        return new StructuredYamlNodeImpl(composer.getSingleNode(), fileName, yamlString);
    }

    public static StructuredYamlNode fromInputStream(@NotNull String fileName, @NotNull InputStream inputStream)
    {
        String fileContent;
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             InputStreamReader isr = new InputStreamReader(bis))
        {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = isr.read(buffer)) != -1)
                sb.append(buffer, 0, read);

            fileContent = sb.toString();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }

        return fromYamlString(fileName, fileContent);
    }

    @Override
    public boolean isType(YAMLNodeType type)
    {
        if (this.thisNode == null)
            return false;

        return type.isTypeOf(this.thisNode.getTag())
                || (type == YAMLNodeType.STRING && this.thisNode instanceof ScalarNode);
    }

    @Override
    public String asString() throws YamlParsingException
    {
        if (!(this.thisNode instanceof ScalarNode))
            this.throwTypeMismatchException(YAMLNodeType.STRING);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return scalarNode.getValue();
    }

    @Override
    public String asString(String defaultValue) throws YamlParsingException
    {
        return this.thisNode == null ? defaultValue: this.asString();
    }

    @Override
    public Integer asInteger() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.INTEGER))
            this.throwTypeMismatchException(YAMLNodeType.INTEGER);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Integer.parseInt(scalarNode.getValue());
    }

    @Override
    public Integer asInteger(Integer defaultValue) throws YamlParsingException
    {
        return this.thisNode == null ? defaultValue: this.asInteger();
    }

    @Override
    public Long asLong() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.INTEGER))
            this.throwTypeMismatchException(YAMLNodeType.INTEGER);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Long.parseLong(scalarNode.getValue());
    }

    @Override
    public Long asLong(Long defaultValue) throws YamlParsingException
    {
        return this.thisNode == null ? defaultValue: this.asLong();
    }

    @Override
    public Boolean asBoolean() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.BOOLEAN))
            this.throwTypeMismatchException(YAMLNodeType.BOOLEAN);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Boolean.parseBoolean(scalarNode.getValue());
    }

    @Override
    public Boolean asBoolean(Boolean defaultValue) throws YamlParsingException
    {
        return this.thisNode == null ? defaultValue: this.asBoolean();
    }

    @Override
    public Float asFloat() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.FLOAT))
            this.throwTypeMismatchException(YAMLNodeType.FLOAT);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Float.parseFloat(scalarNode.getValue());
    }

    @Override
    public Float asFloat(Float defaultValue) throws YamlParsingException
    {
        return this.thisNode == null ? defaultValue: this.asFloat();
    }

    @Override
    public Double asDouble() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.FLOAT))
            this.throwTypeMismatchException(YAMLNodeType.FLOAT);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Double.parseDouble(scalarNode.getValue());
    }

    @Override
    public Double asDouble(Double defaultValue) throws YamlParsingException
    {
        return this.thisNode == null ? defaultValue: this.asDouble();
    }

    @Override
    public Byte asByte() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.INTEGER))
            this.throwTypeMismatchException(YAMLNodeType.INTEGER);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Byte.parseByte(scalarNode.getValue());
    }

    @Override
    public Byte[] asBinary() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;
        else if (!this.isType(YAMLNodeType.BINARY))
            this.throwTypeMismatchException(YAMLNodeType.BINARY);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return IntStream.range(0, scalarNode.getValue().length())
                .mapToObj(scalarNode.getValue()::charAt)
                .map(c -> (byte) c.charValue())
                .toArray(Byte[]::new);
    }

    @Override
    public boolean isNull()
    {
        return this.isType(YAMLNodeType.NULL);
    }

    @Override
    public boolean isNullNode()
    {
        return this.thisNode == null;
    }

    @Override
    public List<StructuredYamlNode> asList() throws YamlParsingException
    {
        return this.asSequenceStream().collect(Collectors.toList());
    }

    @Override
    public <T> List<T> asList(ValueMapper<T> mapper) throws YamlParsingException
    {
        try
        {
            return this.asSequenceStream()
                    .map(node -> {
                        try
                        {
                            return node.getAs(mapper);
                        }
                        catch (YamlParsingException e)
                        {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        }
        catch (RuntimeException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof YamlParsingException)
                throw (YamlParsingException) cause;
            else
                throw e;
        }
    }

    @Override
    public <K, V> @NotNull Map<K, V> asMap(ValueMapper<K> keyMapper, ValueMapper<V> valueMapper) throws YamlParsingException
    {
        try
        {
            return this.asMapStream()
                    .collect(Collectors.toMap(
                            nodePair -> {
                                try
                                {
                                    return keyMapper.map(nodePair.getLeft());
                                }
                                catch (Exception e)
                                {
                                    throw new RuntimeException(e);
                                }
                            },
                            nodePair -> {
                                try
                                {
                                    return valueMapper.map(nodePair.getRight());
                                }
                                catch (Exception e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
        }
        catch (RuntimeException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof YamlParsingException)
                throw (YamlParsingException) cause;
            else
                throw e;
        }
    }

    @Override
    public @NotNull <K, V> Stream<Map.Entry<K, V>> asMapStream(ValueMapper<K> keyMapper, ValueMapper<V> valueMapper) throws YamlParsingException
    {
        try
        {
            return this.asMapStream()
                    .map(nodePair -> {
                        try
                        {
                            return new AbstractMap.SimpleEntry<>(keyMapper.map(nodePair.getLeft()), valueMapper.map(nodePair.getRight()));
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    });
        }
        catch (RuntimeException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof YamlParsingException)
                throw (YamlParsingException) cause;
            else
                throw e;
        }
    }

    private Stream<Pair<StructuredYamlNode, StructuredYamlNode>> asMapStream() throws YamlParsingException
    {
        if (this.thisNode == null)
            return Stream.empty();
        else if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;
        assert mappingNode != null;

        return mappingNode.getValue().stream()
                .filter(nodeTuple -> nodeTuple.getKeyNode() instanceof ScalarNode)
                .map(nodeTuple -> new Pair<>(
                        new StructuredYamlNodeImpl(this.root, null, this.thisNode, nodeTuple.getKeyNode()),
                        new StructuredYamlNodeImpl(this.root, null, this.thisNode, nodeTuple.getValueNode())
                ));
    }

    @Override
    public @NotNull Map<String, Object> asMap() throws YamlParsingException
    {
        return this.asMap(StructuredYamlNode::asString, StructuredYamlNode::asObject);
    }

    @Override
    public @NotNull Map<StructuredYamlNode, StructuredYamlNode> asNodeMap() throws YamlParsingException
    {
        return this.asMapStream()
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public Object asObject() throws YamlParsingException
    {
        if (this.thisNode == null)
            return null;

        switch (YAMLNodeType.fromTag(this.thisNode.getTag()))
        {
            case STRING:
                return this.asString();
            case INTEGER:
                return this.asInteger();
            case FLOAT:
                return this.asDouble();
            case BOOLEAN:
                return this.asBoolean();
            case BINARY:
                return this.asBinary();
            case MAPPING:
                return this.asMap(StructuredYamlNode::asObject, StructuredYamlNode::asObject);
            case LIST:
                return this.asList(StructuredYamlNode::asObject);
            case NULL:
            default:
                return null;
        }
    }

    @Override
    public Stream<StructuredYamlNode> asSequenceStream() throws YamlParsingException
    {
        if (this.thisNode == null)
            return Stream.empty();
        else if (!this.isType(YAMLNodeType.LIST))
            this.throwTypeMismatchException(YAMLNodeType.LIST);

        if (!this.childrenCache.isEmpty())  // キャッシュに１つでも入っていたら, キャッシュは過不足なく構築されている。
            return this.childrenCache.values().stream();

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        sequenceNode.getValue().stream()
                .map(node -> new StructuredYamlNodeImpl(this.root, this.keyName, this.thisNode, node))
                .forEach(node -> this.childrenCache.put(this.childrenCache.size(), node));

        return this.childrenCache.values().stream();
    }

    @Override
    public void addSequenceItem(StructuredYamlNode item) throws YamlParsingException
    {
        if (this.thisNode == null)
            return;
        else if (!this.isType(YAMLNodeType.LIST))
            this.throwTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        sequenceNode.getValue().add(item.getThisNode());

        this.childrenCache.put(this.childrenCache.size(), item);
    }

    @Override
    public void removeSequenceItem(StructuredYamlNode item) throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.LIST))
            this.throwTypeMismatchException(YAMLNodeType.LIST);

        this.childrenCache.values().remove(item);
        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        if (sequenceNode == null)
            return;
        sequenceNode.getValue().remove(item.getThisNode());
    }

    @Override
    public List<? extends Pair<? extends StructuredYamlNode, ? extends StructuredYamlNode>> getMappingEntries() throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;
        if (mappingNode == null)
            return Collections.emptyList();

        return mappingNode.getValue().stream()
                .map(nodeTuple -> new Pair<>(
                        new StructuredYamlNodeImpl(this.root, this.keyName, this.thisNode, nodeTuple.getKeyNode()),
                        new StructuredYamlNodeImpl(this.root, nodeTuple.getKeyNode().toString(), this.thisNode, nodeTuple.getValueNode())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void clearItems()
    {
        if (this.thisNode == null)
            return;

        this.childrenCache.clear();
        if (this.isType(YAMLNodeType.LIST))
        {
            SequenceNode sequenceNode = (SequenceNode) this.thisNode;
            sequenceNode.getValue().clear();
        }
        else if (this.isType(YAMLNodeType.MAPPING))
        {
            MappingNode mappingNode = (MappingNode) this.thisNode;
            mappingNode.getValue().clear();
        }

    }

    @Override
    public @NotNull String getFileContent()
    {
        assert this.root.fileContent != null;
        return this.root.fileContent;
    }

    @Override
    public @NotNull String getFileName()
    {
        assert this.root.fileName != null;
        return this.root.fileName;
    }

    @Override
    public int getStartLine()
    {
        if (this.thisNode == null)
            return this.parentNode.getStartMark().getLine();
        return this.thisNode.getStartMark().getLine();
    }

    @Override
    public int getEndLine()
    {
        if (this.thisNode == null)
            return this.parentNode.getEndMark().getLine();
        return this.thisNode.getEndMark().getLine();
    }

    private String[] getLines(int errorLine, int topBottomOffset)
    {
        if (errorLine < 0)
            return new String[0];

        String[] lines = this.getFileContent().split("\n");

        int start = Math.max(0, errorLine - topBottomOffset);
        int end = Math.min(lines.length, errorLine + topBottomOffset + 1);

        String[] result = new String[end - start];
        System.arraycopy(lines, start, result, 0, end - start);

        return result;
    }

    private static StructuredYamlNode createNodeByMap(Map<?, ?> map)
    {
        List<NodeTuple> nodes = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            YAMLNodeType keyType = getTypeByObject(entry.getKey());
            YAMLNodeType valueType = getTypeByObject(entry.getValue());

            if (keyType == null || valueType == null)
                throw new IllegalArgumentException("Unsupported type: " + entry.getKey().getClass() + ", " + entry.getValue().getClass());

            ScalarNode keyNode = new ScalarNode(keyType.getTag(), entry.getKey().toString(), null, null, DumperOptions.ScalarStyle.PLAIN);
            ScalarNode valueNode = new ScalarNode(valueType.getTag(), entry.getValue().toString(), null, null, DumperOptions.ScalarStyle.PLAIN);
            nodes.add(new NodeTuple(keyNode, valueNode));
        }

        return new StructuredYamlNodeImpl(new MappingNode(Tag.MAP, nodes, DumperOptions.FlowStyle.AUTO), null, null);
    }

    @Override
    public StructuredYamlNode changeScalarValue(YAMLNodeType scalarType, Object value)
    {
        this.thisNode = new ScalarNode(
                scalarType.getTag(),
                value.toString(),
                this.thisNode == null ? null: this.thisNode.getStartMark(),
                this.thisNode == null ? null: this.thisNode.getEndMark(),
                DumperOptions.ScalarStyle.PLAIN
        );

        return this;
    }

    @Override
    public boolean containsKey(Object key)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        if (this.childrenCache.containsKey(key))
            return true;  // キャッシュに有る場合はそれを返す。
        MappingNode mappingNode = (MappingNode) this.thisNode;
        assert mappingNode != null;

        for (NodeTuple nodeTuple : mappingNode.getValue())
        {
            Node keyNode = nodeTuple.getKeyNode();
            if (keyNode instanceof ScalarNode)
            {
                ScalarNode scalarNode = (ScalarNode) keyNode;
                if (Objects.equals(scalarNode.getValue(), key))  // key が null の場合もある。
                    return true;
            }
        }

        return false;
    }

    @Override
    public void add(StructuredYamlNode key, StructuredYamlNode value) throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;
        assert mappingNode != null;
        mappingNode.getValue().add(new NodeTuple(key.getThisNode(), value.getThisNode()));
    }

    @Override
    public void remove(Object key)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        this.childrenCache.remove(key);  // キャッシュにゴミが残る可能性があるので, 削除しておく。
        MappingNode mappingNode = (MappingNode) this.thisNode;
        assert mappingNode != null;

        Object realKey = key;
        if (key instanceof StructuredYamlNode)
            realKey = ((StructuredYamlNode) key).asString();

        List<NodeTuple> tuples = mappingNode.getValue();
        for (NodeTuple nodeTuple : tuples)
        {
            Node keyNode = nodeTuple.getKeyNode();
            if (!(keyNode instanceof ScalarNode))
                continue;

            ScalarNode scalarNode = (ScalarNode) keyNode;
            if (Objects.equals(scalarNode.getValue(), realKey))  // key が null の場合もある。
            {
                tuples.remove(nodeTuple);
                return;
            }
        }
    }

    @Override
    public void mergeMapping(StructuredYamlNode other)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);
        else if (!other.isType(YAMLNodeType.MAPPING))  // これは明確な想定されていない例外なので。
            throw new IllegalArgumentException("Other node is not a mapping");

        MappingNode thisMappingNode = (MappingNode) this.thisNode;
        MappingNode otherMappingNode = (MappingNode) other.getThisNode();
        assert thisMappingNode != null;
        assert otherMappingNode != null;

        for (NodeTuple otherTuple : otherMappingNode.getValue())
        {
            Node otherKeyNode = otherTuple.getKeyNode();
            if (!(otherKeyNode instanceof ScalarNode))
                continue;

            ScalarNode otherScalarNode = (ScalarNode) otherKeyNode;
            String otherKey = otherScalarNode.getValue();

            // 既存のキーがあれば上書き、なければ追加する。↓ループは存在するまで探索する。
            NodeTuple tupleToRemove = null;
            NodeTuple tupleToAdd = null;
            List<NodeTuple> thisTuples = thisMappingNode.getValue();
            for (NodeTuple thisTuple : thisTuples)
            {
                Node thisKeyNode = thisTuple.getKeyNode();
                if (!(thisKeyNode instanceof ScalarNode))
                    continue;

                ScalarNode thisScalarNode = (ScalarNode) thisKeyNode;
                String thisKey = thisScalarNode.getValue();

                if (Objects.equals(thisKey, otherKey))
                {
                    // ここで追加・削除すると ConcurrentModificationException が発生する。
                    tupleToRemove = thisTuple;
                    tupleToAdd = new NodeTuple(thisTuple.getKeyNode(), otherTuple.getValueNode());
                    break;
                }
            }

            if (tupleToRemove == null)
                thisTuples.add(otherTuple);
            else
            {
                thisTuples.remove(tupleToRemove);
                thisTuples.add(tupleToAdd);
            }
        }

        // マージ後は不明値が残る可能性があるので, キャッシュをクリアしておく。
        this.childrenCache.clear();
    }

    private static YAMLNodeType getTypeByObject(Object obj)
    {
        if (obj == null)
            return YAMLNodeType.NULL;
        else if (obj instanceof String)
            return YAMLNodeType.STRING;
        else if (obj instanceof Integer)
            return YAMLNodeType.INTEGER;
        else if (obj instanceof Long)
            return YAMLNodeType.INTEGER;
        else if (obj instanceof Boolean)
            return YAMLNodeType.BOOLEAN;
        else if (obj instanceof Float)
            return YAMLNodeType.FLOAT;
        else if (obj instanceof Double)
            return YAMLNodeType.FLOAT;
        else if (obj instanceof Byte)
            return YAMLNodeType.INTEGER;
        else if (obj instanceof Byte[])
            return YAMLNodeType.BINARY;
        else if (obj instanceof Map)
            return YAMLNodeType.MAPPING;
        else if (obj instanceof List)
            return YAMLNodeType.LIST;
        else
            return null;
    }

    private void throwTypeMismatchException(YAMLNodeType expectedType) throws YamlParsingException
    {
        this.ensureValuePresent();

        assert this.thisNode != null;
        int errorLine = this.thisNode.getStartMark().getLine();
        throw new YAMLTypeMismatchException(
                this.getFileName(),
                this.keyName,
                errorLine,
                this.getLines(errorLine, DEFAULT_TOP_BOTTOM_OFFSET),
                expectedType,
                YAMLNodeType.fromTag(this.thisNode.getTag())
        );
    }

    // Mapping の場合は, 子を取得できる。
    @Override
    public StructuredYamlNode get(Object key)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        if (key instanceof StructuredYamlNode)
            key = ((StructuredYamlNode) key).asString();

        if (this.childrenCache.containsKey(key))
            return this.childrenCache.get(key);  // キャッシュに有る場合はそれを返す。

        MappingNode mappingNode = (MappingNode) this.thisNode;
        assert mappingNode != null;

        for (NodeTuple nodeTuple : mappingNode.getValue())
        {
            Node keyNode = nodeTuple.getKeyNode();
            if (keyNode instanceof ScalarNode)
            {
                ScalarNode scalarNode = (ScalarNode) keyNode;
                if (!Objects.equals(scalarNode.getValue(), key))  // key が null の場合もある。
                    continue;

                StructuredYamlNode node = new StructuredYamlNodeImpl(this.root, scalarNode.getValue(), this.thisNode, nodeTuple.getValueNode());
                this.childrenCache.put(key, node);  // キャッシュに保存しておく。
                return node;
            }
        }

        return createNullNode(this.root, key.toString(), this.thisNode);
    }

    @Override
    public int size()
    {
        if (this.thisNode == null)
            return -1;
        else if (this.isType(YAMLNodeType.LIST))
        {
            SequenceNode sequenceNode = (SequenceNode) this.thisNode;
            return sequenceNode.getValue().size();
        }
        else if (this.isType(YAMLNodeType.MAPPING))
        {
            MappingNode mappingNode = (MappingNode) this.thisNode;
            return mappingNode.getValue().size();
        }
        else if (this.isType(YAMLNodeType.STRING))
        {
            ScalarNode scalarNode = (ScalarNode) this.thisNode;
            return scalarNode.getValue().length();
        }
        else
            return 0;
    }

    @Override
    public void ensureTypeOf(YAMLNodeType... types) throws YamlParsingException
    {
        if (types.length == 0)
            return;

        for (YAMLNodeType type : types)
            if (this.isType(type) || type == YAMLNodeType.NULL && this.thisNode == null)
                return;

        if (this.thisNode == null)
            this.ensureValuePresent();

        throw new YAMLTypeMismatchException(
                this.getFileName(),
                this.keyName,
                this.thisNode.getStartMark().getLine(),
                this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET),
                types[0],
                YAMLNodeType.fromTag(this.thisNode.getTag())
        );
    }

    @Override
    public void ensureTypeOfIfExists(YAMLNodeType... types) throws YamlParsingException
    {
        if (this.thisNode != null)
            this.ensureTypeOf(types);
    }

    private void ensureValuePresent() throws YamlParsingException
    {
        if (this.thisNode == null)
            throw new YamlMissingKeyValueException(
                    "No value found for key: " + this.keyName,
                    this.getFileName(),
                    this.keyName,
                    this.parentNode.getStartMark().getLine(),
                    this.getLines(this.parentNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET)
            );
    }

    @Override
    public <T> T getAs(ValueMapper<T> mapper) throws YamlParsingException
    {
        this.ensureValuePresent();
        try
        {
            return mapper.map(this);
        }
        catch (Exception e)
        {
            throw new YamlValueParsingException(
                    "Failed to parse value",
                    this.getFileName(),
                    null,
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET),
                    e
            );
        }
    }

    @Override
    public <T> T getAs(ValueMapper<T> mapper, T defaultValue) throws YamlParsingException
    {
        if (this.thisNode == null)
            return defaultValue;
        else
            return this.getAs(mapper);
    }

    @Override
    public Set<StructuredYamlNode> keys() throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            this.throwTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;
        assert mappingNode != null;

        return mappingNode.getValue().stream()
                .map(NodeTuple::getKeyNode)
                .filter(node -> node instanceof ScalarNode)
                .map(node -> new StructuredYamlNodeImpl(this.root, this.keyName, this.thisNode, node))
                .collect(Collectors.toSet());
    }

    @Override
    public StructuredYamlNode getItem(int index)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.LIST))
            this.throwTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        assert sequenceNode != null;

        if (index < 0 || index >= sequenceNode.getValue().size())
            throw new YamlParsingException(
                    "Index out of range: " + index + (sequenceNode.getValue().isEmpty() ? ", but no item found": ", Size: " + sequenceNode.getValue().size()),
                    this.getFileName(),
                    this.keyName,
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET)
            );

        if (this.childrenCache.containsKey(index))
            return this.childrenCache.get(index);  // キャッシュに有る場合はそれを返す。

        Node node = sequenceNode.getValue().get(index);
        StructuredYamlNode result = new StructuredYamlNodeImpl(this.root, this.keyName, this.thisNode, node);
        this.childrenCache.put(index, result);  // キャッシュに保存しておく。

        return result;
    }

    @Override
    public void validateIfExists(Validator validator) throws YamlParsingException
    {
        if (this.thisNode != null)
            this.validate(validator, null);
    }

    @Override
    public void validateIfExists(Validator validator, @Nullable String message) throws YamlParsingException
    {
        if (this.thisNode != null)
            this.validate(validator, message);
    }

    @Override
    public void validate(Validator validator) throws YamlParsingException
    {
        this.validate(validator, null);
    }

    public void validate(Validator validator, @Nullable String message) throws YamlParsingException
    {
        if (this.thisNode == null)
            throw new YamlMissingKeyValueException(
                    "No value found for key: " + this.keyName,
                    this.getFileName(),
                    this.keyName,
                    this.parentNode.getStartMark().getLine(),
                    this.getLines(this.parentNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET)
            );

        try
        {
            validator.validate(this);
        }
        catch (YamlParsingException e)
        {
            throw e;
        }
        catch (Throwable e)
        {

            String fullMessage;
            if (message == null)
            {
                if (validator.getMessage() == null)
                    fullMessage = "Validation failed, caused by " + e.getMessage();
                else
                    fullMessage = validator.getMessage() + ", (caused by " + e.getMessage() + ")";
            }
            else
                fullMessage = message + ", (caused by " + e.getMessage() + ")";

            e.printStackTrace();

            throw new YamlValueParsingException(
                    "Validation failed: " + fullMessage,
                    this.getFileName(),
                    this.keyName,
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET),
                    e
            );
        }
    }

    @Override
    public boolean test(Validator validator) throws YamlParsingException
    {
        if (this.thisNode == null)
            return false;

        try
        {
            validator.validate(this);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    @Override
    public StructuredYamlNode renewByMap(Map<?, ?> map)
    {
        return createNodeByMap(map);
    }
}
