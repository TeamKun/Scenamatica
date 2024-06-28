package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.compiler.CategoryManager;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.ActionReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.EventReference;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.bookkeeper.utils.MapUtils;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Value
public class CompiledAction implements ICompiled
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SUPER_ACTION = "super";
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
    private static final String KEY_ADMONITIONS = "admonitions";

    ClassNode clazz;
    String id;
    String name;
    ActionReference superAction;
    String description;
    CategoryManager.CategoryEntry category;
    EventReference[] events;
    Contract executable;
    Contract watchable;
    Contract requireable;
    MCVersion supportsSince;
    MCVersion supportsUntil;
    Map<String, ActionInput> inputs;
    Map<String, ActionOutput> outputs;
    GenericAdmonition[] admonitions;

    public CompiledAction(ClassNode clazz, String id, String name, ActionReference superAction, String description, CategoryManager.CategoryEntry category,
                          EventReference[] events,

                          Contract executable,
                          Contract watchable,
                          Contract requireable,

                          MCVersion supportsSince, MCVersion supportsUntil,
                          Map<String, ActionInput> inputs,
                          Map<String, ActionOutput> outputs,
                          GenericAdmonition[] admonitions)
    {
        this.clazz = clazz;
        this.id = id;
        this.name = name;
        this.superAction = superAction;
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
        this.admonitions = admonitions;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_ID, this.id);
        map.put(KEY_NAME, this.name);
        if (this.superAction != null)
            map.put(KEY_SUPER_ACTION, this.superAction.getReference());
        map.put(KEY_DESCRIPTION, this.description);
        if (this.category != null)
            map.put(KEY_CATEGORY, this.category.getReference());
        map.put(KEY_EVENTS, this.events == null ? null: Arrays.stream(this.events).map(EventReference::getReference).toArray());
        map.put(KEY_EXECUTABLE, this.executable.serialize());
        map.put(KEY_WATCHABLE, this.watchable.serialize());
        map.put(KEY_REQUIREABLE, this.requireable.serialize());
        MapUtils.putIfNotNull(map, KEY_SUPPORTS_SINCE, this.supportsSince);
        MapUtils.putIfNotNull(map, KEY_SUPPORTS_UNTIL, this.supportsUntil);
        MapUtils.putIfNotNull(map, KEY_INPUTS, serializeInputs());
        MapUtils.putIfNotNull(map, KEY_OUTPUTS, serializeOutputs());


        if (!(this.admonitions == null || this.admonitions.length == 0))
            map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).toArray());
        return map;
    }

    private Map<String, Object> serializeOutputs()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, ActionOutput> entry : this.outputs.entrySet())
            map.put(entry.getKey(), entry.getValue().serialize());

        return map;
    }

    private Map<String, Object> serializeInputs()
    {
        Map<String, Object> map = new LinkedHashMap<>();
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
        private static final String KEY_INHERITEd_FROM = "inheritedFrom";
        private static final String KEY_ADMONITIONS = "admonitions";

        String name;
        String description;
        ActionMethod[] targets;
        TypeReference type;
        MCVersion supportsSince;
        MCVersion supportsUntil;
        Double min;
        Double max;
        ActionReference inheritedFrom;
        GenericAdmonition[] admonitions;

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_NAME, this.name);
            map.put(KEY_DESCRIPTION, this.description);
            map.put(KEY_TYPE, this.type.getReference());
            MapUtils.putIfNotNull(map, KEY_TARGETS, this.targets);
            MapUtils.putIfNotNull(map, KEY_SUPPORTS_SINCE, this.supportsSince);
            MapUtils.putIfNotNull(map, KEY_SUPPORTS_UNTIL, this.supportsUntil);
            MapUtils.putIfNotNull(map, KEY_MIN, this.min);
            MapUtils.putIfNotNull(map, KEY_MAX, this.max);
            if (this.inheritedFrom != null)
                map.put(KEY_INHERITEd_FROM, this.inheritedFrom.getReference());
            if (!(this.admonitions == null || this.admonitions.length == 0))
                map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).toArray());
            return map;
        }

        public static ActionOutput inherit(ActionOutput value, ActionReference superAction)
        {
            return new ActionOutput(
                    value.name,
                    value.description,
                    value.targets,
                    value.type,
                    value.supportsSince,
                    value.supportsUntil,
                    value.min,
                    value.max,
                    superAction,
                    value.admonitions
            );
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
        private static final String KEY_INHERITED_FROM = "inheritedFrom";
        private static final String KEY_ADMONITIONS = "admonitions";

        String name;
        String description;
        ActionMethod[] requiredOn;
        ActionMethod[] availableFor;
        MCVersion supportsSince;
        MCVersion supportsUntil;
        Double min;
        Double max;
        Object constValue;
        boolean requiresActor;
        ActionReference inheritedFrom;
        GenericAdmonition[] admonitions;

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_NAME, this.name);
            map.put(KEY_DESCRIPTION, this.description);
            MapUtils.putIfNotNull(map, KEY_REQUIRED_ON, this.requiredOn);
            MapUtils.putIfNotNull(map, KEY_AVAILABLE_FOR, this.availableFor);
            MapUtils.putIfNotNull(map, KEY_SUPPORTS_SINCE, this.supportsSince);
            MapUtils.putIfNotNull(map, KEY_SUPPORTS_UNTIL, this.supportsUntil);
            MapUtils.putIfNotNull(map, KEY_MIN, this.min);
            MapUtils.putIfNotNull(map, KEY_MAX, this.max);
            MapUtils.putIfNotNull(map, KEY_CONST_VALUE, this.constValue);
            MapUtils.putIfTrue(map, KEY_REQUIRES_ACTOR, this.requiresActor);
            if (this.inheritedFrom != null)
                map.put(KEY_INHERITED_FROM, this.inheritedFrom.getReference());
            if (!(this.admonitions == null || this.admonitions.length == 0))
                map.put(KEY_ADMONITIONS, Arrays.stream(this.admonitions).map(GenericAdmonition::serialize).toArray());
            return map;
        }

        public static ActionInput inherit(@NotNull ActionInput parent, @NotNull ActionReference ref)
        {
            return new ActionInput(
                    parent.name,
                    parent.description,
                    parent.requiredOn,
                    parent.availableFor,
                    parent.supportsSince,
                    parent.supportsUntil,
                    parent.min,
                    parent.max,
                    parent.constValue,
                    parent.requiresActor,
                    ref,
                    parent.admonitions
            );
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
