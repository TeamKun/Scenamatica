package org.kunlab.scenamatica.reporter;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@NotNull
public class ScenarioResultSet
{
    private final long startedAt;
    private final long finishedAt;
    private final long elapsed;

    private final List<QueuedScenario> scenarios;
    private final List<ScenarioResult> results;

    private final List<ScenarioResult> passes;
    private final List<ScenarioResult> failures;
    private final List<ScenarioResult> cancels;
    private final List<ScenarioResult> skips;
    private final List<ScenarioResult> errors;
    private final List<ScenarioResult> flakes;

    private final int countTotal;
    private final int countPasses;
    private final int countFailures;
    private final int countCancels;
    private final int countSkips;
    private final int countErrors;
    private final int countFlakes;

    private final int totalRan;

    public ScenarioResultSet(long startedAt, long finishedAt, List<QueuedScenario> scenarios)
    {
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.elapsed = finishedAt - startedAt;

        this.scenarios = scenarios;
        this.results = retrieveResults(scenarios);

        this.passes = new ArrayList<>();
        this.failures = new ArrayList<>();
        this.cancels = new ArrayList<>();
        this.skips = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.flakes = new ArrayList<>();

        this.categorize();

        this.countTotal = this.results.size();
        this.countPasses = this.passes.size();
        this.countFailures = this.failures.size();
        this.countCancels = this.cancels.size();
        this.countSkips = this.skips.size();
        this.countErrors = this.errors.size();
        this.countFlakes = this.flakes.size();

        this.totalRan = this.countTotal - this.countSkips - this.countCancels;
    }

    private static List<ScenarioResult> retrieveResults(List<? extends QueuedScenario> scenarios)
    {
        return scenarios.stream()
                .map(QueuedScenario::getResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<? extends ScenarioResult> pickupFlakes(List<? extends ScenarioResult> results)
    {
        return new ArrayList<>(results.stream()
                // 2回以上実行されているものを抽出
                .filter(r -> r.getAttemptOf() > 1)
                // 後に成功したものを抽出（2回以上実行 ＝ 1回目..は失敗）
                .filter(r -> !r.getScenarioResultCause().isFailure())
                // 最大試行回数のものを抽出
                .collect(Collectors.toMap(
                        r -> Arrays.asList(r.getTestID(), r.getScenario()),
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparingInt(ScenarioResult::getAttemptOf))
                ))
                .values());
    }

    private static String formatTime(long time)
    {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
    }

    private void categorize()
    {
        for (ScenarioResult result : this.results)
        {
            switch (result.getScenarioResultCause())
            {
                case PASSED:
                    this.passes.add(result);
                    break;
                case SKIPPED:
                    this.skips.add(result);
                    break;
                case INTERNAL_ERROR:
                    this.errors.add(result);
                    break;
                case CANCELLED:
                    this.cancels.add(result);
                    break;
                default:
                    this.failures.add(result);
                    break;
            }
        }

        this.flakes.addAll(pickupFlakes(this.passes));
    }

    public boolean hasFailures()
    {
        return this.countFailures > 0;
    }

    public boolean hasFlakes()
    {
        return this.countFlakes > 0;
    }

    public boolean isAllPassed()
    {
        return this.countTotal == this.countPasses;
    }

    public boolean isNothingTested()
    {
        return this.countTotal == 0;
    }

    public String getStartedAtString()
    {
        return formatTime(this.startedAt);
    }

    public String getFinishedAtString()
    {
        return formatTime(this.finishedAt);
    }

    public String getElapsedString()
    {
        return formatTime(this.elapsed);
    }
}
