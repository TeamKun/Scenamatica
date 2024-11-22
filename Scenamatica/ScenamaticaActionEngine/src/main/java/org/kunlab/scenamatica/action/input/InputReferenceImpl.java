package org.kunlab.scenamatica.action.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.action.input.InputReference;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.Objects;

@Data
public class InputReferenceImpl<T> implements InputReference<T>
{

    @NotNull
    private final InputToken<T> token;
    @Nullable
    private final Object referencing;
    @Nullable
    private final Object rawValue;
    @Getter
    @NotNull
    private final String[] containingReferences;

    @Setter(AccessLevel.NONE)
    private T value;
    @Setter(AccessLevel.NONE)
    private boolean isResolved;

    public InputReferenceImpl(@NotNull InputToken<T> token, @Nullable Object referencing, T value, @Nullable Object rawValue, boolean isResolved) throws YamlParsingException
    {
        this.token = token;
        this.referencing = referencing;
        this.containingReferences = referencing == null ? new String[0]: ReferenceResolver.selectReferences(referencing);
        this.value = value;
        this.rawValue = rawValue;
        this.isResolved = isResolved;
    }

    public static <D> InputReference<D> valued(InputToken<D> token, D value) throws YamlParsingException
    {
        return new InputReferenceImpl<>(token, null, value, value, true);
    }

    public static <D> InputReference<D> valuedCast(InputToken<D> token, StructureSerializer serializer, Object value)
            throws InvalidScenarioFileException
    {
        return new InputReferenceImpl<>(token, null, SmartCaster.smartCast(token, serializer, value), value, true);
    }

    @SneakyThrows
    public static <D> InputReference<D> empty(InputToken<D> token)
    {
        return new InputReferenceImpl<>(token, null, null, null, false);
    }

    public static <D> InputReference<D> references(InputToken<D> token, Object referencing) throws YamlParsingException
    {
        return new InputReferenceImpl<>(token, referencing, null, null, false);
    }

    @Override
    public boolean isEquals(String reference)
    {
        return Objects.equals(this.referencing, reference);
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
            return true;
        if (!(object instanceof InputReferenceImpl))
            return false;
        InputReferenceImpl<?> that = (InputReferenceImpl<?>) object;
        return Objects.equals(this.getReferencing(), that.getReferencing())
                && Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getReferencing(), this.getValue());
    }

    @Override
    public void resolve(@Nullable T value)
    {
        this.value = value;
        this.isResolved = true;
    }

    @Override
    public void resolve(@NotNull StructureSerializer serializer, @NotNull SessionStorage variables)
            throws InvalidScenarioFileException
    {
        if (this.rawValue != null)
        {
            this.resolve(this.smartCast(serializer, this.rawValue));
            return;
        }
        if (this.containingReferences.length == 0)
            throw new BrokenReferenceException(null, "This reference doesn't contain any references: " + this.referencing);
        assert this.referencing != null;

        Object resolved = ReferenceResolver.resolveReferences(this.referencing, this.containingReferences, variables);

        if (ReferenceResolver.containsReference(resolved))
            throw new BrokenReferenceException(null, "Failed to resolve reference: " + this.referencing + " -> " + resolved);

        this.resolve(this.smartCast(serializer, resolved));
    }

    @Override
    public void release()
    {
        if (this.referencing == null && this.value == this.rawValue)
            return; // 最適化
        this.value = null;
        this.isResolved = false;
    }

    @Override
    public boolean isEmpty()
    {
        return this.value == null && this.referencing == null && this.rawValue == null;
    }

    private T smartCast(@NotNull StructureSerializer serializer, @Nullable Object resolved)
            throws InvalidScenarioFileException
    {
        return SmartCaster.smartCast(this.token, serializer, resolved);
    }
}
