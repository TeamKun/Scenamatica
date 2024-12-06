package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;

import java.util.Map;

@Getter
public class BrokenReferenceException extends RuntimeScenarioException
{
    @Nullable
    private final String reference;
    @Nullable
    private final String partiallyResolvedReference;
    @Nullable
    private final Map<String, ?> partiallyResolvedValues;

    public BrokenReferenceException(@Nullable String reference, @Nullable String partiallyResolvedReference, @Nullable Map<String, ?> partiallyResolvedValues)
    {
        super(ScenarioResultCause.UNRESOLVED_REFERENCES, "Unable to resolve a reference: " + reference);
        this.reference = reference;
        this.partiallyResolvedReference = partiallyResolvedReference;
        this.partiallyResolvedValues = partiallyResolvedValues;
    }

    public BrokenReferenceException(@Nullable String message, @Nullable String reference)
    {
        super(ScenarioResultCause.UNRESOLVED_REFERENCES, message);
        this.reference = reference;
        this.partiallyResolvedReference = null;
        this.partiallyResolvedValues = null;
    }

    public BrokenReferenceException(String message, @Nullable String reference, @Nullable String partiallyResolvedReference, @Nullable Map<String, ?> partiallyResolvedValues)
    {
        super(ScenarioResultCause.UNRESOLVED_REFERENCES, message);
        this.reference = reference;
        this.partiallyResolvedReference = partiallyResolvedReference;
        this.partiallyResolvedValues = partiallyResolvedValues;
    }

    public BrokenReferenceException(String message, @Nullable Object reference, @Nullable String partiallyResolvedReference, @Nullable Map<String, ?> partiallyResolvedValues)
    {
        super(ScenarioResultCause.UNRESOLVED_REFERENCES, message);
        this.reference = reference == null ? null: String.valueOf(reference);
        this.partiallyResolvedReference = partiallyResolvedReference;
        this.partiallyResolvedValues = partiallyResolvedValues;
    }
}
