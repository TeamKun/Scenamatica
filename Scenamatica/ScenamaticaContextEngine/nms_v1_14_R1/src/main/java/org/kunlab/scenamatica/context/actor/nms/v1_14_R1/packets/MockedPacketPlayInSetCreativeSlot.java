package org.kunlab.scenamatica.context.actor.nms.v1_14_R1.packets;

import lombok.Getter;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.PacketPlayInSetCreativeSlot;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;

@Getter
public class MockedPacketPlayInSetCreativeSlot extends PacketPlayInSetCreativeSlot
{
    private final int slot;
    private final ItemStack itemStack;

    public MockedPacketPlayInSetCreativeSlot(int slot, org.bukkit.inventory.ItemStack bukkitItemStack)
    {
        this.slot = slot;
        this.itemStack = CraftItemStack.asNMSCopy(bukkitItemStack);
    }

    @Override
    public int b()
    {
        return this.slot;
    }
}
