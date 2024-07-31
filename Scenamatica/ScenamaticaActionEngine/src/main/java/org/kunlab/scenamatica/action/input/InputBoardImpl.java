package org.kunlab.scenamatica.action.input;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InputBoardImpl implements InputBoard
{
    private final ScenarioType type;
    private final List<InputValueHolder<?>> values;
    // Contracts
    private final List<InputToken<?>[]> oneOf;
    private final List<InputToken<?>[]> requiredNonNull;
    private final List<ValidatorElement> validators;

    private boolean hasUnresolved;
    private boolean validated;

    private InputValueHolder<?> lastExistCheck;

    public InputBoardImpl(@NotNull ScenarioType type, @NotNull InputToken<?>... tokens)
    {
        this.type = type;
        this.values = convertValueHolder(tokens);
        this.oneOf = new ArrayList<>();
        this.requiredNonNull = new ArrayList<>();
        this.validators = new ArrayList<>();
        this.validated = false;
        if (tokens.length > 0)
            this.hasUnresolved = true;
    }

    private static List<InputValueHolder<?>> convertValueHolder(InputToken<?>[] tokens)
    {
        List<InputValueHolder<?>> values = new ArrayList<>();
        for (InputToken<?> token : tokens)
            values.add(new InputValueHolderImpl<>(token));
        return values;
    }

    @Override
    public InputBoard oneOf(InputToken<?>... tokens)
    {
        this.ensureAllContains(tokens);
        this.oneOf.add(tokens);
        this.validated = false;
        return this;
    }

    @Override
    public InputBoard requirePresent(InputToken<?>... tokens)
    {
        this.ensureAllContains(tokens);
        this.requiredNonNull.add(tokens);
        this.validated = false;
        return this;
    }

    private InputBoard ensureAllContains(InputToken<?>[] tokens)
    {
        for (InputToken<?> token : tokens)
            if (!this.contains(token))
                throw new IllegalArgumentException("Unknown token: " + token.getName());

        return this;
    }

    @Override
    public InputBoard register(InputToken<?> token)
    {
        if (this.contains(token))
        {
            this.unregister(token);
            this.validated = false;
        }

        this.values.add(new InputValueHolderImpl<>(token));
        this.hasUnresolved = true;
        this.validated = false;
        return this;
    }

    private void unregister(InputToken<?> token)
    {
        this.values.removeIf(value -> value.isEquals(token));
    }

    @Override
    public InputBoard registerAll(InputToken<?>... token)
    {
        for (InputToken<?> t : token)
            this.register(t);

        return this;
    }

    @Override
    public InputBoard validator(@NotNull Predicate<? super InputBoard> validator, @Nullable String validateFailedMessage)
    {
        this.validated = false;
        this.validators.add(new ValidatorElement(validator, validateFailedMessage));
        return this;
    }

    @Override
    public boolean contains(InputToken<?> token)
    {
        for (InputValueHolder<?> value : this.values)
        {
            if (value.isEquals(token))
            {
                this.lastExistCheck = value;
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> InputValueHolder<T> getHolder(InputToken<T> token)
    {
        InputValueHolder<?> lastHolder = this.lastExistCheck;
        if (lastHolder != null && lastHolder.isEquals(token))
            // noinspection unchecked
            return (InputValueHolder<T>) lastHolder;

        for (InputValueHolder<?> value : this.values)
        {
            if (value.isEquals(token))
                // noinspection unchecked,rawtypes
                return (InputValueHolder) value;
        }

        throw new IllegalArgumentException("Token " + token.getName() + " is not registered.");
    }

    @Override
    public <T> T get(InputToken<T> token)
    {
        return this.getHolder(token).getValue();
    }

    @Override
    public <T> T orElse(InputToken<? extends T> token, @NotNull Supplier<? extends T> defaultValue)
    {
        if (this.isResolved(token))
            return this.get(token);
        else
            return defaultValue.get();
    }

    @Override
    public boolean has(@NotNull InputToken<?> token)
    {
        return this.getHolder(token).isResolved();
    }

    @Override
    public boolean isResolved(@NotNull InputToken<?> token)
    {
        return this.getHolder(token).isResolved();
    }

    @Override
    public <T, U> U ifResolved(@NotNull InputToken<T> token, @NotNull Function<? super InputValueHolder<T>, ? extends U> mapper, @Nullable U defaultValue)
    {
        if (this.isResolved(token))
            return mapper.apply(this.getHolder(token));
        else
            return defaultValue;
    }

    @Override
    public <T> boolean ifResolved(@NotNull InputToken<T> token, @NotNull Predicate<? super InputValueHolder<T>> predicate, boolean defaultValue)
    {
        if (this.isResolved(token))
            return predicate.test(this.getHolder(token));
        else
            return defaultValue;
    }

    @Override
    public <T> boolean isResolved(@NotNull InputToken<T> token, @NotNull Predicate<? super InputValueHolder<T>> predicate)
    {
        return this.ifResolved(token, predicate, false);
    }

    @Override
    public boolean isPresent(@NotNull InputToken<?> token)
    {
        return this.isResolved(token) && this.getHolder(token).isNotNull();
    }

    @Override
    public <T, U> U ifPresent(@NotNull InputToken<T> token, @NotNull Function<? super T, ? extends U> mapper, @Nullable U defaultValue)
    {
        if (this.isPresent(token))
            return mapper.apply(this.get(token));
        else
            return defaultValue;
    }

    @Override
    public <T> boolean ifPresent(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate, boolean defaultValue)
    {
        if (this.isPresent(token))
            return predicate.test(this.get(token));
        else
            return defaultValue;
    }

    @Override
    public <T> boolean ifPresent(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate)
    {
        return this.ifPresent(token, predicate, true);
    }

    @Override
    public <T> boolean runIfPresent(@NotNull InputToken<T> token, @NotNull Consumer<? super T> predicate)
    {
        if (this.isPresent(token))
        {
            predicate.accept(this.get(token));
            return true;
        }
        else
            return false;
    }

    private void validateContract()
    {
        for (InputToken<?>[] tokens : this.oneOf)
        {
            int count = 0;
            for (InputToken<?> token : tokens)
            {
                if (this.getHolder(token).isResolved())
                    count++;
            }

            if (count != 1)
                throw new IllegalArgumentException("Invalid contract: OneOf value " + Arrays.stream(tokens)
                        .map(InputToken::getName)
                        .collect(Collectors.joining(", "))
                        + " must be included.");
        }

        for (InputToken<?>[] tokens : this.requiredNonNull)
            for (InputToken<?> token : tokens)
                if (this.getHolder(token).isNull())
                    throw new IllegalArgumentException("Invalid contract: Required value '" + token.getName() + "' must be included and not null.");
    }

    @Override
    public void compile(@NotNull StructureSerializer serializer, @NotNull Map<String, Object> map)
    {
        this.validated = false;
        boolean allResolved = true;
        for (InputValueHolder<?> value : this.values)
        {
            if (!map.containsKey(value.getToken().getName()))
            {
                value.setEmpty();
                continue;
            }

            Object obj = map.get(value.getToken().getName());
            value.set(serializer, obj);
            if (value.isResolved())
                value.validate(this.type);
            else
                allResolved = false;
        }

        this.hasUnresolved = !allResolved;

        this.validateContract();
    }

    @Override
    public void resolveReferences(@NotNull StructureSerializer serializer, @NotNull SessionStorage variables)
    {
        for (InputValueHolder<?> value : this.values)
        {
            if (value.isResolved() || value.isEmpty())
                continue;

            value.getValueReference().resolve(serializer, variables);

            // assert value.isPresent();
            value.validate(this.type);
        }

        this.validate();
        this.hasUnresolved = false;
    }

    @Override
    public void releaseReferences()
    {
        if (!this.values.isEmpty())
            this.hasUnresolved = true;

        for (InputValueHolder<?> value : this.values)
            value.getValueReference().release();
    }

    @Override
    public boolean hasUnresolvedReferences()
    {
        return this.hasUnresolved;
    }

    @Override
    public void validate()
    {
        if (this.validated)
            return;

        for (ValidatorElement validator : this.validators)
        {
            if (!validator.validator.test(this))
                throw new IllegalArgumentException(validator.message);
        }

        this.validated = true;
    }

    @Override
    public String getValuesString()
    {
        boolean isAppended = false;
        StringBuilder builder = new StringBuilder();
        for (InputValueHolder<?> value : this.values)
        {
            boolean shouldAppend = value.isResolved();
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

        if (this.values.size() != arg.values.size())
            return false;

        for (int i = 0; i < this.values.size(); i++)
        {
            if (!this.values.get(i).equals(arg.values.get(i)))
                return false;
        }

        return true;
    }

    @Value
    private static class ValidatorElement
    {
        Predicate<? super InputBoard> validator;
        String message;
    }
}
