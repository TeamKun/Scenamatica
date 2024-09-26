package org.kunlab.scenamatica.scenario.storages;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;

import java.util.HashMap;
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
    private static final String KEY_RUNIF = "runif";

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

    private static boolean containsOutputKey(String key)
    {
        return key.contains(OUTPUT_MARKER) || key.equals(KEY_OUTPUT);
    }

    private static boolean containsOutputKey(String[] keys)
    {
        for (String key : keys)
            if (containsOutputKey(key))
                return true;

        return false;
    }

    private static String getFullQualifiedOutputKey(QueuedScenario scenario, String key)
    {
        return KEY_OUTPUT + KEY_SEPARATOR + scenario.getEngine().getScenario().getName() + KEY_SEPARATOR + key;
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
        if (!containsOutputKey(key))
            throw new IllegalStateException("This storage is read-only: " + key);

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
            throw new BrokenReferenceException(key, KEY, this.map);

        // 末尾の .output を, OUTPUT_IDENTIFIER に置換する
        String generalKey = key.substring(0, key.length() - OUTPUT_MARKER.length() + 1) + OUTPUT_IDENTIFIER;

        QueuedScenario current = this.session.getCurrent();
        super.set(getFullQualifiedOutputKey(current, generalKey), value);
    }

    /* non-public */ Object getScenarioDetail(ScenarioSession session, String[] keys)
    {
        String scenarioName = keys[0];
        if (keys.length == 1)
            throw new BrokenReferenceException("Broken reference: imperfect reference string.", scenarioName);

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
        else if (containsOutputKey(keys))
            return this.getScenarioOutputsDetail(scenario, keys);

        ScenarioResult result = scenario.getResult();
        String key = keys[1];
        switch (key)
        {
            case KEY_SCENARIO_RESULT:
                ensureHasResult(result);
                String resultType = keys[2];
                if (keys.length == 3)
                    return this.getScenarioResultDetail(result, resultType, sliceKey(keys, 3));
                else
                    return result.getCause() == ScenarioResultCause.PASSED;
            case KEY_SCENARIO_STARTED_AT:
                if (result == null && !scenario.isRunning())
                    throw new IllegalStateException("scenario is not started: " + scenario.getEngine().getScenario().getName());
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

    private Pair<Object, Integer> lookupOutputWithFullKey(QueuedScenario scenario, String[] keys)
    {
        final String separator = KEY_SEPARATOR;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < keys.length; i++)
        {
            String key = keys[i];
            if (builder.length() > 0)
                builder.append(separator);
            if (containsOutputKey(key))
            {
                String generalKey = getFullQualifiedOutputKey(scenario, builder + OUTPUT_IDENTIFIER);
                Object lookupResult;
                try
                {
                    lookupResult = super.get(generalKey);
                    return new Pair<>(lookupResult, i);
                }
                catch (BrokenReferenceException e)
                {
                    // builder.append(key);
                    throw e;
                }
            }
            else
                builder.append(key);
        }

        throw new BrokenReferenceException(builder.toString(), null, null);
    }

    private Object getScenarioOutputsDetail(QueuedScenario scenario, String[] keys)
    {
        // キーが, scenario.scenarios.<シナリオ名："output">.output.<キー："output"> の可能性を考慮する。
        Pair<Object, Integer> pair = this.lookupOutputWithFullKey(scenario, keys);
        Object output = pair.getLeft();
        int index = pair.getRight();

        assert output instanceof Map;
        //noinspection unchecked
        Map<String, Object> map = (Map<String, Object>) output;
        if (index == keys.length - 1)
            return map;

        return get(map, sliceKey(keys, index + 1), this.ser);
    }

    private Object getScenarioResultDetail(ScenarioResult result, String resultType, String[] referencePrefix)
    {
        String fullReference = String.join(KEY_SEPARATOR, referencePrefix) + KEY_SEPARATOR + resultType;

        switch (resultType)
        {
            case KEY_SCENARIO_RESULT_CAUSE:
                return result.getCause().toString();
            case KEY_SCENARIO_RESULT_STATE:
                return result.getState().toString();
            case KEY_SCENARIO_RESULT_ATTEMPT_OF:
                return result.getAttemptOf();
            default:
                throw new BrokenReferenceException(fullReference, resultType, new HashMap<String, Object>()
                {{
                    this.put(KEY_SCENARIO_RESULT_CAUSE, result.getCause().toString());
                    this.put(KEY_SCENARIO_RESULT_STATE, result.getState().toString());
                    this.put(KEY_SCENARIO_RESULT_ATTEMPT_OF, result.getAttemptOf());
                }});
        }
    }

    @Override
    public String getKey()
    {
        return KEY;
    }
}
