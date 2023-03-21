package net.kunmc.lab.scenamatica;

import lombok.Builder;
import lombok.Getter;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaEnvironment;

import java.util.logging.Logger;

@Getter
@Builder
public class Environment implements ScenamaticaEnvironment
{
    private final Logger logger;
    private final ExceptionHandler exceptionHandler;
}
