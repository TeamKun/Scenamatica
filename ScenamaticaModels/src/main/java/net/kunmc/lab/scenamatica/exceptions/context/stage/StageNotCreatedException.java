package net.kunmc.lab.scenamatica.exceptions.context.stage;

import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;

public class StageNotCreatedException extends ContextPreparationException
{
    public StageNotCreatedException()
    {
        super("Stage is not created.");
    }
}
