package org.kunlab.scenamatica.bookkeeper.utils;

import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Timekeeper
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Logger logger;
    private final String topic;

    private long startedAt;
    private long endedAt;
    private boolean interrupted;

    public Timekeeper(Logger logger, String topic)
    {
        this.logger = logger;
        this.topic = topic;
    }

    public void start()
    {
        this.startedAt = System.currentTimeMillis();

        this.logger.info("[{}] Task started at {}", this.topic, format(this.startedAt));
    }

    public void end()
    {
        if (!isRunning())
            throw new IllegalStateException("Task " + this.topic + " is not running");

        this.endedAt = System.currentTimeMillis();

        this.logger.info("[{}] Task ended at {} (duration: {})", this.topic, format(this.endedAt), formatDuration(this.endedAt - this.startedAt));
    }

    public void interrupt()
    {
        this.interrupted = true;

        this.logger.info("[{}] Task interrupted at {} (duration: {})", this.topic, format(System.currentTimeMillis()), formatDuration(System.currentTimeMillis() - this.startedAt));
    }

    public boolean isRunning()
    {
        return this.startedAt > 0 && this.endedAt == 0;
    }

    private static String format(long ms)
    {
        return FORMATTER.format(LocalDateTime.ofEpochSecond(ms / 1000, 0, OffsetDateTime.now().getOffset()));
    }

    private static String formatDuration(long ms)
    {
        if (ms < 1000)
            return ms + "ms";

        return Duration.ofMillis(ms).toString();
    }
}
