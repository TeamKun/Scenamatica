package org.kunlab.scenamatica.action.input;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;
import org.kunlab.scenamatica.interfaces.scenario.SessionVariableHolder;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InputBoardImpl implements InputBoard
{
    private final ScenarioType type;
    private final InputValueHolder<?>[] values;
    // Contracts
    private final List<InputToken<?>[]> oneOf;
    private final Consumer<? super InputBoard> validator;

    private boolean validated;

    public InputBoardImpl(@NotNull ScenarioType type, Consumer<? super InputBoard> validator, @NotNull InputToken<?>... tokens)
    {
        this.type = type;
        this.validator = validator;
        this.values = convertValueHolder(tokens);
        this.oneOf = new ArrayList<>();
        this.validated = false;
    }

    public InputBoardImpl(@NotNull ScenarioType type, @NotNull InputToken<?>... tokens)
    {
        this(type, null, tokens);
    }

    private static InputValueHolder<?>[] convertValueHolder(InputToken<?>[] tokens)
    {
        InputValueHolder<?>[] values = new InputValueHolder<?>[tokens.length];
        for (int i = 0; i < tokens.length; i++)
            values[i] = new InputValueHolderImpl<>(tokens[i]);
        return values;
    }

    @Override
    public InputBoard oneOf(InputToken<?>... tokens)
    {
        for (InputToken<?> token : tokens)
            if (!this.contains(token))
                throw new IllegalArgumentException("Unknown token: " + token.getName());

        this.oneOf.add(tokens);
        return this;
    }

    @Override
    public boolean contains(InputToken<?> token)
    {
        for (InputValueHolder<?> value : this.values)
            if (value.isEquals(token))
                return true;
        return false;
    }

    @Override
    public <T> InputValueHolder<T> getHolder(InputToken<T> token)
    {
        for (InputValueHolder<?> value : this.values)
        {
            if (value.isEquals(token))
                // noinspection unchecked,rawtypes
                return (InputValueHolder) value;
        }

        throw new IllegalArgumentException("Token not found");
    }

    @Override
    public <T> T get(InputToken<T> token)
    {
        return this.getHolder(token).getValue();
    }

    private void validateContract()
    {
        for (InputToken<?>[] tokens : this.oneOf)
        {
            int count = 0;
            for (InputToken<?> token : tokens)
            {
                if (this.getHolder(token).isPresent())
                    count++;
            }

            if (count != 1)
                throw new IllegalArgumentException("Invalid contract: OneOf value must be included only one.");
        }
    }

    @Override
    public void compile(@NotNull StructureSerializer serializer, @NotNull Map<String, Object> map)
    {
        this.validated = false;
        for (InputValueHolder<?> value : this.values)
        {
            Object obj = map.get(value.getToken().getName());
            value.set(serializer, obj);
            if (value.isPresent())
                value.validate(this.type);
        }

        this.validateContract();
    }

    @Override
    public void resolveReferences(@NotNull StructureSerializer serializer, @NotNull SessionVariableHolder variables)
    {
        for (InputValueHolder<?> value : this.values)
        {
            if (value.isPresent())
                continue;

            value.getValueReference().resolve(serializer, variables);

            // assert value.isPresent();
            value.validate(this.type);
        }
    }

    @Override
    public boolean hasUnresolvedReferences()
    {
        for (InputValueHolder<?> value : this.values)
        {
            if (!value.isPresent())
                return true;
        }
        return false;
    }

    @Override
    public void validate()
    {
        if (this.validator != null)
            this.validator.accept(this);

        this.validated = true;
    }

    @Override
    public String getValuesString()
    {
        boolean isAppended = false;
        StringBuilder builder = new StringBuilder();
        for (InputValueHolder<?> value : this.values)
        {
            boolean shouldAppend = value.isPresent();
            if (!shouldAppend)
                continue;

            if (isAppended)
                builder.append(", ");
            else
                isAppended = true;

            String tokenName = value.getToken().getName();
            builder.append(tokenName).append("=").append(value.getValue());
        }

        return builder.toString();
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof InputBoardImpl))
            return false;

        InputBoardImpl arg = (InputBoardImpl) argument;

        if (this.values.length != arg.values.length)
            return false;

        for (int i = 0; i < this.values.length; i++)
        {
            if (!this.values[i].equals(arg.values[i]))
                return false;
        }

        return true;
    }
}
