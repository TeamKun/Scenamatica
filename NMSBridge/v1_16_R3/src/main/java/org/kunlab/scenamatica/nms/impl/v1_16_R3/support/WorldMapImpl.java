package org.kunlab.scenamatica.nms.impl.v1_16_R3.support;

import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.kunlab.scenamatica.nms.impl.v1_16_R3.WrapperProviderImpl;
import org.kunlab.scenamatica.nms.supports.WorldMap;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;

import java.util.Map;

public class WorldMapImpl extends WorldMap
{
    public WorldMapImpl(Map<?, ?> raw)
    {
        super(raw);
    }

    @Override
    protected boolean isSameWorld(Object key, NamespacedKey bukkitKey)
    {
        // noinspection unchecked
        ResourceKey<WorldServer> nmsKey = (ResourceKey<WorldServer>) key;
        MinecraftKey mcWorldKey = CraftNamespacedKey.toMinecraft(bukkitKey);
        return nmsKey.a().equals(mcWorldKey);
    }

    @Override
    protected NMSWorldServer constructWorld(Object key, World world)
    {
        return WrapperProviderImpl.wrap$(world);
    }
}
