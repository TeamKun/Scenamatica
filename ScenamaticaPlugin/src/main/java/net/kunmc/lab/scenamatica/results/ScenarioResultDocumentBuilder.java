package net.kunmc.lab.scenamatica.results;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.Constants;
import net.kunmc.lab.scenamatica.enums.ScenarioResultCause;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScenarioResultDocumentBuilder
{

    public static Document build(@NotNull ScenamaticaRegistry registry, @NotNull ScenarioSession session)
    {
        Document document = createBase();

        buildScenamatica(document, registry, session);
        buildTestSuites(document, registry, session);

        return document;
    }

    private static void buildScenamatica(@NotNull Document document, @NotNull ScenamaticaRegistry registry, @NotNull ScenarioSession session)
    {
        Element scenamatica = document.createElementNS(ResultKeys.SCENAMATICA_NAMESPACE, ResultKeys.KEY_SCENAMATICA);
        scenamatica.setAttribute("xmlns:" + ResultKeys.SCENAMATICA_NAMESPACE_ID, ResultKeys.SCENAMATICA_NAMESPACE);

        buildSoftwareInfo(document, registry, session);
        buildPluginsInfo(document, registry, session);
    }

    private static void buildTestSuites(@NotNull Document document, @NotNull ScenamaticaRegistry registry, @NotNull ScenarioSession session)
    {
        Element testSuites = document.createElement(ResultKeys.KEY_TEST_SUITES);
        testSuites.setAttribute(ResultKeys.KEY_SUITES_TIME, String.valueOf(
                unixToISO8601(session.getStartedAt(), session.getFinishedAt()))
        );

        Multimap<Plugin, ScenarioResult> results = groupingResultsByPlugin(session);

        for (Plugin plugin : results.keySet())
            testSuites.appendChild(buildSuite(document, plugin, new ArrayList<>(results.get(plugin))));
    }

    private static Element buildSuite(@NotNull Document document, @NotNull Plugin plugin, @NotNull List<? extends ScenarioResult> results)
    {
        Element testSuite = document.createElement(ResultKeys.KEY_TEST_SUITE);
        testSuite.setAttribute(ResultKeys.KEY_SUITE_ID, toPluginID(plugin));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_NAME, plugin.getName() + "-" + plugin.getDescription().getVersion());
        testSuite.setAttribute(ResultKeys.KEY_SUITE_TIME, String.valueOf(
                unixToISO8601(summingResultsTime(results))
        ));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_TESTS, String.valueOf(results.size()));

        // testSuite の attribute にあとで追加するために一時的に保持する
        int failures = 0;
        int skipped = 0;
        int errors = 0;

        for (ScenarioResult result : results)
        {
            ScenarioResultCause cause = result.getScenarioResultCause();
            if (cause.isFailure())
                failures++;
            else if (cause.isSkipped() || cause.isCancelled())
                skipped++;
            else if (cause.isError())
                errors++;

            testSuite.appendChild(buildTestCase(document, result));
        }

        testSuite.setAttribute(ResultKeys.KEY_SUITE_FAILURES, String.valueOf(failures));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_SKIPPED, String.valueOf(skipped));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_ERRORS, String.valueOf(errors));

        return testSuite;
    }

    private static Element buildTestCase(@NotNull Document document, @NotNull ScenarioResult result)
    {
        Element testCase = document.createElement(ResultKeys.KEY_TEST_CASE);
        testCase.setAttribute(ResultKeys.KEY_CASE_NAME, result.getScenario().getName());
        testCase.setAttribute(ResultKeys.KEY_CASE_TIME, String.valueOf(
                unixToISO8601(result.getStartedAt(), result.getFinishedAt()))
        );
        testCase.setAttribute(ResultKeys.KEY_CASE_STATUS, result.getState().name());

        if (result.getScenarioResultCause().isFailure())
            testCase.appendChild(buildCauseFailure(document, result));
        else if (result.getScenarioResultCause().isSkipped() || result.getScenarioResultCause().isCancelled())
            testCase.appendChild(document.createElement(ResultKeys.KEY_CASE_SKIPPED));
        else if (result.getScenarioResultCause().isError())
            testCase.appendChild(document.createElement(ResultKeys.KEY_CASE_ERROR));

        return testCase;
    }

    private static Element buildCauseError(@NotNull Document document, @NotNull ScenarioResult result)
    {
        Element error = document.createElement(ResultKeys.KEY_CASE_ERROR);
        error.setAttribute(ResultKeys.KEY_CASE_ERROR_TYPE, result.getScenarioResultCause().name());

        Action<?> failedAction = result.getFailedAction();
        if (failedAction != null)
            error.setAttribute(ResultKeys.KEY_CASE_ERROR_MESSAGE, "Failed to pass scenario: " + failedAction.getName());

        return error;
    }

    private static Element buildCauseFailure(@NotNull Document document, @NotNull ScenarioResult result)
    {
        Element failure = document.createElement(ResultKeys.KEY_CASE_FAILURE);
        failure.setAttribute(ResultKeys.KEY_CASE_FAILURE_TYPE, result.getScenarioResultCause().name());

        Action<?> failedAction = result.getFailedAction();
        if (failedAction != null)
            failure.setAttribute(ResultKeys.KEY_CASE_FAILURE_MESSAGE, failedAction.getName());

        return failure;
    }

    private static void buildPluginsInfo(@NotNull Document document, @NotNull ScenamaticaRegistry registry, @NotNull ScenarioSession session)
    {
        Element pluginsElement = document.createElement(ResultKeys.KEY_PLUGINS);
        List<Plugin> plugins = session.getScenarios().stream().parallel()
                .map(scenario -> scenario.getEngine().getPlugin())
                .distinct()
                .collect(Collectors.toList());

        for (Plugin plugin : plugins)
        {
            Element pluginElement = document.createElement(ResultKeys.KEY_PLUGIN);
            pluginElement.setAttribute(ResultKeys.KEY_ID, toPluginID(plugin));

            PluginDescriptionFile description = plugin.getDescription();

            pluginElement.appendChild(document.createElement(ResultKeys.KEY_PLUGIN_NAME))
                    .setTextContent(description.getName());
            pluginElement.appendChild(document.createElement(ResultKeys.KEY_PLUGIN_VERSION))
                    .setTextContent(description.getVersion());
            pluginElement.appendChild(document.createElement(ResultKeys.KEY_PLUGIN_DESCRIPTION))
                    .setTextContent(description.getDescription());
            pluginElement.appendChild(document.createElement(ResultKeys.KEY_PLUGIN_URL))
                    .setTextContent(description.getWebsite());
            pluginElement.appendChild(document.createElement(ResultKeys.KEY_PLUGIN_WEBSITE))
                    .setTextContent(description.getWebsite());

            Element authorsElement = document.createElement(ResultKeys.KEY_PLUGIN_AUTHORS);
            for (String author : description.getAuthors())
                authorsElement.appendChild(document.createElement(ResultKeys.KEY_PLUGIN_AUTHOR))
                        .setTextContent(author);

            pluginElement.appendChild(authorsElement);

            pluginsElement.appendChild(pluginElement);
        }

        document.appendChild(pluginsElement);
    }

    private static void buildSoftwareInfo(@NotNull Document document, @NotNull ScenamaticaRegistry registry, @NotNull ScenarioSession session)
    {
        document.appendChild(document.createElement(ResultKeys.KEY_SCENAMATICA_VERSION))
                .setTextContent(registry.getPlugin().getDescription().getVersion());

        document.appendChild(document.createElement(ResultKeys.KEY_SCENAMATICA_BUILD))
                .setTextContent(Constants.DEBUG_BUILD ? "Debug": "Release");
    }

    private static Document createBase()
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document document = factory.newDocumentBuilder().newDocument();
            document.setXmlStandalone(true);

            return document;
        }
        catch (ParserConfigurationException e)
        {
            throw new IllegalStateException(e); // 多分起こらない。
        }
    }

    private static String toPluginID(Plugin plugin)
    {
        Pattern pattern = Pattern.compile("[^a-z0-9-]");
        String pluginName = plugin.getName()
                .replace(" ", "-");

        return pattern.matcher(pluginName).replaceAll("");
    }

    private static Multimap<Plugin, ScenarioResult> groupingResultsByPlugin(@NotNull ScenarioSession session)
    {
        return session.getScenarios().stream().parallel()
                .map(scenario -> Pair.of(scenario.getEngine().getPlugin(), scenario.getResult()))
                .reduce(ArrayListMultimap.create(), (map, pair) -> {
                    map.put(pair.getLeft(), pair.getRight());
                    return map;
                }, (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                });
    }

    private static long summingResultsTime(@NotNull List<? extends ScenarioResult> results)
    {
        return results.stream().parallel()
                .mapToLong(result -> result.getFinishedAt() - result.getStartedAt())
                .sum();
    }

    private static double unixToISO8601(long unix)
    {
        return unix / 1000.0d;
    }

    private static double unixToISO8601(long startUnix, long endUnix)
    {
        return unixToISO8601(endUnix - startUnix);
    }
}
