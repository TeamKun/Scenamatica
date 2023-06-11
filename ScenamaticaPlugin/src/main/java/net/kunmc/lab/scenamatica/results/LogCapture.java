package net.kunmc.lab.scenamatica.results;

import lombok.Value;
import net.kunmc.lab.scenamatica.events.actions.server.ServerLogEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LogCapture implements Listener
{
    private final List<TestLogs> finalEntries;

    private List<LogEntry> currentEntries;
    private UUID currentTestID;

    public LogCapture()
    {
        this.finalEntries = new ArrayList<>();
    }

    public void init(@NotNull Plugin scenamatica)
    {
        Bukkit.getPluginManager().registerEvents(this, scenamatica);
    }

    public void startCapture(UUID testID)
    {
        this.currentTestID = testID;
        this.currentEntries = new ArrayList<>();
    }

    public void endCapture()
    {
        this.finalEntries.add(new TestLogs(this.currentTestID, this.currentEntries));
        this.currentTestID = null;
        this.currentEntries = null;
    }

    public void clear()
    {
        this.finalEntries.clear();
    }

    public List<LogEntry> getEntries(UUID testID)
    {
        return this.finalEntries.stream()
                .filter(e -> e.getTestID().equals(testID))
                .findFirst()
                .map(TestLogs::getEntries)
                .orElse(null);
    }

    public List<TestLogs> getFinalEntries()
    {
        return this.finalEntries;
    }

    @EventHandler
    public void onLog(ServerLogEvent e)
    {
        if (this.currentTestID == null || this.currentEntries == null)
            return;

        this.currentEntries.add(new LogEntry(
                        e.getLevelName(),
                        System.currentTimeMillis(),
                        e.getMessage(),
                        this.currentTestID
                )
        );
    }

    @Value
    public static class LogEntry
    {
        private static final SimpleDateFormat DT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private static final String LOG_FORMAT = "[%s %s] %s";

        String level;
        long time;
        String line;

        UUID testID;

        @Override
        public String toString()
        {
            return String.format(
                    LOG_FORMAT,
                    DT_FORMATTER.format(new Date(this.time)),
                    this.level,
                    this.line
            );
        }
    }

    @Value
    public static class TestLogs
    {
        UUID testID;
        List<LogEntry> entries;
    }
}
