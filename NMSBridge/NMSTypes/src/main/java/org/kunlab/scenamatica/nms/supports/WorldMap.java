package org.kunlab.scenamatica.nms.supports;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;

import java.util.Map;

public abstract class WorldMap implements NMSWrapped
{
    protected final Map<?, ?> raw;

    protected WorldMap(final Map<?, ?> raw)
    {
        this.raw = raw;
    }

    protected abstract boolean isSameWorld(final Object key, final NamespacedKey bukkitKey);

    public NMSWorldServer getWorld(final NamespacedKey key)
    {
        for (Map.Entry<?, ?> entry : this.raw.entrySet())
        {
            if (this.isSameWorld(entry.getKey(), key))
                return this.constructWorld(entry.getKey(), (World) entry.getValue());
        }

        return null;
    }

    public void removeWorld(final NamespacedKey key)
    {
        this.raw.entrySet().removeIf(entry -> this.isSameWorld(entry.getKey(), key));
    }

    public void removeWorld(final World world)
    {
        this.raw.entrySet().removeIf(entry -> this.isSameWorld(entry.getKey(), world.getKey()));
    }

    protected abstract NMSWorldServer constructWorld(final Object key, final World world);

    @Override
    public Object getNMSRaw()
    {
        return this.raw;
    }

    @Override
    public Object getNMSCraftRaw()
    {
        return null;
    }
}
