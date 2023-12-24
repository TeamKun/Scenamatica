package org.kunlab.scenamatica.action.input;

import lombok.Value;
import org.kunlab.scenamatica.interfaces.action.input.InputTraverser;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;

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
    public O traverse(I obj)
    {
        return this.traverser.traverse(obj);
    }

    @Override
    public O tryTraverse(Object obj)
    {
        if (this.inputClazz.isInstance(obj))
            return this.traverse(this.inputClazz.cast(obj));
        else
            throw new IllegalArgumentException("obj is not instance of " + this.inputClazz.getName());
    }
}
