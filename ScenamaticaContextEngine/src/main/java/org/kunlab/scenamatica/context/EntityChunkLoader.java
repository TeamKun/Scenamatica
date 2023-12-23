package org.kunlab.scenamatica.context;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityChunkLoader extends BukkitRunnable implements Listener
{
    private final Map</* entity: */ ? super Entity, /* previousTickLocation: */ Location> entities;
    private final List<? super Chunk> loadedChunks;
    private final List<? super Chunk> dontUnloadChunks;

    private boolean destryoed;

    public EntityChunkLoader(ScenamaticaRegistry registry)
    {
        this.entities = new HashMap<>();
        this.loadedChunks = new LinkedList<>();
        this.dontUnloadChunks = new ArrayList<>();

        this.runTaskTimer(registry.getPlugin(), 0, 1);
    }

    public void addEntity(Entity entity)
    {
        this.entities.put(entity, entity.getLocation());
        this.loadChunkSafe(entity.getChunk());
    }

    private void loadChunkSafe(Chunk chunk)
    {
        if (chunk.isForceLoaded())
        {
            this.dontUnloadChunks.add(chunk);
            return;
        }

        if (!this.loadedChunks.contains(chunk))
        {
            this.loadedChunks.add(chunk);
            chunk.setForceLoaded(true);
            chunk.load();
        }
    }

    private void unloadChunkSafe(Chunk chunk)
    {
        if (!(chunk.isForceLoaded() || this.canUnload(chunk)))
            return;
        else if (this.dontUnloadChunks.contains(chunk))
        {
            this.dontUnloadChunks.remove(chunk);
            return;
        }

        Chunk worldSpawnChunk = chunk.getWorld().getSpawnLocation().getChunk();
        if (chunk.equals(worldSpawnChunk))
            return;

        if (this.loadedChunks.contains(chunk))
        {
            this.loadedChunks.remove(chunk);
            chunk.setForceLoaded(false);
            chunk.unload();
        }
    }

    public void clear()
    {
        new ArrayList<>(this.entities.keySet()).stream()
                .map(Entity.class::cast)
                .forEach(this::removeEntity);
    }

    public void removeEntity(Entity entity)
    {
        this.entities.remove(entity);
        this.unloadChunkSafe(entity.getChunk());
    }

    private boolean canUnload(Chunk chunk)
    {
        return this.entities.keySet().stream()
                .filter(location -> location instanceof Entity)
                .map(location -> (Entity) location)
                .noneMatch(entity -> entity.getChunk().equals(chunk));
    }

    public void shutdown()
    {
        if (this.destryoed)
            throw new IllegalStateException("ChunkLoader already destroyed");

        this.destryoed = true;

        this.clear();
        this.cancel();
    }

    @Override
    public void run()
    {
        for (Map.Entry<? super Entity, Location> entry : new LinkedHashMap<>(this.entities).entrySet())
        {
            Entity entity = (Entity) entry.getKey();
            Location previousTickLocation = entry.getValue();
            Location currentLocation = entity.getLocation();

            boolean isEntityChunkChanged = previousTickLocation.equals(currentLocation)
                    || previousTickLocation.getChunk().equals(currentLocation.getChunk());
            if (isEntityChunkChanged)
                continue;

            this.unloadChunkSafe(previousTickLocation.getChunk());
            this.loadChunkSafe(currentLocation.getChunk());
            this.entities.put(entity, currentLocation);
        }
    }
}
