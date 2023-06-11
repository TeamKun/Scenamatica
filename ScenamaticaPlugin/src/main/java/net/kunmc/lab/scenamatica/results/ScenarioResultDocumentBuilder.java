package net.kunmc.lab.scenamatica.results;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.kunmc.lab.scenamatica.enums.ScenarioResultCause;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.scenario.QueuedScenario;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ScenarioResultDocumentBuilder
{

    public static Document build(@NotNull ScenarioSession session)
    {
        Document document = createBase();
        buildTestSuites(document, session);

        return document;
    }

    private static void buildTestSuites(@NotNull Document document, @NotNull ScenarioSession session)
    {
        Element testSuites = document.createElement(ResultKeys.KEY_TEST_SUITES);
        testSuites.setAttribute(ResultKeys.KEY_SUITES_TIME, String.valueOf(
                unixToISO8601(session.getStartedAt(), session.getFinishedAt()))
        );

        Multimap<Plugin, ScenarioResult> results = groupingResultsByPlugin(session);

        for (Plugin plugin : results.keySet())
            testSuites.appendChild(buildSuite(document, plugin, new ArrayList<>(results.get(plugin))));

        document.appendChild(testSuites);
    }

    private static Element buildSuite(@NotNull Document document, @NotNull Plugin plugin, @NotNull List<? extends ScenarioResult> results)
    {

        Element testSuite = document.createElement(ResultKeys.KEY_TEST_SUITE);
        buildPluginInfo(testSuite, plugin);
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

    private static void buildPluginInfo(@NotNull Element parent, @NotNull Plugin plugin)
    {
        Document document = parent.getOwnerDocument();
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

        parent.appendChild(pluginElement);
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
                .replace(" ", "-")
                .toLowerCase(Locale.ENGLISH);

        return pattern.matcher(pluginName).replaceAll("");
    }

    private static Multimap<Plugin, ScenarioResult> groupingResultsByPlugin(@NotNull ScenarioSession session)
    {
        Multimap<Plugin, ScenarioResult> results = HashMultimap.create();

        for (QueuedScenario scenario : session.getScenarios())
        {
            ScenarioResult result = scenario.getResult();
            if (result != null)
                results.put(scenario.getEngine().getPlugin(), result);
        }

        return results;
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
