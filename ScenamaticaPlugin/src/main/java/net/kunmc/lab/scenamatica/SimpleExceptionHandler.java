package net.kunmc.lab.scenamatica;

import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class SimpleExceptionHandler implements ExceptionHandler
{
    private final Logger logger;

    @Override
    public void report(Throwable e)
    {
        this.logger.log(Level.WARNING, "An unexpected exception occurred while operating Scenamatica daemon.", e);
    }
}
