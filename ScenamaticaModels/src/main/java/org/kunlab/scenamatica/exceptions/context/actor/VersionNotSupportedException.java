package org.kunlab.scenamatica.exceptions.context.actor;

import lombok.Getter;

/**
 * バージョンがサポートされていないことを示す例外です。
 */
@Getter
public class VersionNotSupportedException extends ActorCreationException
{
    private final String version;

    public VersionNotSupportedException(String version)
    {
        super("Bukkit version " + version + " is not supported.");
        this.version = version;
    }
}
