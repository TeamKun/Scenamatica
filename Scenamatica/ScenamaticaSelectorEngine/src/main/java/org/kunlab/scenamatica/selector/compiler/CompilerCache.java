package org.kunlab.scenamatica.selector.compiler;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.selector.Selector;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompilerCache
{
    private static final int MAX_CACHES = 10;

    private final Map<String, CacheEntry> cache;
    private int caches;

    public CompilerCache()
    {
        this.cache = new ConcurrentHashMap<>();
    }

    @Nullable
    public Selector get(String selector, boolean hasBasis)
    {
        CacheEntry entry = this.cache.get(selector);
        if (entry == null)
            return null;
        else if (entry.hasBasis != hasBasis)
            return null;

        entry.hit();
        return entry.getSelector();
    }

    public void cache(String selector, Selector compiled, boolean hasBasis)
    {
        if (this.caches >= MAX_CACHES)
            this.dropOneCache();

        this.cache.put(selector, new CacheEntry(compiled, hasBasis));
        this.caches++;
    }

    private void dropOneCache()
    {
        this.cache.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().getHits()))
                .ifPresent(e -> {
                    this.cache.remove(e.getKey());
                    this.caches--;
                });
    }

    @Data
    private static class CacheEntry
    {
        private final Selector selector;
        private final boolean hasBasis;
        private int hits;

        public void hit()
        {
            this.hits++;
        }
    }
}
