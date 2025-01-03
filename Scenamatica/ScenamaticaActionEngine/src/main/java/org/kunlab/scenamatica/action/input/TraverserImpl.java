package org.kunlab.scenamatica.action.input;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.interfaces.action.input.InputTraverser;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

@Value
public class TraverserImpl<I, O> implements Traverser<I, O>
{
    Class<? extends I> inputClazz;
    InputTraverser<? super I, ? extends O> traverser;

    public TraverserImpl(Class<? extends I> inputClass, InputTraverser<? super I, ? extends O> traverser)
    {
        this.inputClazz = inputClass;
        this.traverser = traverser;
    }

    public static <T, R> Traverser<T, R> of(Class<? extends T> inputClass, InputTraverser<? super T, ? extends R> traverser)
    {
        return new TraverserImpl<>(inputClass, traverser);
    }

    @Override
    public O traverse(@NotNull StructureSerializer serializer, I obj) throws InvalidScenarioFileException
    {
        return this.traverser.traverse(serializer, obj);
    }

    @Override
    public O tryTraverse(@NotNull StructureSerializer serializer, Object obj) throws InvalidScenarioFileException
    {
        if (this.inputClazz.isInstance(obj))
            return this.traverse(serializer, this.inputClazz.cast(obj));
        else
            throw new IllegalArgumentException("obj is not instance of " + this.inputClazz.getName());
    }
}
