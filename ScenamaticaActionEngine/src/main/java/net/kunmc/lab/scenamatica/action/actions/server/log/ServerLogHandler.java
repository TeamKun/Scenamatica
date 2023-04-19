package net.kunmc.lab.scenamatica.action.actions.server.log;

import lombok.AllArgsConstructor;
import org.bukkit.Server;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@AllArgsConstructor
public class ServerLogHandler extends Handler
{
    private final Server server;

    public void init()
    {
        this.server.getLogger().addHandler(this);
    }

    public void shutdown()
    {
        this.server.getLogger().removeHandler(this);
    }

    @Override
    public void publish(LogRecord record)
    {
        String sender = record.getLoggerName();
        String message = record.getMessage();
        Level level = record.getLevel();

        this.server.getPluginManager().callEvent(new ServerLogEvent(sender, message, level));
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {

    }
}
