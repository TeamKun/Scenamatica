package org.kunlab.scenamatica.context.stage.nms.v_1_16_5;

import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.kunlab.scenamatica.context.stage.MinecraftServerController;

import java.util.Iterator;
import java.util.Map;

public class MinecraftServerControllerImpl implements MinecraftServerController
{
    private final MinecraftServer server;

    public MinecraftServerControllerImpl()
    {
        this.server = ((CraftServer) Bukkit.getServer()).getServer();
    }

    @Override
    public void removeWorld(NamespacedKey key)
    {
        Iterator<Map.Entry<ResourceKey<World>, WorldServer>> iterator = this.server.worldServer.entrySet().iterator();
        MinecraftKey minecraftKey = CraftNamespacedKey.toMinecraft(key);

        while (iterator.hasNext())
        {
            Map.Entry<ResourceKey<World>, WorldServer> entry = iterator.next();
            ResourceKey<World> resourceKey = entry.getKey();

            if (resourceKey.a().equals(minecraftKey))
                iterator.remove();
        }
    }
}
