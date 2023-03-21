package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.WorldServer;

class MockedPlayer extends EntityPlayer
{
    public MockedPlayer(MinecraftServer minecraftserver,
                        WorldServer worldserver,
                        GameProfile gameprofile)
    {
        super(minecraftserver, worldserver, gameprofile, new MocketPlayerInteractManager(worldserver));
    }


}
