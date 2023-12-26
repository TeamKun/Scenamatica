package org.kunlab.scenamatica.action.input;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputTraverser;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Value
@NotNull
public class InputTokenImpl<T> implements InputToken<T>
{
    String name;
    Class<T> clazz;
    List<Traverser<?, T>> traversers;
    @Nullable
    T defaultValue;

    Map<ScenarioType, Collection<ValidatorElement>> validators;

    private InputTokenImpl(InputTokenImpl<T> token, @Nullable ScenarioType type, Predicate<? super T> validator, String message)
    {
        this.name = token.name;
        this.clazz = token.clazz;
        this.traversers = token.traversers;
        this.defaultValue = token.defaultValue;
        this.validators = Collections.unmodifiableMap(this.appendValidatorsMap(token, type, validator, message).asMap());
    }

    private InputTokenImpl(InputTokenImpl<T> token, @Nullable Traverser<?, T> traverser)
    {
        this.name = token.name;
        this.clazz = token.clazz;
        this.defaultValue = token.defaultValue;
        this.validators = token.validators;

        List<Traverser<?, T>> traversers = new ArrayList<>(token.traversers);
        traversers.add(traverser);
        this.traversers = Collections.unmodifiableList(traversers);
    }

    private InputTokenImpl(InputTokenImpl<T> token, @Nullable T defaultValue)
    {
        this.name = token.name;
        this.clazz = token.clazz;
        this.traversers = token.traversers;
        this.defaultValue = defaultValue == null ? token.defaultValue: defaultValue;
        this.validators = token.validators;
    }

    @SafeVarargs
    private InputTokenImpl(String name, Class<T> clazz, Traverser<?, T>... traversers)
    {
        this(name, clazz, null, traversers);
    }

    @SafeVarargs
    private InputTokenImpl(String name, Class<T> clazz, T defaultValue, Traverser<?, T>... traversers)
    {
        this.name = name;
        this.clazz = clazz;
        this.traversers = Collections.unmodifiableList(Arrays.asList(traversers));
        this.defaultValue = defaultValue;
        this.validators = Collections.emptyMap();
    }

    public static <T> InputToken<T> of(String name, Class<T> clazz, Traverser<?, T> traverser)
    {
        return new InputTokenImpl<>(name, clazz, traverser);
    }

    public static <T> InputToken<T> of(String name, Class<? extends T> clazz)
    {
        // noinspection unchecked,rawtypes
        return new InputTokenImpl<>(name, clazz, null, TraverserImpl.of(clazz, InputTraverser.casted()));
    }

    public static <T> InputToken<T> of(String name, Class<T> clazz, Traverser<?, T> traverser, T defaultValue)
    {
        return new InputTokenImpl<>(name, clazz, defaultValue, traverser);
    }

    public static <T> InputToken<T> of(String name, Class<? extends T> clazz, T defaultValue)
    {
        // noinspection unchecked,rawtypes
        return new InputTokenImpl<>(name, clazz, defaultValue, TraverserImpl.of(clazz, InputTraverser.casted()));
    }

    @NotNull
    private Multimap<ScenarioType, ValidatorElement> appendValidatorsMap(InputTokenImpl<T> token, @Nullable ScenarioType type, Predicate<? super T> validator, String message)
    {
        Multimap<ScenarioType, ValidatorElement> validators = ArrayListMultimap.create(token.validators.size(), 1);
        for (Map.Entry<ScenarioType, Collection<ValidatorElement>> entry : token.validators.entrySet())
        {
            ScenarioType key = entry.getKey();
            Collection<ValidatorElement> value = entry.getValue();
            validators.putAll(key, value);
        }
        validators.put(type, new ValidatorElement(validator, message));
        return validators;
    }

    @Override
    public InputToken<T> validator(ScenarioType type, Predicate<? super T> validator, String message)
    {
        return new InputTokenImpl<>(this, type, validator, message);
    }

    @Override
    public InputToken<T> validator(Predicate<? super T> validator, String message)
    {
        InputToken<T> token = this;
        for (ScenarioType type : ScenarioType.values())
            token = this.validator(type, validator, message);

        return token;
    }

    @Override
    public <I> InputToken<T> traverser(Class<I> clazz, InputTraverser<? super I, ? extends T> traverser)
    {
        return new InputTokenImpl<>(this, TraverserImpl.of(clazz, traverser));
    }

    @Override
    public InputToken<T> defaultValue(T defaultValue)
    {
        return new InputTokenImpl<>(this, defaultValue);
    }

    @Override
    public void validate(ScenarioType type, T value)
    {
        for (ValidatorElement validator : this.validators.get(type))
            if (!validator.validator.test(value))
                throw new IllegalArgumentException(String.format(validator.message, value));
    }

    @Override
    public T traverse(@NotNull StructureSerializer serializer, Object obj)
    {
        for (Traverser<?, T> traverser : this.traversers)
            if (traverser.getInputClazz().isInstance(obj))
                return traverser.tryTraverse(serializer, obj);

        throw new IllegalArgumentException("Unknown traverser type");
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
            return true;
        if (!(object instanceof InputTokenImpl))
            return false;
        InputToken<?> that = (InputToken<?>) object;
        return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getName());
    }

    @Value
    private class ValidatorElement
    {
        Predicate<? super T> validator;
        String message;
    }
}
