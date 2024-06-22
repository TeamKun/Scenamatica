package org.kunlab.scenamatica.bookkeeper.compiler;

import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.BookkeeperConfig;
import org.kunlab.scenamatica.bookkeeper.BookkeeperCore;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.IReference;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionCategoryDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.ActionDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.OutputsDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.TypeDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Timekeeper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Slf4j(topic = "Compiler")
public class Compiler
{
    private final BookkeeperCore core;
    private final List<ICompiler<?, ?, ?>> compilers;
    private final CategoryManager categoryManager;

    public Compiler(BookkeeperCore core)
    {
        this.core = core;
        this.compilers = new ArrayList<>();
        this.categoryManager = core.getCategoryManager();

        BookkeeperConfig config = core.getConfig();
        this.addCompilers(core, config);
    }

    private void addCompilers(BookkeeperCore core, BookkeeperConfig config)
    {
        TypeCompiler type = new TypeCompiler(core);
        this.compilers.add(type);

        EventCompiler event = null;
        if (this.core.getConfig().isResolveEvents())
            this.compilers.add(event = new EventCompiler(
                    config.getOutputDir(),
                    core.getTempDir(),
                    config.getLanguage(),
                    config.getEventsURL(),
                    config.getEventsLicenseURL()
            ));

        ActionCompiler action = new ActionCompiler(
                type,
                event,
                this.categoryManager
        );
        this.compilers.add(action);
    }

    public void init()
    {
        log.info("Initializing compilers...");
        this.compilers.forEach(ICompiler::init);
    }

    public List<IReference<?>> compile(List<? extends IDefinition> targets)
    {
        log.info("Compiling definitions...");
        Timekeeper timekeeper = new Timekeeper(log, "COMPILE");
        timekeeper.start();

        List<IDefinition> sortedList = calcOrder(targets);

        List<IReference<?>> compiled = new ArrayList<>();
        for (ICompiler<?, ?, ?> compiler : this.compilers)
        {
            Timekeeper tCompile = new Timekeeper(log, "COMPILE-" + compiler.getClass().getSimpleName());
            tCompile.start();
            this.categoryManager.changePhase(compiler.getName());

            Class<? extends IDefinition> definitionType = compiler.getDefinitionType();
            for (IDefinition definition : sortedList)
            {
                if (definitionType.isInstance(definition))
                {
                    // noinspection unchecked
                    IReference<?> reference = compile((ICompiler<IDefinition, ?, ?>) compiler, definition);
                    if (reference != null)
                        compiled.add(reference);
                }
            }

            tCompile.end();
        }

        this.categoryManager.flush();
        timekeeper.end();

        return compiled;
    }

    public void flush()
    {
        Path baseDir = this.core.getConfig().getOutputDir();

        log.info("Finishing compilation...");
        Timekeeper timekeeper = new Timekeeper(log, "FLUSH");
        timekeeper.start();
        for (ICompiler<?, ?, ?> compiler : this.compilers)
            compiler.flush(baseDir);

        log.info("Compilation finished.");
        timekeeper.end();
    }

    private <T extends IDefinition> IReference<?> compile(ICompiler<T, ?, ?> compiler, T definition)
    {
        return compiler.compile(definition);
    }

    private List<IDefinition> calcOrder(List<? extends IDefinition> targets)
    {
        log.info("Calculating compilation order...");
        Timekeeper timekeeper = new Timekeeper(log, "CORDER");
        timekeeper.start();
        List<IDefinition> sortedList = sort(targets);

        sortedList = reorderByType(sortedList);

        timekeeper.end();
        return sortedList;
    }

    private Map<IDefinition, List<IDefinition>> buildDependencyGraph(List<? extends IDefinition> targets)
    {
        log.info("Building dependency graph...");
        Timekeeper timekeeper = new Timekeeper(log, "DEPGRAPH");
        timekeeper.start();

        Map<IDefinition, List<IDefinition>> dependencyGraph = new HashMap<>();
        for (IDefinition target : targets)
        {
            for (IDefinition other : targets)
            {
                if (target.equals(other))
                    continue;

                if (target.isDependsOn(other))
                    dependencyGraph.computeIfAbsent(target, k -> new ArrayList<>())
                            .add(other);
            }

            dependencyGraph.putIfAbsent(target, new ArrayList<>());
        }

        timekeeper.end();
        return dependencyGraph;
    }

    public List<IDefinition> sort(List<? extends IDefinition> targets)
    {
        Map<IDefinition, List<IDefinition>> dependencyGraph = buildDependencyGraph(targets);
        Map<IDefinition, Integer> inDegree = new HashMap<>();
        Queue<IDefinition> queue = new LinkedList<>();
        List<IDefinition> sortedList = new ArrayList<>();

        for (Map.Entry<IDefinition, List<IDefinition>> entry : dependencyGraph.entrySet())
        {
            for (IDefinition neighbor : entry.getValue())
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
            inDegree.putIfAbsent(entry.getKey(), 0);
        }

        for (Map.Entry<IDefinition, Integer> entry : inDegree.entrySet())
            if (entry.getValue() == 0)
                queue.add(entry.getKey());

        while (!queue.isEmpty())
        {
            IDefinition current = queue.poll();
            sortedList.add(current);

            for (IDefinition neighbor : dependencyGraph.get(current))
            {
                int degree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, degree);

                if (degree == 0)
                    queue.add(neighbor);
            }
        }

        if (sortedList.size() != dependencyGraph.size())
            throw new RuntimeException("Graph contains a cycle!");

        Collections.reverse(sortedList);

        return sortedList;
    }

    private static List<IDefinition> reorderByType(List<? extends IDefinition> sortedList)
    {
        List<IDefinition> reordered = new ArrayList<>();
        Map<Class<? extends IDefinition>, List<IDefinition>> classified = classify(sortedList);

        // TypeDefinition: 0
        // OutputDefinition: 1
        // OutputsDefinition: 2
        // ActionCategoryDefinition: 3
        // ActionDefinition: 4
        for (int i = 0; i < 5; i++)
        {
            Class<? extends IDefinition> type;
            switch (i)
            {
                case 0:
                    type = TypeDefinition.class;
                    break;
                case 1:
                    type = OutputDefinition.class;
                    break;
                case 2:
                    type = OutputsDefinition.class;
                    break;
                case 3:
                    type = ActionCategoryDefinition.class;
                    break;
                case 4:
                    type = ActionDefinition.class;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + i);
            }

            List<IDefinition> definitions = classified.get(type);
            if (definitions == null)
                continue;

            reordered.addAll(definitions);
        }

        return reordered;
    }

    private static Map<Class<? extends IDefinition>, List<IDefinition>> classify(List<? extends IDefinition> targets)
    {
        Map<Class<? extends IDefinition>, List<IDefinition>> classified = new HashMap<>();
        for (IDefinition target : targets)
            classified.computeIfAbsent(
                    target.getClass(),
                    k -> new ArrayList<>()
            ).add(target);

        return classified;
    }
}
