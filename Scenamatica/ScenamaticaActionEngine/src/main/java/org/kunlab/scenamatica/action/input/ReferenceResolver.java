package org.kunlab.scenamatica.action.input;

import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.structures.ObjectNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReferenceResolver
{
    public static final String REFERENCE_PATTERN_PF = "${%s}";
    public static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\{([\\w_\\-.]+)}");

    public static boolean containsReference(String str)
    {
        return REFERENCE_PATTERN.matcher(str).find();
    }

    public static boolean containsReference(Collection<?> collection) throws YamlParsingException
    {
        for (Object obj : collection)
            if (containsReference(obj))
                return true;

        return false;
    }

    public static boolean containsReference(Map<?, ?> map) throws YamlParsingException
    {
        for (Object obj : map.values())
            if (containsReference(obj))
                return true;

        return false;
    }

    public static boolean containsReference(Object obj) throws YamlParsingException
    {
        if (obj == null)
            return false;
        else if (obj instanceof Collection)
            return containsReference((Collection<?>) obj);
        else if (obj instanceof Map)
            return containsReference((Map<?, ?>) obj);
        else if (obj instanceof StructuredYamlNode)
            return containsReference((StructuredYamlNode) obj);
        else
            return containsReference(obj.toString());
    }

    private static boolean containsReference(StructuredYamlNode node) throws YamlParsingException
    {
        if (node.getThisNode() instanceof ObjectNode)
            return false;
        else if (node.isType(YAMLNodeType.STRING))
            return containsReference(node.asString());
        else if (node.isType(YAMLNodeType.LIST))
            return containsReference(node.asList(StructuredYamlNode::asObject));
        else if (node.isType(YAMLNodeType.MAPPING))
            return containsReference(node.asMap(StructuredYamlNode::asObject, StructuredYamlNode::asObject));
        else
            return false;
    }

    private static Object resolveReferences(String base, String[] references, SessionStorage variables)
    {
        boolean isOnlyReference = references.length == 1 && base.equals(String.format(REFERENCE_PATTERN_PF, references[0]));

        boolean containsNull = false;
        for (String reference : references)
        {
            Object obj = variables.get(reference);
            if (isOnlyReference)
                return obj;

            containsNull |= obj == null;

            String value = obj == null ? "": obj.toString();
            base = base.replace(String.format(REFERENCE_PATTERN_PF, reference), value);
        }


        // 文字列が空・null が含まれている => すべて null
        return base.isEmpty() && containsNull ? null: base;
    }

    private static Collection<?> resolveReferences(Collection<?> base, String[] references, SessionStorage variables)
    {
        List<Object> resolved = new ArrayList<>();
        for (Object obj : base)
        {
            Object resolvedObj;
            if (obj instanceof Collection)
                resolvedObj = resolveReferences((Collection<?>) obj, references, variables);
            else if (obj instanceof Map)
                resolvedObj = resolveReferences((Map<?, ?>) obj, references, variables);
            else if (obj == null)
                resolvedObj = null;
            else
                resolvedObj = resolveReferences(obj.toString(), references, variables);

            resolved.add(resolvedObj);
        }

        return resolved;
    }

    private static Map<?, ?> resolveReferences(Map<?, ?> base, String[] references, SessionStorage variables)
    {
        Map<Object, Object> resolved = new HashMap<>();
        for (Map.Entry<?, ?> entry : base.entrySet())
        {
            Object value = entry.getValue();
            // キーはスコープ外

            Object resolvedValue;
            if (entry.getValue() instanceof Collection)
                resolvedValue = resolveReferences((Collection<?>) value, references, variables);
            else if (entry.getValue() instanceof Map)
                resolvedValue = resolveReferences((Map<?, ?>) value, references, variables);
            else if (entry.getValue() == null)
                resolvedValue = null;
            else
                resolvedValue = resolveReferences(value.toString(), references, variables);

            resolved.put(entry.getKey(), resolvedValue);
        }

        return resolved;
    }

    /* non-public */
    static Object resolveReferences(Object base, String[] references, SessionStorage variables) throws YamlParsingException
    {
        if (base instanceof Collection)
            return resolveReferences((Collection<?>) base, references, variables);
        else if (base instanceof Map)
            return resolveReferences((Map<?, ?>) base, references, variables);
        else if (base == null)
            return null;
        else if (base instanceof StructuredYamlNode)
            return resolveReferences((StructuredYamlNode) base, references, variables);
        else
            return resolveReferences(base.toString(), references, variables);
    }

    private static Object resolveReferences(StructuredYamlNode node, String[] references, SessionStorage variables) throws YamlParsingException
    {
        return resolveReferences(node.asObject(), references, variables);
    }

    static String[] selectReferences(Object referencing) throws YamlParsingException
    {
        // awdawd${hoge} -> [hoge]
        // awdawd${hoge}${fuga} -> [hoge, fuga]
        // awdawd${hoge}${fuga}${piyo} -> [hoge, fuga, piyo]

        // { awd: "${hoge}", fuga: "${fuga}" } -> [hoge, fuga]
        // [ "${hoge}", "${fuga}" ] -> [hoge, fuga]

        if (referencing instanceof StructuredYamlNode)
            return selectReferences((StructuredYamlNode) referencing).toArray(new String[0]);
        if (referencing instanceof String)
            return selectReferences((String) referencing).toArray(new String[0]);
        else if (referencing instanceof Iterable)
        {
            Iterable<?> iterable = (Iterable<?>) referencing;
            Set<String> references = new HashSet<>();
            for (Object obj : iterable)
            {
                if (obj != null)
                    references.addAll(selectReferences(obj.toString()));
            }
            return references.toArray(new String[0]);
        }
        else if (referencing instanceof Map)
        {
            Set<String> references = new HashSet<>();
            for (Object obj : ((Map<?, ?>) referencing).values())
            {
                if (obj != null)
                    references.addAll(selectReferences(obj.toString()));
            }

            return references.toArray(new String[0]);
        }
        else
            throw new IllegalArgumentException("Unsupported reference type: " + referencing.getClass().getName());

    }

    private static List<String> selectReferences(StructuredYamlNode node) throws YamlParsingException
    {
        if (node.isType(YAMLNodeType.STRING))
            return selectReferences(node.asString());
        else if (node.isType(YAMLNodeType.LIST))
        {
            List<String> references = new ArrayList<>();
            for (String child : node.asList(StructuredYamlNode::asString))
                references.addAll(selectReferences(child));

            return references;
        }
        else if (node.isType(YAMLNodeType.MAPPING))
        {
            List<String> references = new ArrayList<>();
            for (StructuredYamlNode child : node.asNodeMap().values())
                references.addAll(selectReferences(child));

            return references;
        }
        else
            throw new IllegalArgumentException("Unsupported reference type: " + node);
    }

    private static List<String> selectReferences(String referencedString)
    {
        Matcher matcher = REFERENCE_PATTERN.matcher(referencedString);
        List<String> references = new ArrayList<>();

        while (matcher.find())
            references.add(matcher.group(1));

        return references;
    }
}
