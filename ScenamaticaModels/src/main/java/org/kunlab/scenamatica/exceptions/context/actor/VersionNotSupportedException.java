package org.kunlab.scenamatica.exceptions.context.actor;

import lombok.Getter;
import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;

/**
 * バージョンがサポートされていないことを示す例外です。
 */
@Getter
public class VersionNotSupportedException extends ContextPreparationException
{
    private final String version;

    public VersionNotSupportedException(String version)
    {
        super("Bukkit version " + version + " is not supported.");
        this.version = version;
    }
}
