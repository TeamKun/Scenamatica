package org.kunlab.scenamatica.scenariofile;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YAMLTypeMismatchException;
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
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class StructuredYamlNodeImpl implements StructuredYamlNode
{
    private static final int DEFAULT_TOP_BOTTOM_OFFSET = 3;

    private final StructuredYamlNodeImpl root;
    @Nullable
    private final String fileName;
    @Nullable
    private final String fileContent;
    private Node thisNode;

    private StructuredYamlNodeImpl(StructuredYamlNodeImpl root, Node thisNode)
    {
        this.root = root;
        this.thisNode = thisNode;

        this.fileName = null;
        this.fileContent = null;
    }

    private StructuredYamlNodeImpl(Node thisNode, String fileName, String fileContent)
    {
        this.root = this;
        this.thisNode = thisNode;
        this.fileName = fileName;
        this.fileContent = fileContent;
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
        return type.isTypeOf(this.thisNode.getTag());
    }

    @Override
    public String asString()
    {
        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return scalarNode.getValue();
    }

    @Override
    public int asInt() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.INTEGER))
            throw this.createTypeMismatchException(YAMLNodeType.INTEGER);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Integer.parseInt(scalarNode.getValue());
    }

    @Override
    public boolean asBoolean() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.BOOLEAN))
            throw this.createTypeMismatchException(YAMLNodeType.BOOLEAN);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Boolean.parseBoolean(scalarNode.getValue());
    }

    @Override
    public float asFloat() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.FLOAT))
            throw this.createTypeMismatchException(YAMLNodeType.FLOAT);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return Float.parseFloat(scalarNode.getValue());
    }

    @Override
    public byte[] asBinary() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.BINARY))
            throw this.createTypeMismatchException(YAMLNodeType.BINARY);

        ScalarNode scalarNode = (ScalarNode) this.thisNode;
        return scalarNode.getValue().getBytes();
    }

    @Override
    public boolean isNull() throws YAMLTypeMismatchException
    {
        return this.isType(YAMLNodeType.NULL);
    }

    @Override
    public List<StructuredYamlNode> asList() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.LIST))
            throw this.createTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        return sequenceNode.getValue().stream()
                .map(node -> new StructuredYamlNodeImpl(this.root, node))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<StructuredYamlNode> asSequenceStream() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.LIST))
            throw this.createTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        return sequenceNode.getValue().stream()
                .map(node -> new StructuredYamlNodeImpl(this.root, node));
    }

    @Override
    public void addSequenceItem(StructuredYamlNode item) throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.LIST))
            throw this.createTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        sequenceNode.getValue().add(item.getThisNode());
    }

    @Override
    public void removeSequenceItem(StructuredYamlNode item) throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.LIST))
            throw this.createTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;
        sequenceNode.getValue().remove(item.getThisNode());
    }

    @Override
    public List<? extends Pair<? extends StructuredYamlNode, ? extends StructuredYamlNode>> getMappingEntries() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;

        return mappingNode.getValue().stream()
                .map(nodeTuple -> new Pair<>(
                        new StructuredYamlNodeImpl(this.root, nodeTuple.getKeyNode()),
                        new StructuredYamlNodeImpl(this.root, nodeTuple.getValueNode())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void clearItems()
    {
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
        return this.thisNode.getStartMark().getLine();
    }

    @Override
    public int getEndLine()
    {
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

    private YAMLTypeMismatchException createTypeMismatchException(YAMLNodeType expectedType)
    {
        int errorLine = this.thisNode.getStartMark().getLine();
        return new YAMLTypeMismatchException(
                this.getFileName(),
                errorLine,
                this.getLines(errorLine, DEFAULT_TOP_BOTTOM_OFFSET),
                expectedType,
                YAMLNodeType.fromTag(this.thisNode.getTag())
        );
    }

    @Override
    public StructuredYamlNode changeScalarValue(YAMLNodeType scalarType, Object value)
    {
        this.thisNode = new ScalarNode(
                scalarType.getTag(),
                value.toString(),
                this.thisNode.getStartMark(),
                this.thisNode.getEndMark(),
                DumperOptions.ScalarStyle.PLAIN
        );

        return this;
    }

    @Override
    public boolean containsKey(Object key)
            throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;

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
    public void add(StructuredYamlNode key, StructuredYamlNode value) throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;
        mappingNode.getValue().add(new NodeTuple(key.getThisNode(), value.getThisNode()));
    }

    @Override
    public void remove(Object key)
            throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;

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
            throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);
        else if (!other.isType(YAMLNodeType.MAPPING))  // これは明確な想定されていない例外なので。
            throw new IllegalArgumentException("Other node is not a mapping");

        MappingNode thisMappingNode = (MappingNode) this.thisNode;
        MappingNode otherMappingNode = (MappingNode) other.getThisNode();

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
    }

    // Mapping の場合は, 子を取得できる。
    @Override
    public StructuredYamlNode get(Object key)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;

        for (NodeTuple nodeTuple : mappingNode.getValue())
        {
            Node keyNode = nodeTuple.getKeyNode();
            if (keyNode instanceof ScalarNode)
            {
                ScalarNode scalarNode = (ScalarNode) keyNode;
                if (Objects.equals(scalarNode.getValue(), key))  // key が null の場合もある。
                    return new StructuredYamlNodeImpl(this.root, nodeTuple.getValueNode());
            }
        }

        throw new YamlParsingException(
                "Key \"" + key + "\" not found",
                this.getFileName(),
                Objects.toString(key),
                this.thisNode.getStartMark().getLine(),
                this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET)
        );
    }

    @Override
    public void ensureTypeOf(Object key, YAMLNodeType type) throws YamlParsingException
    {
        StructuredYamlNode node = this.get(key);
        if (!node.isType(type))
            throw new YAMLTypeMismatchException(
                    this.getFileName(),
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET),
                    type,
                    YAMLNodeType.fromTag(node.getThisNode().getTag())
            );
    }

    @Override
    public int size()
    {
        if (this.isType(YAMLNodeType.LIST))
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
    public List<StructuredYamlNode> keys() throws YAMLTypeMismatchException
    {
        if (!this.isType(YAMLNodeType.MAPPING))
            throw this.createTypeMismatchException(YAMLNodeType.MAPPING);

        MappingNode mappingNode = (MappingNode) this.thisNode;

        return mappingNode.getValue().stream()
                .map(NodeTuple::getKeyNode)
                .filter(node -> node instanceof ScalarNode)
                .map(node -> new StructuredYamlNodeImpl(this.root, node))
                .collect(Collectors.toList());
    }

    @Override
    public StructuredYamlNode getItem(int index)
            throws YamlParsingException
    {
        if (!this.isType(YAMLNodeType.LIST))
            throw this.createTypeMismatchException(YAMLNodeType.LIST);

        SequenceNode sequenceNode = (SequenceNode) this.thisNode;

        if (index < 0 || index >= sequenceNode.getValue().size())
            throw new YamlParsingException(
                    "Index out of range: " + index,
                    this.getFileName(),
                    String.format("Index: %d, Size: %d", index, sequenceNode.getValue().size()),
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET)
            );

        return new StructuredYamlNodeImpl(this.root, sequenceNode.getValue().get(index));
    }

    @Override
    public <T> T getAs(Object key, ValueMapper<T> mapper) throws YamlParsingException
    {
        StructuredYamlNode node = this.get(key);
        try
        {
            return mapper.map(node);
        }
        catch (Exception e)
        {
            throw new YamlValueParsingException(
                    "Failed to parse value: " + e.getMessage(),
                    this.getFileName(),
                    Objects.toString(key),
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET),
                    e
            );
        }
    }

    @Override
    public void validate(Object key, Validator validator, @Nullable String message) throws YamlParsingException
    {
        StructuredYamlNode node = this.get(key);
        try
        {
            validator.validate(node);
        }
        catch (Exception e)
        {
            if (message == null)
                message = "";

            throw new YamlValueParsingException(
                    "Validation failed: " + message + " (caused by " + e.getMessage() + ")",
                    this.getFileName(),
                    Objects.toString(key),
                    this.thisNode.getStartMark().getLine(),
                    this.getLines(this.thisNode.getStartMark().getLine(), DEFAULT_TOP_BOTTOM_OFFSET),
                    e
            );
        }
    }

    @Override
    public void validateIfContainsKey(Object key, Validator validator) throws YamlParsingException
    {
        if (this.containsKey(key))
            this.validate(key, validator, null);
    }

    @Override
    public void validateIfContainsKey(Object key, Validator validator, @Nullable String message) throws YamlParsingException
    {
        if (this.containsKey(key))
            this.validate(key, validator, message);
    }
}
