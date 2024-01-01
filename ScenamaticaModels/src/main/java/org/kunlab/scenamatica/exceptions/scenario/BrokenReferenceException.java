package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;

@Getter
public class BrokenReferenceException extends IllegalArgumentException
{
    private final String reference;

    public BrokenReferenceException(String reference)
    {
        super("Unknown reference: " + reference);
        this.reference = reference;
    }

}
