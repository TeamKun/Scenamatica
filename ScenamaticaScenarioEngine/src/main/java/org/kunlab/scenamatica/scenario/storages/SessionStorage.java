package org.kunlab.scenamatica.scenario.storages;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.Map;

public class SessionStorage extends AbstractVariableProvider implements ChildStorage
{
    public static final String KEY_SCENARIO_STARTED_AT = "started_t";
    public static final String KEY_SCENARIO_FINISHED_AT = "finished_at";
    public static final String KEY_SCENARIO_TEST_ID = "test_id";

    public static final String KEY_SCENARIO_RESULT = "result";
    public static final String KEY_SCENARIO_RESULT_CAUSE = "cause";
    public static final String KEY_SCENARIO_RESULT_STATE = "state";
    public static final String KEY_SCENARIO_RESULT_ATTEMPT_OF = "attempt_of";

    private final StructureSerializer serializer;

    public SessionStorage(@NotNull StructureSerializer serializer, @NotNull ScenarioSession session)
    {
        super();

        this.serializer = serializer;
        this.putAll(childMap(
                "started_at", func(keys -> session.getStartedAt()),
                "scenarios", func(keys -> {
                    if (keys.length == 0)
                        return session.getScenarios();
                    else
                        return this.getScenarioDetail(session, keys);
                })
        ));
    }

    private static void ensureHasResult(ScenarioResult result)
    {
        if (result == null)
            throw new IllegalArgumentException("scenario is not finished");
    }

    private Object getScenarioDetail(ScenarioSession session, String[] keys)
    {
        String scenarioName = keys[0];
        if (keys.length == 1)
            throw new IllegalArgumentException("Ambiguous key: " + scenarioName);

        QueuedScenario scenario = session.getScenarios().stream()
                .filter(s -> s.getEngine().getScenario().getName().equals(scenarioName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("scenario not found: " + scenarioName));

        return this.getScenarioDetail(scenario, sliceKey(keys, 1));
    }

    /* non-public */ Object getScenarioDetail(@NotNull QueuedScenario scenario, String[] keys)
    {
        ScenarioResult result = scenario.getResult();
        String key = keys[1];
        switch (key)
        {
            case KEY_SCENARIO_RESULT:
                ensureHasResult(result);
                return this.getScenarioResultDetail(result, sliceKey(keys, 2));
            case KEY_SCENARIO_STARTED_AT:
                if (result == null && !scenario.isRunning())
                    throw new IllegalArgumentException("scenario is not started: " + scenario.getEngine().getScenario().getName());
                else
                    return scenario.getStartedAt();
            case KEY_SCENARIO_FINISHED_AT:
                ensureHasResult(result);
                return result.getFinishedAt();
            case KEY_SCENARIO_TEST_ID:
                ensureHasResult(result);
                return result.getTestID().toString();
            default:
                Map<String, Object> map = this.serializer.serialize(scenario.getEngine().getScenario(), ScenarioFileStructure.class);
                return get(map, sliceKey(keys, 1));
        }
    }

    private Object getScenarioResultDetail(ScenarioResult result, String[] keys)
    {
        if (keys.length == 0)
            return result.getScenarioResultCause() == ScenarioResultCause.PASSED;

        String key = keys[0];
        switch (key)
        {
            case KEY_SCENARIO_RESULT_CAUSE:
                return result.getScenarioResultCause().toString();
            case KEY_SCENARIO_RESULT_STATE:
                return result.getState().toString();
            case KEY_SCENARIO_RESULT_ATTEMPT_OF:
                return result.getAttemptOf();
            default:
                throw new IllegalArgumentException("Ambiguous key: " + key);
        }
    }

    @Override
    public String getKey()
    {
        return "session";
    }
}
