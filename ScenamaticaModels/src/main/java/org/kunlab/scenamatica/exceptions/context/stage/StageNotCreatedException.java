package org.kunlab.scenamatica.exceptions.context.stage;

import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;

public class StageNotCreatedException extends ContextPreparationException
{
    public StageNotCreatedException()
    {
        super("Stage is not created.");
    }
}
