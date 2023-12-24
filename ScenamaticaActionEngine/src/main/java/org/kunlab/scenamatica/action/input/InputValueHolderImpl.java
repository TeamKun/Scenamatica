package org.kunlab.scenamatica.action.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputReference;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;

import java.util.Objects;

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
        if (!this.isPresent())
            throw new IllegalStateException("Value is not present");

        return this.valueReference.getValue();
    }

    @Override
    public void set(@Nullable Object obj)
    {
        if (obj == null)
            this.valueReference = InputReferenceImpl.valued(this.token, this.token.getDefaultValue());
        else if (InputReferenceImpl.containsReference(obj))
            // assert obj instanceof String
            this.valueReference = new InputReferenceImpl<>(this.token, obj);
        else
            this.valueReference = InputReferenceImpl.valued(this.token, this.token.traverse(obj));
    }

    @Override
    public boolean isPresent()
    {
        return this.valueReference != null && this.valueReference.isResolved();
    }

    @Override
    public void validate(ScenarioType type)
    {
        this.token.validate(type, this.valueReference.getValue());
    }

}
