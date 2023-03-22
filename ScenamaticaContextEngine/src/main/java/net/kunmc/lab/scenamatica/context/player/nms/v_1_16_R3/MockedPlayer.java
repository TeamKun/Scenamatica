package net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.util.Vector;

class MockedPlayer extends EntityPlayer
{
    public MockedPlayer(MinecraftServer minecraftserver,
                        WorldServer worldserver,
                        GameProfile gameprofile)
    {
        super(minecraftserver, worldserver, gameprofile, new MocketPlayerInteractManager(worldserver));
        this.setNoGravity(false);
    }

    @Override
    public boolean doAITick()
    {
        return true;  // ノックバック用
    }

    @Override
    public void tick()
    {
        super.tick();

        this.processGravity();
    }

    private void processGravity()
    {
        Vec3D vec = getMot();

        if (!this.onGround)
        {
            if (this.inWater)
                vec = vec.add(0, -0.0252f, 0);
            else
                vec = vec.add(0, -0.4, 0);
        }

        this.move(EnumMoveType.SELF, vec);
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float f)
    {
        System.out.println("damageEntity");
        Entity damager = damageSource.getEntity();

        boolean damaged = super.damageEntity(damageSource, f);

        if (damaged && damager != null)
            Runner.run(() -> applyKnockBack(damager));

        return damaged;
    }

    private void applyKnockBack(Entity damager)
    {
        if (this.dead)
            return;

        float force = 3.55f;

        Vector direction = this.getBukkitEntity().getLocation().toVector()
                .subtract(damager.getBukkitEntity().getLocation().toVector())
                .normalize();
        Vector relVec = direction.clone().setY(1.7);
        relVec.multiply(0.5 / Math.max(1.0, relVec.length()))
                .setX(relVec.getX() * force)
                .setZ(relVec.getZ() * force)
                .multiply(1.2).add(relVec);

        Vec3D vec = new Vec3D(relVec.getX(), relVec.getY(), relVec.getZ());

        this.move(EnumMoveType.SELF, vec);
    }
}
