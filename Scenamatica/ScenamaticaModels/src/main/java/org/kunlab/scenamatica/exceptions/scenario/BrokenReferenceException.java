package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class BrokenReferenceException extends IllegalArgumentException
{
    @Nullable
    private final String reference;

    public BrokenReferenceException(@Nullable String reference)
    {
        super("Unknown reference: " + reference);
        this.reference = reference;
    }

    public BrokenReferenceException(String message, @Nullable String reference)
    {
        super(message);
        this.reference = reference;
    }

    public BrokenReferenceException(String message, @Nullable Object reference)
    {
        super(message);
        this.reference = reference == null ? null: String.valueOf(reference);
    }
}
