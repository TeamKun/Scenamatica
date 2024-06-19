package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.CategoryReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.EventReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Value
public class CompiledAction implements ICompiled
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_EVENTS = "events";
    private static final String KEY_EXECUTABLE = "executable";
    private static final String KEY_WATCHABLE = "watchable";
    private static final String KEY_REQUIREABLE = "requireable";
    private static final String KEY_SUPPORTS_SINCE = "supports_since";
    private static final String KEY_SUPPORTS_UNTIL = "supports_until";
    private static final String KEY_INPUTS = "inputs";
    private static final String KEY_OUTPUTS = "outputs";

    String id;
    String name;
    String description;
    CategoryReference category;
    EventReference[] events;
    Contract executable;
    Contract watchable;
    Contract requireable;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    Map<String, ActionInput> inputs;
    Map<String, ActionOutput> outputs;

    public CompiledAction(String id, String name, String description, CategoryReference category,
                          EventReference[] events,

                          Contract executable,
                          Contract watchable,
                          Contract requireable,

                          MCVersion supportsSince, MCVersion supportsUntil,
                          Map<String, ActionInput> inputs,
                          Map<String, ActionOutput> outputs)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.events = events;
        this.executable = executable;
        this.watchable = watchable;
        this.requireable = requireable;
        this.supportsSince = supportsSince;
        this.supportsUntil = supportsUntil;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_ID, this.id);
        map.put(KEY_NAME, this.name);
        map.put(KEY_DESCRIPTION, this.description);
        map.put(KEY_CATEGORY, this.category == null ? null: this.category.getID());
        map.put(KEY_EVENTS, this.events == null ? null: Arrays.stream(this.events).map(EventReference::getID).toArray());
        map.put(KEY_EXECUTABLE, this.executable.serialize());
        map.put(KEY_WATCHABLE, this.watchable.serialize());
        map.put(KEY_REQUIREABLE, this.requireable.serialize());
        map.put(KEY_SUPPORTS_SINCE, this.supportsSince);
        map.put(KEY_SUPPORTS_UNTIL, this.supportsUntil);
        map.put(KEY_INPUTS, serializeInputs());
        map.put(KEY_OUTPUTS, serializeOutputs());
        return map;
    }

    private Map<String, Object> serializeOutputs()
    {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, ActionOutput> entry : this.outputs.entrySet())
            map.put(entry.getKey(), entry.getValue().serialize());

        return map;
    }

    private Map<String, Object> serializeInputs()
    {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, ActionInput> entry : this.inputs.entrySet())
            map.put(entry.getKey(), entry.getValue().serialize());

        return map;
    }

    @Value
    public static class ActionOutput
    {
        private static final String KEY_NAME = "name";
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_TARGETS = "targets";
        private static final String KEY_TYPE = "type";
        private static final String KEY_SUPPORTS_SINCE = "supportsSince";
        private static final String KEY_SUPPORTS_UNTIL = "supportsUntil";
        private static final String KEY_MIN = "min";
        private static final String KEY_MAX = "max";

        String name;
        String description;
        ActionMethod[] targets;
        TypeReference type;
        MCVersion supportsSince;
        MCVersion supportsUntil;
        double min;
        double max;

        public ActionOutput(String name, String description, ActionMethod[] targets, TypeReference type,
                            MCVersion supportsSince, MCVersion supportsUntil, double min, double max)
        {
            this.name = name;
            this.description = description;
            this.targets = targets;
            this.type = type;
            this.supportsSince = supportsSince;
            this.supportsUntil = supportsUntil;
            this.min = min;
            this.max = max;
        }

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_NAME, this.name);
            map.put(KEY_DESCRIPTION, this.description);
            map.put(KEY_TARGETS, this.targets);
            map.put(KEY_TYPE, this.type.getID());
            map.put(KEY_SUPPORTS_SINCE, this.supportsSince);
            map.put(KEY_SUPPORTS_UNTIL, this.supportsUntil);
            map.put(KEY_MIN, this.min);
            map.put(KEY_MAX, this.max);
            return map;
        }
    }

    @Value
    public static class ActionInput
    {
        private static final String KEY_NAME = "name";
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_REQUIRED_ON = "requiredOn";
        private static final String KEY_AVAILABLE_FOR = "availableFor";
        private static final String KEY_SUPPORTS_SINCE = "supportsSince";
        private static final String KEY_SUPPORTS_UNTIL = "supportsUntil";
        private static final String KEY_MIN = "min";
        private static final String KEY_MAX = "max";
        private static final String KEY_CONST_VALUE = "const";
        private static final String KEY_REQUIRES_ACTOR = "requiresActor";

        String name;
        String description;
        ActionMethod[] requiredOn;
        ActionMethod[] availableFor;
        MCVersion supportsSince;
        MCVersion supportsUntil;
        double min;
        double max;
        Object constValue;
        boolean requiresActor;

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_NAME, this.name);
            map.put(KEY_DESCRIPTION, this.description);
            map.put(KEY_REQUIRED_ON, this.requiredOn);
            map.put(KEY_AVAILABLE_FOR, this.availableFor);
            map.put(KEY_SUPPORTS_SINCE, this.supportsSince);
            map.put(KEY_SUPPORTS_UNTIL, this.supportsUntil);
            map.put(KEY_MIN, this.min);
            map.put(KEY_MAX, this.max);
            map.put(KEY_CONST_VALUE, this.constValue);
            map.put(KEY_REQUIRES_ACTOR, this.requiresActor);
            return map;
        }


    }

    @Value
    public static class Contract
    {
        boolean available;
        String description;

        private Contract(boolean available, String description)
        {
            this.available = available;
            if (!(available || description == null))
                throw new IllegalArgumentException("Description must be null if the action is unavailable.");
            this.description = description;
        }

        public Object serialize()
        {
            if (!this.available)
                return false;

            return this.description == null ? true: this.description;
        }

        public static Contract ofAvailable(@NotNull String desc)
        {
            return new Contract(true, desc);
        }

        public static Contract ofUnavailable()
        {
            return new Contract(false, null);
        }
    }
}
