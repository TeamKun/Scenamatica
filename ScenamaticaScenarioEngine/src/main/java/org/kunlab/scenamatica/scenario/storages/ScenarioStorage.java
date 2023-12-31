package org.kunlab.scenamatica.scenario.storages;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.Map;
import java.util.UUID;

public class ScenarioStorage extends AbstractVariableProvider implements ChildStorage
{
    public static final String KEY = "scenario";

    public static final String KEY_SCENARIO_STARTED_AT = "started_at";
    public static final String KEY_SCENARIO_FINISHED_AT = "finished_at";
    public static final String KEY_SCENARIO_TEST_ID = "test_id";

    public static final String KEY_SCENARIO_RESULT = "result";
    public static final String KEY_SCENARIO_RESULT_CAUSE = "cause";
    public static final String KEY_SCENARIO_RESULT_STATE = "state";
    public static final String KEY_SCENARIO_RESULT_ATTEMPT_OF = "attempt_of";

    private static final String KEY_OUTPUT = "output";
    private static final String KEY_TRIGGER = "trigger";

    private static final String OUTPUT_MARKER = "." + KEY_OUTPUT;

    private static final String OUTPUT_IDENTIFIER = "$" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "$";

    private final ScenarioSession session;

    public ScenarioStorage(@NotNull ScenarioSession session, @NotNull StructureSerializer serializer)
    {
        super(serializer);
        this.session = session;
    }

    private static void ensureHasResult(ScenarioResult result)
    {
        if (result == null)
            throw new IllegalArgumentException("scenario is not finished");
    }

    private static boolean isOutputKey(String key)
    {
        return key.endsWith(OUTPUT_MARKER) || key.equals(KEY_OUTPUT);
    }

    private static String getOutputkey(QueuedScenario scenario, String key)
    {
        return KEY_OUTPUT + "." + scenario.getEngine().getScenario().getName() + "." + key;
    }

    @Override
    public @Nullable Object get(@NotNull String key)
    {
        QueuedScenario current = this.session.getCurrent();
        assert current != null;

        return this.getScenarioDetail(current, splitKey(key));
    }

    @Override
    public void set(@NotNull String key, @Nullable Object value)
    {
        if (!isOutputKey(key))
            throw new IllegalArgumentException("This storage is read-only: " + key);

        /*
         1. runOn == TRIGGER
            1.1. runAs == NORMAL => ERR
            1.2. runAs == RUNIF
                scenario.trigger.runif.output
            1.3. runAs == BEFORE
                scenario.trigger.before.<idx|name>.output
            1.4. runAs == AFTER
                scenario.trigger.after.<idx|name>.output
        2. runOn == SCENARIOS
            2.1. runAs == NORMAL
                scenario.scenarios.<idx|name>.output
            2.2. runAs == RUNIF
                scenario.scenarios.runif.output
            2.3. runAs == BEFORE => ERR
            2.4. runAs == AFTER => ERR
        3. runOn == RUNIF
            3.1. runAs == NORMAL => ERR
            3.2. runAs == RUNIF
                scenario.runif.output
            3.3. runAs == BEFORE => ERR
            3.4. runAs == AFTER => ERR
         */
        String[] keys = splitKey(key);
        if (keys.length < 2)
            throw new IllegalArgumentException("Ambiguous key: " + key);

        // 末尾の .output を, OUTPUT_IDENTIFIER に置換する
        String generalKey = key.substring(0, key.length() - OUTPUT_MARKER.length()) + OUTPUT_IDENTIFIER;

        QueuedScenario current = this.session.getCurrent();
        super.set(getOutputkey(current, generalKey), value);
    }

    /* non-public */ Object getScenarioDetail(ScenarioSession session, String[] keys)
    {
        String scenarioName = keys[0];
        if (keys.length == 1)
            throw new IllegalArgumentException("Ambiguous key: " + scenarioName);

        QueuedScenario scenario = session.getScenarios().stream()
                .filter(s -> s.getEngine().getScenario().getName().equalsIgnoreCase(scenarioName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("scenario not found: " + scenarioName));

        return this.getScenarioDetail(scenario, sliceKey(keys, 1));
    }

    private Object getScenarioDetail(@NotNull QueuedScenario scenario, String[] keys)
    {
        assert this.ser != null;

        if (keys.length == 0)
            return this.ser.serialize(scenario.getEngine().getScenario(), ScenarioFileStructure.class);
        else if (isOutputKey(keys[keys.length - 1]))
            return this.getScenarioOutputsDetail(scenario, keys);

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
                UUID id = scenario.getEngine().getTestID();
                if (id == null)
                    return null;
                else
                    return id.toString();
            case KEY_TRIGGER:
                Map<String, Object> trigger = this.ser.serialize(scenario.getEngine().getRanBy(), TriggerStructure.class);
                return get(trigger, sliceKey(keys, 2));  // {scenario, trigger, <keys>...}
            default:
                Map<String, Object> map = this.ser.serialize(scenario.getEngine().getScenario(), ScenarioFileStructure.class);
                return get(map, sliceKey(keys, 1));  // {scenario, <keys>...}
        }
    }

    private Pair<Object, Integer> lookupOutput(QueuedScenario scenario, String[] keys)
    {
        final String separator = KEY_SEPARATOR.replace(".", "\\.");
        StringBuilder builder = new StringBuilder(KEY_OUTPUT);
        for (int i = 0; i < keys.length; i++)
        {
            String key = keys[i];
            if (builder.length() > 0)
                builder.append(separator);
            if (isOutputKey(key))
            {
                String generalKey = getOutputkey(scenario, builder + "." + OUTPUT_IDENTIFIER);
                Object lookupResult = super.get(generalKey);
                if (lookupResult == null)
                    builder.append(key);
                else
                    return new Pair<>(lookupResult, i);
            }
            else
                builder.append(key);
        }

        throw new IllegalArgumentException("Ambiguous key: " + builder);
    }

    private Object getScenarioOutputsDetail(QueuedScenario scenario, String[] keys)
    {
        Pair<Object, Integer> pair = this.lookupOutput(scenario, keys);
        // キーが, scenario.scenarios.<シナリオ名："output">.output.<キー："output"> の可能性を考慮する。
        Object output = pair.getLeft();
        int index = pair.getRight();

        assert output instanceof Map;
        //noinspection unchecked
        Map<String, Object> map = (Map<String, Object>) output;
        if (index == keys.length - 1)
            return map;

        return get(map, sliceKey(keys, index + 1), this.ser);
    }

    private Object getScenarioResultDetail(ScenarioResult result, String[] keys)
    {
        if (keys.length == 0)
            return result.getCause() == ScenarioResultCause.PASSED;

        String key = keys[0];
        switch (key)
        {
            case KEY_SCENARIO_RESULT_CAUSE:
                return result.getCause().toString();
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
        return KEY;
    }
}
