package org.kunlab.scenamatica.context.actor.nms.v1_15_R1;

import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.ItemActionContext;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MovingObjectPositionBlock;

public class MockedItemActionContext extends ItemActionContext
{
    public MockedItemActionContext(EntityHuman var0, EnumHand var1, ItemStack var2, MovingObjectPositionBlock var3)
    {
        super(var0.world, var0, var1, var2, var3);
    }
}
