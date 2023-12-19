package org.kunlab.scenamatica.action.selector.compiler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.action.selector.Selector;
import org.kunlab.scenamatica.action.selector.compiler.lexer.SelectorLexer;
import org.kunlab.scenamatica.action.selector.compiler.lexer.SelectorToken;
import org.kunlab.scenamatica.action.selector.compiler.parser.PropertiedSelector;
import org.kunlab.scenamatica.action.selector.compiler.parser.SelectorSyntaxAnalyzer;
import org.kunlab.scenamatica.action.selector.compiler.parser.SyntaxTree;
import org.kunlab.scenamatica.action.selector.compiler.parser.SyntaxTreeTraverser;
import org.kunlab.scenamatica.action.selector.predicates.DistancePredicate;
import org.kunlab.scenamatica.action.selector.predicates.GameModePredicate;
import org.kunlab.scenamatica.action.selector.predicates.LevelPredicate;
import org.kunlab.scenamatica.action.selector.predicates.LocationPredicate;
import org.kunlab.scenamatica.action.selector.predicates.NamePredicate;
import org.kunlab.scenamatica.action.selector.predicates.ScorePredicate;
import org.kunlab.scenamatica.action.selector.predicates.SelectorPredicate;
import org.kunlab.scenamatica.action.selector.predicates.TagPredicate;
import org.kunlab.scenamatica.action.selector.predicates.TeamPredicate;
import org.kunlab.scenamatica.action.selector.predicates.TypePredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class SelectorCompiler
{
    private static final SelectorCompiler INSTANCE;

    static
    {
        INSTANCE = new SelectorCompiler();
    }

    private final List<SelectorPredicate<? extends Entity>> predicates;

    private SelectorCompiler()
    {
        this.predicates = Collections.unmodifiableList(getAllPredicates());
    }

    private static List<SelectorPredicate<? extends Entity>> getAllPredicates()
    {
        ArrayList<SelectorPredicate<? extends Entity>> predicates = new ArrayList<>();
        // <editor-fold desc="Predicates registering">
        predicates.add(new DistancePredicate());
        predicates.add(new GameModePredicate());
        predicates.add(new LevelPredicate());
        predicates.add(new LocationPredicate());
        predicates.add(new NamePredicate());
        predicates.add(new ScorePredicate());
        predicates.add(new TagPredicate());
        predicates.add(new TeamPredicate());
        predicates.add(new TypePredicate());
        // </editor-fold>
        return predicates;
    }

    private static List<SelectorPredicate<? extends Entity>> getPredicatesFor(Map<String, Object> properties)
    {
        return INSTANCE.predicates.stream()
                .filter(predicate -> predicate.isApplicableKey(properties))
                .collect(Collectors.toList());
    }

    private static BiPredicate<? super Player, ? extends Entity> combinePredicates(
            List<? extends SelectorPredicate<? extends Entity>> predicates,
            Map<? super String, Object> properties)
    {
        // 複数の Predicate を結合
        return (player, entity) -> {
            for (SelectorPredicate<? extends Entity> predicate : predicates)
            {
                if (!predicate.getApplicableClass().isInstance(entity))
                    continue;
                // noinspection rawtypes,unchecked
                if (!((SelectorPredicate) predicate).test(player, entity, properties))
                    return false;
            }
            return true;
        };
    }

    public static Selector compile(String selector, boolean canProvideBasis)
    {
        // コンパイル
        LinkedList<SelectorToken> tokens = SelectorLexer.tokenize(selector);
        SyntaxTree tree = SelectorSyntaxAnalyzer.analyze(tokens);
        PropertiedSelector elements = SyntaxTreeTraverser.traverse(tree);

        // Predicate 生成
        List<SelectorPredicate<? extends Entity>> predicates = getPredicatesFor(elements.getProperties());
        Map<String, Object> properties = elements.getProperties();
        for (SelectorPredicate<? extends Entity> predicate : predicates)
            predicate.normalizeMap(properties);

        if (!canProvideBasis)
            ensureNoBasisRequired(predicates);

        BiPredicate<? super Player, ? extends Entity> predicate = combinePredicates(predicates, elements.getProperties());

        return new Selector(selector, elements.getType(), predicate);
    }

    private static void ensureNoBasisRequired(List<? extends SelectorPredicate<? extends Entity>> predicates)
    {
        predicates.stream()
                .filter(SelectorPredicate::isBasisRequired)
                .map(pred -> pred.getClass().getSimpleName())
                .map(name -> name.replace("Predicate", ""))
                .map(String::toLowerCase)
                .reduce((a, b) -> a + ", " + b)
                .ifPresent(names -> {
                    throw new IllegalArgumentException("This selector(s) requires basis: " + names);
                });
    }
}
