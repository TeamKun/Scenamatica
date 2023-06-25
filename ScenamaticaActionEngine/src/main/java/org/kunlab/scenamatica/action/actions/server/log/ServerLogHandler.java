package org.kunlab.scenamatica.action.actions.server.log;

import org.kunlab.scenamatica.events.actions.server.ServerLogEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.bukkit.Server;

public class ServerLogHandler extends AbstractAppender
{
    private static final Logger ROOT;

    static
    {
        ROOT = (Logger) LogManager.getRootLogger();
    }

    private final Server server;

    public ServerLogHandler(Server server)
    {
        super("ScenamaticaLogWatcher", null, null, true, Property.EMPTY_ARRAY);

        this.server = server;
    }

    public void init()
    {
        this.start();  // これがないと例外ループする。
        ROOT.addAppender(this);
    }

    public void shutdown()
    {
        this.stop();
        ROOT.removeAppender(this);
    }

    @Override
    public void append(LogEvent event)
    {
        LogEvent e = event.toImmutable();

        String sender = e.getLoggerName();
        String message = e.getMessage().getFormattedMessage();
        Level level = e.getLevel();


        this.server.getPluginManager().callEvent(new ServerLogEvent(sender, message, level));
    }
}
