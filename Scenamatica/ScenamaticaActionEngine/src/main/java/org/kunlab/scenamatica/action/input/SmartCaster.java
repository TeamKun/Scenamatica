package org.kunlab.scenamatica.action.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.scenariofile.StructuredYamlNodeImpl;
import org.kunlab.scenamatica.structures.ObjectNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SmartCaster
{
    private static <T extends Number> T castNumber(Number num, Class<? extends T> clazz)
    {
        if (clazz == Integer.class || clazz == int.class)
            return clazz.cast(num.intValue());
        else if (clazz == Long.class || clazz == long.class)
            return clazz.cast(num.longValue());
        else if (clazz == Double.class || clazz == double.class)
            return clazz.cast(num.doubleValue());
        else if (clazz == Float.class || clazz == float.class)
            return clazz.cast(num.floatValue());
        else if (clazz == Short.class || clazz == short.class)
            return clazz.cast(num.shortValue());
        else if (clazz == Byte.class || clazz == byte.class)
            return clazz.cast(num.byteValue());
        else if (clazz == Number.class)
            return clazz.cast(num);
        else
            throw new IllegalArgumentException("Unknown number type: " + clazz);
    }

    private static <T extends Number> T smartCastNumber(Object obj, Class<T> clazz) throws YamlParsingException
    {
        if (obj instanceof Number)  // 自分自身が Number ならそのままキャスト
            return castNumber((Number) obj, clazz);
        else if (obj instanceof String) // 数を表す文字列なら BigDecimal に変換してキャスト
        {
            BigDecimal decimal = new BigDecimal((String) obj);
            return castNumber(decimal, clazz);
        }
        else if (obj instanceof StructuredYamlNode)  // YAML なら Number か判定してキャスト
        {
            StructuredYamlNode node = (StructuredYamlNode) obj;
            node.ensureTypeOf(YAMLNodeType.NUMBER);

            return castNumber(node.asNumber(), clazz);
        }
        else
            throw new IllegalArgumentException("Unknown number type: " + clazz);
    }

    private static Boolean smartCastBoolean(Object obj) throws YamlParsingException
    {
        if (obj instanceof Boolean)
            return (Boolean) obj;  // Boolean#parseBoolean は false になるので使わない
        else if (obj instanceof String)  // 文字列なら Boolean に変換してキャスト
        {
            boolean isTrue = Boolean.parseBoolean((String) obj);  // これは "false" かどうか, 不正文字列かを判定しない。
            if (isTrue || "false".equalsIgnoreCase((String) obj))
                return isTrue;
            else
                throw new IllegalArgumentException("Unable top parse boolean from string: " + obj);
        }
        else if (obj instanceof StructuredYamlNode)  // YAML なら Boolean か判定してキャスト
        {
            StructuredYamlNode node = (StructuredYamlNode) obj;
            node.ensureTypeOf(YAMLNodeType.BOOLEAN);

            return node.asBoolean();
        }
        else
            throw new IllegalArgumentException("Unknown boolean type: " + obj);
    }

    private static String smartCastString(Object obj) throws YamlParsingException
    {
        if (obj instanceof String)
            return (String) obj;
        else if (obj instanceof StructuredYamlNode)
            return ((StructuredYamlNode) obj).asString();
        else
            return String.valueOf(obj);
    }

    private static <T> T smartCast(Object fromObject, Class<? extends T> toClass) throws YamlParsingException
    {
        if (fromObject == null)
            return null;
        else if (toClass.isInstance(fromObject))
            return toClass.cast(fromObject);

        // StructuredYamlNode あたりの処理
        if ((fromObject instanceof ObjectNode && ((ObjectNode) fromObject).isType(toClass)))
            return toClass.cast(((ObjectNode) fromObject).getObject());
        else if (fromObject instanceof StructuredYamlNode && ((StructuredYamlNode) fromObject).getThisNode() instanceof ObjectNode)
        {
            ObjectNode node = (ObjectNode) ((StructuredYamlNode) fromObject).getThisNode();
            if (node.isType(toClass))
                return toClass.cast(node.getObject());
        }

        if (Number.class.isAssignableFrom(toClass))
            // noinspection unchecked
            return toClass.cast(smartCastNumber(fromObject, (Class<? extends Number>) toClass));
        else if (toClass == Boolean.class || toClass == boolean.class)
            return toClass.cast(smartCastBoolean(fromObject));
        else if (List.class.isAssignableFrom(toClass))
        {
            if (fromObject instanceof List)
                return toClass.cast(fromObject);
            else if (fromObject instanceof StructuredYamlNode)
            {
                StructuredYamlNode node = (StructuredYamlNode) fromObject;
                node.ensureTypeOf(YAMLNodeType.LIST);

                return toClass.cast(node.asList(StructuredYamlNode::asObject));
            }
            else
                throw new IllegalArgumentException("Unknown list type: " + fromObject);
        }
        else if (toClass == Map.class)
        {
            if (fromObject instanceof Map)
                return toClass.cast(fromObject);
            else if (fromObject instanceof StructuredYamlNode)
            {
                StructuredYamlNode node = (StructuredYamlNode) fromObject;
                node.ensureTypeOf(YAMLNodeType.MAPPING);

                return toClass.cast(node.asMap(StructuredYamlNode::asObject, StructuredYamlNode::asObject));
            }
            else
                throw new IllegalArgumentException("Unknown map type: " + fromObject);
        }
        else if (toClass == String.class)
            return toClass.cast(smartCastString(fromObject));
        else if (StructuredYamlNode.class.isAssignableFrom(toClass))
        {
            if (fromObject instanceof StructuredYamlNode)
                return toClass.cast(fromObject);
            else
                return toClass.cast(StructuredYamlNodeImpl.fromObject(fromObject));
        }


        throw new IllegalArgumentException("Unknown type: " + fromObject + ", expected: " + toClass);
    }

    private static boolean canSmartCast(Class<?> clazz, Object obj)
    {
        if (obj == null || clazz.isInstance(obj) || clazz == String.class
                // Requireable のためのパッチ
                || Requireable.class.isAssignableFrom(clazz) || ActionContext.class.isAssignableFrom(clazz))
            return true;

        return isMatchingType(Number.class, obj, YAMLNodeType.NUMBER)
                || isMatchingType(Boolean.class, obj, YAMLNodeType.BOOLEAN)
                || isMatchingType(List.class, obj, YAMLNodeType.LIST)
                || ((Map.class.isAssignableFrom(clazz) || StructuredYamlNode.class.isAssignableFrom(clazz)) &&
                (obj instanceof Map || obj instanceof StructuredYamlNode && ((StructuredYamlNode) obj).isType(YAMLNodeType.MAPPING)))
                || (Structure.class.isAssignableFrom(clazz) && obj instanceof StructuredYamlNode && ((StructuredYamlNode) obj).isType(YAMLNodeType.MAPPING));
    }

    private static boolean isMatchingType(Class<?> clazz, Object obj, YAMLNodeType nodeType)
    {
        return clazz.isAssignableFrom(obj.getClass())
                || (obj instanceof StructuredYamlNode && ((StructuredYamlNode) obj).isType(nodeType));
    }

    /* non-public */
    static <U> U smartCast(@NotNull InputToken<U> token, @NotNull StructureSerializer serializer, @Nullable Object resolved)
            throws InvalidScenarioFileException
    {
        if (resolved == null)
            return null;

        List<Traverser<?, U>> traversers = token.getTraversers();
        if (traversers.isEmpty())
            return smartCast(resolved, token.getClazz());
        else
        {
            for (Traverser<?, U> traverser : traversers)
            {
                Class<?> possibleType = traverser.getInputClazz();
                if (canSmartCast(possibleType, resolved))
                    return token.getClazz().cast(traverser.tryTraverse(serializer, smartCast(resolved, possibleType)));
                else if (possibleType.isInstance(resolved))
                    return token.getClazz().cast(traverser.tryTraverse(serializer, resolved));
            }

            if (token.getClazz().isInstance(resolved))
                return token.getClazz().cast(resolved);
            else
                throw new IllegalArgumentException("Incompatible type detected for " + token.getName() + ": " + resolved.getClass().getName());
        }
    }
}
