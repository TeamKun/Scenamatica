package org.kunlab.scenamatica.action.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputReference;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Data
public class InputValueHolderImpl<T> implements InputValueHolder<T>
{
    private final InputToken<T> token;
    @Setter(AccessLevel.NONE)
    private InputReference<T> valueReference;

    public InputValueHolderImpl(InputToken<T> token)
    {
        this.token = token;
        this.valueReference = InputReferenceImpl.empty(token);
    }

    @Override
    public boolean isEquals(InputToken<?> token)
    {
        return this.token == token;  // token はメモリ上で一意なので == で比較して良い
    }

    public boolean isEquals(InputValueHolder<?> holder)
    {
        return this.token == holder.getToken()  // token はメモリ上で一意なので == で比較して良い
                && Objects.equals(this.valueReference, holder.getValueReference());
    }

    @Override
    public T getValue()
    {
        if (!this.isResolved())
            throw new IllegalStateException("Value is not present");

        return this.valueReference.getValue();
    }

    @Override
    public boolean isNull()
    {
        return this.isResolved() && this.valueReference.getValue() == null;
    }

    @Override
    public boolean isNotNull()
    {
        return this.isResolved() && this.valueReference.getValue() != null;
    }

    @Override
    public <U> U ifNotNull(@NotNull Function<? super T, ? extends U> mapper, @Nullable U defaultValue)
    {
        if (this.isNotNull())
            return mapper.apply(this.valueReference.getValue());
        else
            return defaultValue;
    }

    @Override
    public boolean ifNotNull(@NotNull Predicate<? super T> predicate, boolean defaultValue)
    {
        if (this.isNotNull())
            return predicate.test(this.valueReference.getValue());
        else
            return defaultValue;
    }

    @Override
    public boolean ifNotNull(@NotNull Predicate<? super T> predicate)
    {
        return this.ifNotNull(predicate, false);
    }

    @Override
    public void set(@NotNull StructureSerializer serializer, @Nullable Object obj)
    {
        if (obj == null)
            this.valueReference = InputReferenceImpl.valued(this.token, this.token.getDefaultValue());
        else if (InputReferenceImpl.containsReference(obj))
            // assert obj instanceof String
            this.valueReference = InputReferenceImpl.references(this.token, obj);
        else
            this.valueReference = InputReferenceImpl.valued(this.token, this.token.traverse(serializer, obj));
    }

    @Override
    public void setEmpty()
    {
        this.valueReference = InputReferenceImpl.empty(this.token);
    }

    @Override
    public boolean isEmpty()
    {
        return this.valueReference.isEmpty();
    }

    @Override
    public boolean isResolved()
    {
        return this.valueReference != null && this.valueReference.isResolved();
    }

    @Override
    public void validate(ScenarioType type)
    {
        this.token.validate(type, this.valueReference.getValue());
    }

}
