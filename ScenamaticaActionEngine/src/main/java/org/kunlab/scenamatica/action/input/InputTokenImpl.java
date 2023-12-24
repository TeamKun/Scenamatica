package org.kunlab.scenamatica.action.input;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputTraverser;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Value
@NotNull
public class InputTokenImpl<T> implements InputToken<T>
{
    String name;
    Class<T> clazz;
    List<Traverser<?, T>> traversers;
    T defaultValue;

    EnumMap<ScenarioType, Consumer<? super T>> validators;

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
        this.traversers = new ArrayList<>(Arrays.asList(traversers));
        this.defaultValue = defaultValue;
        this.validators = new EnumMap<>(ScenarioType.class);
    }

    static <T> InputToken<T> of(String name, Class<T> clazz, Traverser<?, T> traverser)
    {
        return new InputTokenImpl<>(name, clazz, traverser);
    }

    @Override
    public InputToken<T> validator(ScenarioType type, Consumer<? super T> validator)
    {
        this.validators.put(type, validator);
        return this;
    }

    @Override
    public <I> InputToken<T> traverser(Class<I> clazz, InputTraverser<? super I, ? extends T> traverser)
    {
        this.traversers.add(TraverserImpl.of(clazz, traverser));
        return this;
    }

    @Override
    public void validate(ScenarioType type, T value)
    {
        Consumer<? super T> validator = this.validators.get(type);
        if (validator != null)
            validator.accept(value);
    }

    @Override
    public T traverse(Object obj)
    {
        for (Traverser<?, T> traverser : this.traversers)
            if (traverser.getInputClazz().isInstance(obj))
                return traverser.tryTraverse(obj);

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
        return Objects.equals(this.getName(), that.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getName());
    }
}
