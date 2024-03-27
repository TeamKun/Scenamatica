package org.kunlab.scenamatica.results;

import lombok.Getter;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.events.actions.server.ServerLogEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class LogCapture implements Listener
{
    @Getter
    private final List<TestLogs> finalEntries;

    private List<LogEntry> currentEntries;
    private UUID currentTestID;

    public LogCapture()
    {
        this.finalEntries = new LinkedList<>();
    }

    public void init(@NotNull Plugin scenamatica)
    {
        Bukkit.getPluginManager().registerEvents(this, scenamatica);
    }

    public void startCapture(UUID testID)
    {
        this.currentTestID = testID;
        this.currentEntries = new LinkedList<>();
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
            String unstyledLog = ChatColor.stripColor(this.line);

            return String.format(
                    LOG_FORMAT,
                    DT_FORMATTER.format(new Date(this.time)),
                    this.level,
                    unstyledLog
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
