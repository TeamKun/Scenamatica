package org.kunlab.scenamatica.action.selector.compiler;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.action.selector.Selector;
import org.kunlab.scenamatica.action.selector.compiler.lexer.SelectorLexer;
import org.kunlab.scenamatica.action.selector.compiler.lexer.SelectorToken;
import org.kunlab.scenamatica.action.selector.compiler.parser.PropertiedSelector;
import org.kunlab.scenamatica.action.selector.compiler.parser.SelectorSyntaxAnalyzer;
import org.kunlab.scenamatica.action.selector.compiler.parser.SyntaxTree;
import org.kunlab.scenamatica.action.selector.compiler.parser.SyntaxTreeTraverser;
import org.kunlab.scenamatica.action.selector.predicates.LocationPredicate;
import org.kunlab.scenamatica.action.selector.predicates.SelectorPredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SelectorCompiler
{
    private static final SelectorCompiler INSTANCE;

    static
    {
        INSTANCE = new SelectorCompiler();
    }

    private final List<SelectorPredicate<? super Entity>> predicates;

    private SelectorCompiler()
    {
        this.predicates = Collections.unmodifiableList(getAllPredicates());
    }

    private static List<SelectorPredicate<? super Entity>> getAllPredicates()
    {
        ArrayList<SelectorPredicate<? super Entity>> predicates = new ArrayList<>();
        // <editor-fold desc="Predicates registering">
        predicates.add(new LocationPredicate());
        // </editor-fold>
        return predicates;
    }

    private static List<SelectorPredicate<? super Entity>> getPredicatesFor(String key, Map<String, Object> properties)
    {
        return INSTANCE.predicates.stream()
                .filter(predicate -> predicate.isApplicableKey(properties))
                .collect(Collectors.toList());
    }

    private static List<SelectorPredicate<? super Entity>> getPredicatesFor(Map<String, Object> properties)
    {
        ArrayList<SelectorPredicate<? super Entity>> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet())
            predicates.addAll(getPredicatesFor(entry.getKey(), properties));
        return predicates;
    }

    private static Predicate<? super Entity> combinePredicates(
            List<? extends SelectorPredicate<? super Entity>> predicates,
            Map<? super String, Object> properties)
    {
        // 複数の Predicate を結合
        return (entity) -> {
            for (SelectorPredicate<? super Entity> predicate : predicates)
                if (predicate.getApplicableClass().isInstance(entity) &&
                        !predicate.test(entity, properties))
                    return false;
            return true;
        };
    }

    public static Selector compile(String selector)
    {
        // コンパイル
        LinkedList<SelectorToken> tokens = SelectorLexer.tokenize(selector);
        SyntaxTree tree = SelectorSyntaxAnalyzer.analyze(tokens);
        PropertiedSelector elements = SyntaxTreeTraverser.traverse(tree);

        // Predicate 生成
        List<SelectorPredicate<? super Entity>> predicates = getPredicatesFor(elements.getProperties());
        Map<String, Object> properties = elements.getProperties();
        for (SelectorPredicate<? super Entity> predicate : predicates)
            predicate.normalizeMap(properties);

        Predicate<? super Entity> predicate = combinePredicates(predicates, elements.getProperties());

        return new Selector(selector, elements.getType(), predicate);
    }
}
