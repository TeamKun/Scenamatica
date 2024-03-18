package org.kunlab.scenamatica.results;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ScenarioResultDocumentBuilder
{
    private final ScenarioResultWriter writer;

    public ScenarioResultDocumentBuilder(ScenarioResultWriter writer)
    {
        this.writer = writer;
    }

    public Document build(@NotNull ScenarioSession session)
    {
        Document document = this.createBase();
        this.buildTestSuites(document, session);

        return document;
    }

    private void buildTestSuites(@NotNull Document document, @NotNull ScenarioSession session)
    {
        Element testSuites = document.createElement(ResultKeys.KEY_TEST_SUITES);
        testSuites.setAttribute(ResultKeys.KEY_SUITES_TIME, String.valueOf(
                this.unixToISO8601(session.getStartedAt(), session.getFinishedAt()))
        );

        Multimap<Plugin, ScenarioResult> results = this.groupingResultsByPlugin(session);

        for (Plugin plugin : results.keySet())
            testSuites.appendChild(this.buildSuite(document, plugin, new ArrayList<>(results.get(plugin))));

        document.appendChild(testSuites);
    }

    private Element buildSuite(@NotNull Document document, @NotNull Plugin plugin, @NotNull List<? extends ScenarioResult> results)
    {

        Element testSuite = document.createElement(ResultKeys.KEY_TEST_SUITE);
        this.buildPluginInfo(testSuite, plugin);
        testSuite.setAttribute(ResultKeys.KEY_SUITE_ID, this.toPluginID(plugin));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_NAME, plugin.getName() + "-" + plugin.getDescription().getVersion());
        testSuite.setAttribute(ResultKeys.KEY_SUITE_TIME, String.valueOf(
                this.unixToISO8601(this.summingResultsTime(results))
        ));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_TESTS, String.valueOf(results.size()));

        // testSuite の attribute にあとで追加するために一時的に保持する
        int failures = 0;
        int skipped = 0;
        int errors = 0;

        for (ScenarioResult result : results)
        {
            ScenarioResultCause cause = result.getCause();
            if (cause.isFailure())
                failures++;
            else if (cause.isSkipped() || cause.isCancelled())
                skipped++;
            else if (cause.isError())
                errors++;

            testSuite.appendChild(this.buildTestCase(document, result));
        }

        testSuite.setAttribute(ResultKeys.KEY_SUITE_FAILURES, String.valueOf(failures));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_SKIPPED, String.valueOf(skipped));
        testSuite.setAttribute(ResultKeys.KEY_SUITE_ERRORS, String.valueOf(errors));

        // stdout を追加
        String completeLog = this.writer.getLogCapture().getFinalEntries()
                .stream()
                .map(LogCapture.TestLogs::getEntries)
                .flatMap(List::stream)
                .map(LogCapture.LogEntry::toString)
                .reduce("", (a, b) -> a + b + "\n");

        Node stdoutCData = document.createCDATASection(completeLog);
        Element stdout = document.createElement(ResultKeys.KEY_SUITE_STDOUT);
        stdout.appendChild(stdoutCData);
        testSuite.appendChild(stdout);


        return testSuite;
    }

    private Element buildTestCase(@NotNull Document document, @NotNull ScenarioResult result)
    {
        Element testCase = document.createElement(ResultKeys.KEY_TEST_CASE);
        testCase.setAttribute(ResultKeys.KEY_CASE_NAME, result.getScenario().getName());
        testCase.setAttribute(ResultKeys.KEY_CASE_TIME, String.valueOf(
                this.unixToISO8601(result.getStartedAt(), result.getFinishedAt()))
        );
        testCase.setAttribute(ResultKeys.KEY_CASE_STATUS, result.getState().name());

        if (result.getCause().isFailure())
            testCase.appendChild(this.buildCauseFailure(document, result));
        else if (result.getCause().isSkipped() || result.getCause().isCancelled())
            testCase.appendChild(document.createElement(ResultKeys.KEY_CASE_SKIPPED));
        else if (result.getCause().isError())
            testCase.appendChild(document.createElement(ResultKeys.KEY_CASE_ERROR));

        // stdout を追加 (ケースごと)
        String completeLog = this.writer.getLogCapture().getEntries(result.getTestID()).stream()
                .map(LogCapture.LogEntry::toString)
                .reduce("", (a, b) -> a + b + "\n");

        Node stdoutCData = document.createCDATASection(completeLog);
        Element stdout = document.createElement(ResultKeys.KEY_CASE_STDOUT);
        stdout.appendChild(stdoutCData);
        testCase.appendChild(stdout);

        return testCase;
    }

    private Element buildCauseError(@NotNull Document document, @NotNull ScenarioResult result)
    {
        Element error = document.createElement(ResultKeys.KEY_CASE_ERROR);
        error.setAttribute(ResultKeys.KEY_CASE_ERROR_TYPE, result.getCause().name());

        Action failedAction = result.getFailedAction();
        if (failedAction != null)
            error.setAttribute(ResultKeys.KEY_CASE_ERROR_MESSAGE, "Failed to pass scenario: " + failedAction.getName());

        return error;
    }

    private Element buildCauseFailure(@NotNull Document document, @NotNull ScenarioResult result)
    {
        Element failure = document.createElement(ResultKeys.KEY_CASE_FAILURE);
        failure.setAttribute(ResultKeys.KEY_CASE_FAILURE_TYPE, result.getCause().name());

        Action failedAction = result.getFailedAction();
        if (failedAction != null)
            failure.setAttribute(ResultKeys.KEY_CASE_FAILURE_MESSAGE, failedAction.getName());

        return failure;
    }

    private void buildPluginInfo(@NotNull Element parent, @NotNull Plugin plugin)
    {
        Document document = parent.getOwnerDocument();
        Element pluginElement = document.createElement(ResultKeys.KEY_PLUGIN);
        pluginElement.setAttribute(ResultKeys.KEY_ID, this.toPluginID(plugin));

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

    private Document createBase()
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

    private String toPluginID(Plugin plugin)
    {
        Pattern pattern = Pattern.compile("[^a-z0-9-]");
        String pluginName = plugin.getName()
                .replace(" ", "-")
                .toLowerCase(Locale.ENGLISH);

        return pattern.matcher(pluginName).replaceAll("");
    }

    private Multimap<Plugin, ScenarioResult> groupingResultsByPlugin(@NotNull ScenarioSession session)
    {
        Multimap<Plugin, ScenarioResult> results = LinkedListMultimap.create();

        for (QueuedScenario scenario : session.getScenarios())
        {
            ScenarioResult result = scenario.getResult();
            if (result != null)
                results.put(scenario.getEngine().getPlugin(), result);
        }

        return results;
    }

    private long summingResultsTime(@NotNull List<? extends ScenarioResult> results)
    {
        return results.stream()
                .mapToLong(result -> result.getFinishedAt() - result.getStartedAt())
                .sum();
    }

    private double unixToISO8601(long unix)
    {
        return unix / 1000.0d;
    }

    private double unixToISO8601(long startUnix, long endUnix)
    {
        return this.unixToISO8601(endUnix - startUnix);
    }
}
