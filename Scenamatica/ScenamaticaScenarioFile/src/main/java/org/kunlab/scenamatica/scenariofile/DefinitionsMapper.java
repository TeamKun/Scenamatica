package org.kunlab.scenamatica.scenariofile;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefinitionsMapper
{
    public static final String KEY_DEFINITIONS = "definitions";
    public static final String KEY_REFERENCE = "$ref";
    private static final Pattern PATTERN_REFERENCE_EMBED = Pattern.compile("\\$\\{([\\w|{}]+)}");

    public static void resolveReferences(StructuredYamlNode node) throws InvalidScenarioFileException
    {
        if (!node.containsKey(KEY_DEFINITIONS))
            return;

        StructuredYamlNode definitions = node.get(KEY_DEFINITIONS);

        processDefinitions(node, definitions);
    }

    public static StructuredYamlNode resolveReferences(StructuredYamlNode target, StructuredYamlNode schema) throws InvalidScenarioFileException
    {
        return processDefinitions(target, schema);
    }

    private static StructuredYamlNode processDefinitions(StructuredYamlNode targetNode, StructuredYamlNode schema) throws InvalidScenarioFileException
    {
        if (targetNode.isType(YAMLNodeType.MAPPING))
            return processDefsSetOfMapping(targetNode, schema);  // なかで再帰
        else if (targetNode.isType(YAMLNodeType.LIST))
            return processDefsSetOfList(targetNode, schema);  // なかで再帰

        if (!targetNode.isType(YAMLNodeType.STRING))
            return targetNode;

        return processEmbeddedRef(targetNode, schema);   // ${ref} とかの処理
    }

    private static StructuredYamlNode processDefsSetOfMapping(StructuredYamlNode mappingNode, StructuredYamlNode definitions)
            throws InvalidScenarioFileException
    {
        // Mapping に $reference: がある場合は, それを解決する。
        if (mappingNode.containsKey(KEY_REFERENCE))
        {
            String referenceString = mappingNode.get(KEY_REFERENCE).asString();
            mappingNode.remove(KEY_REFERENCE);
            StructuredYamlNode resolvedValue = definitions.get(referenceString);

            if (resolvedValue.isType(YAMLNodeType.MAPPING))
                mappingNode.mergeMapping(resolvedValue);  // スキーム的に, 型がStringであることは保証されている。はず。
            else
                return resolvedValue;  // スキーマ取得用に Object にしたが, 実際は String 等の場合。
        }

        for (Pair<? extends StructuredYamlNode, ? extends StructuredYamlNode> entry : mappingNode.getMappingEntries())
        {
            StructuredYamlNode key = entry.getLeft();
            StructuredYamlNode value = entry.getRight();

            mappingNode.remove(key);
            mappingNode.add(key, processDefinitions(value, definitions));
        }

        return mappingNode;
    }

    private static StructuredYamlNode processDefsSetOfList(StructuredYamlNode listNode, StructuredYamlNode definitions) throws InvalidScenarioFileException
    {
        for (StructuredYamlNode o : listNode.asList())
        {
            listNode.removeSequenceItem(o);
            listNode.addSequenceItem(processDefinitions(o, definitions));
        }

        return listNode;
    }

    private static StructuredYamlNode processEmbeddedRef(StructuredYamlNode scalarValue, StructuredYamlNode defintions) throws YamlParsingException
    {
        String stringValue = scalarValue.asString();
        Matcher matcher = PATTERN_REFERENCE_EMBED.matcher(stringValue);

        // SnakeYAML が勝手に型推論してしまうので, 文字列として参照を置換した後に, キャストし直す必要があるため。
        boolean isIntOrLong = true;
        boolean isDouble = true;
        boolean isBoolean = true;
        boolean replaced = false;
        for (int i = 0; matcher.find(i); i = matcher.end(), replaced = true)
        {
            String ref = matcher.group(1);
            String refFull = matcher.group(0);
            StructuredYamlNode resolvedValue = defintions.get(ref);
            if (resolvedValue == null)
            {
                if (ref.startsWith("{"))
                    stringValue = stringValue.replace("${{}", "{");
                else if (ref.endsWith("}"))
                    stringValue = stringValue.replace("}", "");
                else
                    throw new IllegalArgumentException("Definitions reference not found: " + ref);

                continue;
            }

            if (!resolvedValue.isType(YAMLNodeType.INTEGER))
                isIntOrLong = false;
            if (!resolvedValue.isType(YAMLNodeType.FLOAT))
                isDouble = false;
            if (!resolvedValue.isType(YAMLNodeType.BOOLEAN))
                isBoolean = false;

            stringValue = stringValue.replace(refFull, resolvedValue.toString());
        }

        // 置換されていない場合は、深い置換/型の復元（再変換）は必要ない。
        if (!replaced)
            return scalarValue;

        if (isIntOrLong)
            return scalarValue.changeScalarValue(YAMLNodeType.INTEGER, Long.parseLong(stringValue));
        if (isDouble)
            return scalarValue.changeScalarValue(YAMLNodeType.FLOAT, Double.parseDouble(stringValue));
        if (isBoolean)
            return scalarValue.changeScalarValue(YAMLNodeType.BOOLEAN, Boolean.parseBoolean(stringValue));

        // 全部の方を網羅しているので, 順に脱落していく。 ここに来るのは String だけ。
        // assert value instanceof String;

        return processEmbeddedRef(scalarValue.changeScalarValue(YAMLNodeType.STRING, stringValue), defintions);  // Chu!❤ 再帰関数でごめん
    }
}
