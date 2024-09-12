package org.kunlab.scenamatica.nms.impl.v1_20_R3.world;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.kunlab.scenamatica.nms.types.world.NMSPersistentEntitySectionManager;

import java.lang.reflect.Method;

public class NMSPersistentEntitySectionManagerImpl<E extends org.bukkit.entity.Entity> implements NMSPersistentEntitySectionManager<E>
{
    private static final Method mStartTracking;  // Lnet/minecraft/world/level/entity/PersistentEntitySectionManager; -> c#(Lnet/minecraft/world/entity/EntityAccess;)V
    private static final Method mStartTicking;  // Lnet/minecraft/world/level/entity/PersistentEntitySectionManager; -> e#(Lnet/minecraft/world/entity/EntityAccess;)V

    static
    {
        try
        {
            mStartTracking = PersistentEntitySectionManager.class.getDeclaredMethod("e", EntityAccess.class);
            mStartTicking = PersistentEntitySectionManager.class.getDeclaredMethod("f", EntityAccess.class);


            mStartTracking.setAccessible(true);
            mStartTicking.setAccessible(true);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private final PersistentEntitySectionManager<Entity> nmsMgr;

    public NMSPersistentEntitySectionManagerImpl(PersistentEntitySectionManager<Entity> mgr)
    {
        this.nmsMgr = mgr;
    }

    private static Entity toNMSEntity(org.bukkit.entity.Entity entity)
    {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public void startTracking(E entity)
    {
        try
        {
            mStartTracking.invoke(this.nmsMgr, toNMSEntity(entity));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void startTicking(E entity)
    {
        try
        {
            mStartTicking.invoke(this.nmsMgr, toNMSEntity(entity));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
}
